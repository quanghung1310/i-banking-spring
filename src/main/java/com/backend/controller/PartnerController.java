package com.backend.controller;

import com.backend.constants.ActionConstant;
import com.backend.constants.ErrorConstant;
import com.backend.dto.AccountPaymentDTO;
import com.backend.dto.TransactionDTO;
import com.backend.model.Account;
import com.backend.model.Partner;
import com.backend.model.Transaction;
import com.backend.model.request.bank.QueryAccountRequest;
import com.backend.model.request.partner.GenerateQueryAccountRequest;
import com.backend.model.request.partner.GenerateTransfer;
import com.backend.model.request.transaction.TransferRequest;
import com.backend.model.response.BaseResponse;
import com.backend.model.response.UserResponse;
import com.backend.process.PartnerProcess;
import com.backend.process.TransactionProcess;
import com.backend.service.IAccountPaymentService;
import com.backend.service.IPartnerService;
import com.backend.service.ITransactionService;
import com.backend.service.IUserService;
import com.backend.util.DataUtil;
import com.google.gson.Gson;
import io.vertx.core.impl.StringEscapeUtils;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@Controller
public class PartnerController {
    private static final Logger logger = LogManager.getLogger(PartnerController.class);

    private static final Gson PARSER = new Gson();

//    private static final long SESSION = 300000; //TODO CONFIG IN PROPERTIES

    @Value( "${type.account.payment}" )
    private int paymentBank;

    @Value( "${my.bank.code}" )
    private String myBankCode;

    @Value( "${my.bank.id}" )
    private long myBankId;

    @Value( "${session.request}" )
    private int session;

    @Value( "${fee.transfer}" )
    private int feeTransfer;

    private IPartnerService partnerService;
    private IUserService userService;
    private IAccountPaymentService accountPaymentService;
    private ITransactionService transactionService;

    @Autowired
    public PartnerController(IPartnerService partnerService,
                             IUserService userService,
                             IAccountPaymentService accountPaymentService,
                             ITransactionService transactionService) {
        this.partnerService = partnerService;
        this.userService = userService;
        this.accountPaymentService = accountPaymentService;
        this.transactionService = transactionService;
    }

