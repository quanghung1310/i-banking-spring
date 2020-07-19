package com.backend;

import com.backend.constants.ErrorConstant;
import com.backend.dto.AccountPaymentDTO;
import com.backend.model.Account;
import com.backend.model.Partner;
import com.backend.model.request.QueryAccountRequest;
import com.backend.model.request.TransferRequest;
import com.backend.model.response.BaseResponse;
import com.backend.model.response.UserResponse;
import com.backend.process.PartnerProcess;
import com.backend.service.IAccountPaymentService;
import com.backend.service.IPartnerService;
import com.backend.service.IUserService;
import com.backend.util.DataUtil;
import com.google.gson.Gson;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class PartnerController {
    private static final Logger logger = LogManager.getLogger(PartnerController.class);

    private static final Gson PARSER = new Gson();

    private static final long SESSION = 300000; //TODO CONFIG IN PROPERTIES

    @Value( "${type.account.payment}" )
    private int paymentBank;

    @Value( "${my.bank.code}" )
    private String myBankCode;

    @Value( "${my.bank.id}" )
    private long myBankId;

    @Autowired
    IPartnerService partnerService;

    @Autowired
    IUserService userService;

    @Autowired
    IAccountPaymentService accountPaymentService;
    //todo api create merchant

    @PostMapping(value = "/transfer/bank")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
            //Step 0: Validate request
            //0.1. Base reuqest
            if (!request.isValidData()) {
                logger.warn("{}| Validate request transfer bank data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request transfer bank success!", logId);

            //0.2. Info my bank
            Partner myBank  = partnerService.findByPartnerCode(request.getBankCode());
            if (myBank == null) {
                logger.warn("{}| My bank - {} not fount!", logId, request.getBankCode());
                response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.BAD_REQUEST);
            }
            UserResponse toUser = userService.queryAccount(logId, request.getTo(), myBankId, paymentBank, true);
            if (toUser == null) {
                logger.warn("{}| Account target - {} not fount!", logId, request.getTo());
                response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid information my bank success!", logId);

            //Step 1: A kiểm tra lời gọi api có phải xuất phát từ B (đã đăng ký liên kết từ trước) hay không
            Partner partner = partnerService.findByPartnerCode(request.getMerchantCode());
            if (partner == null) {
                logger.warn("{}| Partner - {} not fount!", logId, request.getMerchantCode());
                response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data partner success!", logId);

            //Step 2: A kiểm tra xem lời gọi này là mới hay là thông tin cũ đã quá hạn
            if (request.getRequestTime() < SESSION) { //todo config in properties
                logger.warn("{}| Request - {} out of session with - {} milliseconds!", logId, request.getRequestId(), SESSION);
                response = DataUtil.buildResponse(ErrorConstant.TIME_EXPIRED, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data session success!", logId);

            //Step 3: A kiểm tra xem gói tin B gửi qua là gói tin nguyên bản hay gói tin đã bị chỉnh sửa
            if (!PartnerProcess.validateTransferHash(logId, partner, request)) {
                logger.warn("{}| Hash - {} wrong!", logId, request.getHash());
                response = DataUtil.buildResponse(ErrorConstant.CHECK_SIGNATURE_FAIL, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid request hash success!", logId);

            //Step 4: verify chữ ký bất đối xứng PGP
            String dataSig = new JsonObject()
                    .put("bankCode", request.getBankCode())
                    .put("from", request.getFrom())
                    .put("isTransfer", request.getIsTransfer())
                    .put("merchantCode", request.getMerchantCode())
                    .put("requestId", request.getRequestId())
                    .put("requestTime", request.getRequestTime())
                    .put("to", request.getTo())
                    .put("value", request.getValue())
                    .put("hash", request.getHash()).toString();
            boolean isVerify = PartnerProcess.verifySignaturePgp(logId, dataSig.getBytes(), partner.getPublicKey());
            if (!isVerify) {
                logger.warn("{}| Signature - {} wrong!", logId, request.getHash());
                response = DataUtil.buildResponse(ErrorConstant.CHECK_SIGNATURE_FAIL, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid request signature success!", logId);

            //Step 5: process transfer
            Account account = toUser.getAccount().get(0);
            long newBalance = request.getValue();
            long currentBalance = account.balance;
            long accountId = account.id;

            if (request.getIsTransfer()) {
                newBalance += currentBalance;
            } else {
                //5.1: Check balance
                if (newBalance > currentBalance) {
                    logger.warn("{}| Current balance account - {} can't transfer!", logId, accountId);
                    response = DataUtil.buildResponse(ErrorConstant.BAD_REQUEST, request.getRequestId(),null);
                    return new ResponseEntity<>(
                            response.toString(),
                            HttpStatus.BAD_REQUEST);
                }
                newBalance = currentBalance - newBalance;
            }

            AccountPaymentDTO accountPaymentDTO = accountPaymentService.updateBalance(logId, accountId, newBalance);
            if (accountPaymentDTO == null) {
                logger.warn("{}| Update new balance - {} to account - {} fail!", logId, newBalance, accountId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                return new ResponseEntity<>(
                        "success",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            logger.error("{}| Request transfer bank catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(),null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/account/bank")
    public ResponseEntity<String> queryAccount(@RequestBody QueryAccountRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
            if (!request.isValidData()) {
                logger.warn("{}| Validate request query account bank data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request query account bank success!", logId);

            //Step 1: A kiểm tra lời gọi api có phải xuất phát từ B (đã đăng ký liên kết từ trước) hay không
            Partner partner = partnerService.findByPartnerCode(request.getPartnerCode());

            if (partner == null) {
                logger.warn("{}| Partner - {} not fount!", logId, request.getPartnerCode());
                response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data partner success!", logId);

            //Step 2: A kiểm tra xem lời gọi này là mới hay là thông tin cũ đã quá hạn
            if (request.getRequestTime() < SESSION) { //todo config in properties
                logger.warn("{}| Request - {} out of session with - {} milliseconds!", logId, request.getRequestId(), SESSION);
                response = DataUtil.buildResponse(ErrorConstant.TIME_EXPIRED, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data session success!", logId);

            //Step 3: A kiểm tra xem gói tin B gửi qua là gói tin nguyên bản hay gói tin đã bị chỉnh sửa
            if (!PartnerProcess.validateQueryAccountHash(logId, partner, request)) {
                logger.warn("{}| Hash - {} wrong!", logId, request.getHash());
                response = DataUtil.buildResponse(ErrorConstant.CHECK_SIGNATURE_FAIL, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid request hash success!", logId);

            //Step 4: Query info account
            UserResponse userResponse = userService.queryAccount(logId, request.getCardNumber(), myBankId, paymentBank, false);
            if (userResponse == null) {
                logger.warn("{}| query account fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, userResponse.toString());
            response.setData(new JsonObject(userResponse.toString()));
            logger.info("{}| Response to client: {}", logId, userResponse.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);

        } catch (Exception ex) {
            logger.error("{}| Request query account bank catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(),null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }
}