    @PostMapping(value = "/transfer-bank")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
            //Step 0: Validate request
            //0.1. Base request
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
            if (System.currentTimeMillis() - request.getRequestTime() > session) {
                logger.warn("{}| Request - {} out of session with - {} milliseconds!", logId, request.getRequestId(), session);
                response = DataUtil.buildResponse(ErrorConstant.TIME_EXPIRED, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data session success!", logId);

            //Step 3: A kiểm tra xem gói tin B gửi qua là gói tin nguyên bản hay gói tin đã bị chỉnh sửa
            if (!PartnerProcess.validateTransferHash(logId, partner, request)) {
                logger.warn("{}| Hash - {} wrong!", logId, request.getHash());
                response = DataUtil.buildResponse(ErrorConstant.HASH_NOT_VALID, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid request hash success!", logId);

            //Step 4: verify chữ ký bất đối xứng PGP
            String dataSig = new JsonObject()
                    .put("bankCode", request.getBankCode())
                    .put("cardName", request.getCardName())
                    .put("from", request.getFrom())
                    .put("isTransfer", request.getIsTransfer())
                    .put("partnerCode", request.getPartnerCode())
                    .put("requestId", request.getRequestId())
                    .put("requestTime", request.getRequestTime())
                    .put("to", request.getTo())
                    .put("typeFee", request.getTypeFee())
                    .put("value", request.getValue())
                    .put("hash", request.getHash())
                    .toString();
            boolean isVerify = PartnerProcess.verifySignaturePgp(logId, StringEscapeUtils.unescapeJava(request.getSignature()).getBytes(), partner.getPublicKey());
            if (!isVerify) {
                logger.warn("{}| Signature - {} wrong!", logId, request.getSignature());
                response = DataUtil.buildResponse(ErrorConstant.CHECK_SIGNATURE_FAIL, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid request signature success!", logId);

            //Step 5: process transfer
            Account account = toUser.getAccount().get(0);
            long newBalance = 0;
            long currentBalance = account.balance;
            long accountId = account.id;
            long balanceTransfer = request.getValue();

            if (!request.getIsTransfer()) {
                //5.1: Check balance
                if (newBalance > currentBalance) {
                    logger.warn("{}| Current balance account - {} can't transfer!", logId, accountId);
                    response = DataUtil.buildResponse(ErrorConstant.BAD_REQUEST, request.getRequestId(), null);
                    return new ResponseEntity<>(
                            response.toString(),
                            HttpStatus.BAD_REQUEST);
                }
            }
            newBalance = TransactionProcess.newBalance(false, request.getTypeFee() != 1, feeTransfer, balanceTransfer, currentBalance);

            //5.2: Insert transaction
            Timestamp currentTime = new Timestamp(request.getRequestTime());
            TransactionDTO transactionDTO = TransactionProcess.createTrans(request.getFrom(),
                    request.getTo(),
                    request.getCardName(),
                    request.getValue(),
                    request.getTypeFee(),
                    1,
                    partner.getId(),
                    request.getDescription(),
                    ActionConstant.COMPLETED.name(),
                    currentTime,
                    currentTime,
                    feeTransfer);
            transactionDTO.setCardName(request.getCardName());
            Transaction transaction = transactionService.saveTransaction(transactionDTO);
            if (transaction == null) {
                logger.warn("{}| Insert new transaction fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            logger.info("{}| Insert new transaction success with transId: {}!", logId, transaction.getTransId());

            //5.3: Update balance
            AccountPaymentDTO accountPaymentDTO = accountPaymentService.updateBalance(logId, accountId, newBalance);
            if (accountPaymentDTO == null) {
                logger.warn("{}| Update new balance - {} to account - {} fail!", logId, newBalance, accountId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, transaction.toString());
                logger.info("{}| Response to client: {}", logId, transaction.toString());
                return new ResponseEntity<>(response.toString(), HttpStatus.OK);
            }
        } catch (Exception ex) {
            logger.error("{}| Request transfer bank catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(),null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/account-bank")
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
//            if (System.currentTimeMillis() - request.getRequestTime() > session) {
//                logger.warn("{}| Request - {} out of session with - {} milliseconds!", logId, request.getRequestId(), session);
//                response = DataUtil.buildResponse(ErrorConstant.TIME_EXPIRED, request.getRequestId(),null);
//                return new ResponseEntity<>(
//                        response.toString(),
//                        HttpStatus.BAD_REQUEST);
//            }
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
            return DataUtil.getStringResponseEntity(logId, userResponse);

        } catch (Exception ex) {
            logger.error("{}| Request query account bank catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(),null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = {"/get-banks", "/get-banks/{bankId}"})
    public ResponseEntity<String> getBanks(@PathVariable(required = false) Integer bankId) {
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request data: bankId - {}", logId, bankId);
        BaseResponse response;
        try {
            List<Partner> partners = new ArrayList<>();
            if (bankId == null) {
                partners = partnerService.getAll();
            } else {
                Partner partner = partnerService.findById(bankId);
                if (partner != null) {
                    partners.add(partner);

                }
            }
            logger.warn("{}| Response to client with size: {}", logId, partners.size());

            if (partners.size() <= 0) {
                response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.OK);
            } else {
                response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, new JsonObject().put("partners", partners).toString());
                return new ResponseEntity<>(response.toString(), HttpStatus.OK);
            }
        } catch (Exception ex) {
            logger.error("{}| Request query account bank catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/generate-query-account")
    public ResponseEntity<String> generateQueryAccount(@RequestBody GenerateQueryAccountRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
            if (!request.isValidData()) {
                logger.warn("{}| Validate request generate query account bank data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request generate query account bank success!", logId);

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

            String hash = PartnerProcess.generateQueryAccountHash(logId, request);
            if (StringUtils.isBlank(hash)) {
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, new JsonObject().put("hash", hash).toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);

        } catch (Exception ex) {
            logger.error("{}| Request query account bank catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(),null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/generate-transfer")
    public ResponseEntity<String> generateTransfefr(@RequestBody GenerateTransfer request) {
        String logId = request.getRequestId();
        logger.info("{}| Request generate transfer data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
            //Step 0: Validate request
            //0.1. Base request
            if (!request.isValidData()) {
                logger.warn("{}| Validate request generate transfer bank data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request generate transfer bank success!", logId);

            //0.2. Info my bank
            Partner myBank = partnerService.findByPartnerCode(request.getBankCode());
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
            Partner partner = partnerService.findByPartnerCode(request.getPartnerCode());
            if (partner == null) {
                logger.warn("{}| Partner - {} not fount!", logId, request.getPartnerCode());
                response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data partner success!", logId);

            //Step 3: A kiểm tra xem gói tin B gửi qua là gói tin nguyên bản hay gói tin đã bị chỉnh sửa
            String hash = PartnerProcess.generateTransferHash(logId, request);
            if (StringUtils.isBlank(hash)) {
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            //Step 4: verify chữ ký bất đối xứng PGP
            String dataSig = new JsonObject()
                    .put("bankCode", request.getBankCode())
                    .put("cardName", request.getCardName())
                    .put("from", request.getFrom())
                    .put("isTransfer", request.getIsTransfer())
                    .put("partnerCode", request.getPartnerCode())
                    .put("requestId", request.getRequestId())
                    .put("requestTime", request.getRequestTime())
                    .put("to", request.getTo())
                    .put("typeFee", request.getTypeFee())
                    .put("value", request.getValue())
                    .put("hash", hash)
                    .toString();
            PGPSecretKey pgpSecretKey = PartnerProcess.readSecretKey(partner.getSecretKey(),
                    PartnerProcess.readPublicKey(partner.getPublicKey()).getKeyID());
            String signature = PartnerProcess.signaturePgp(dataSig, pgpSecretKey, partner.getPassword().toCharArray());

            if (StringUtils.isBlank(signature)) {
                logger.warn("{}| Generate signature: fail", logId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, new JsonObject().put("hash", hash)
                    .put("signature", signature)
                    .toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request transfer bank catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(),null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }
}
