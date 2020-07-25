package com.backend.controller;

import com.backend.constants.ActionConstant;
import com.backend.constants.ErrorConstant;
import com.backend.dto.AccountPaymentDTO;
import com.backend.dto.TransactionDTO;
import com.backend.model.Account;
import com.backend.model.Partner;
import com.backend.model.Transaction;
import com.backend.model.request.bank.QueryAccountRequest;
import com.backend.model.request.transaction.TransferRequest;
import com.backend.model.response.BaseResponse;
import com.backend.model.response.UserResponse;
import com.backend.process.PartnerProcess;
import com.backend.process.UserProcess;
import com.backend.service.IAccountPaymentService;
import com.backend.service.IPartnerService;
import com.backend.service.ITransactionService;
import com.backend.service.IUserService;
import com.backend.util.DataUtil;
import com.google.gson.Gson;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
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
//            if (System.currentTimeMillis() - request.getRequestTime() > session) {
//                logger.warn("{}| Request - {} out of session with - {} milliseconds!", logId, request.getRequestId(), session);
//                response = DataUtil.buildResponse(ErrorConstant.TIME_EXPIRED, request.getRequestId(),null);
//                return new ResponseEntity<>(
//                        response.toString(),
//                        HttpStatus.BAD_REQUEST);
//            }
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
            PGPSecretKey pgpSecretKey = PartnerProcess.readSecretKey(partner.getSecretKey(),
                    PartnerProcess.readPublicKey(partner.getPublicKey()).getKeyID());
            String genSig = PartnerProcess.signaturePgp(dataSig, pgpSecretKey, partner.getPassword().toCharArray());
            boolean isVerify = PartnerProcess.verifySignaturePgp(logId, genSig.getBytes(), partner.getPublicKey());
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
            newBalance = UserProcess.newBalance(false, request.getTypeFee(), feeTransfer, balanceTransfer, currentBalance);

            //5.2: Insert transaction
            Timestamp currentTime = new Timestamp(request.getRequestTime());
            TransactionDTO transactionDTO = UserProcess.createTrans(request.getFrom(),
                    request.getTo(),
                    request.getValue(),
                    request.getTypeFee(),
                    1,
                    partner.getId(),
                    request.getDescription(),
                    ActionConstant.COMPLETED.name(),
                    currentTime,
                    currentTime,
                    feeTransfer);

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
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

//    public static void main(String[] args) throws IOException, PGPException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException {
//        String logId = DataUtil.createRequestId();
//
//        String pri = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n" +
//                "Version: Keybase OpenPGP v1.0.0\n" +
//                "Comment: https://keybase.io/crypto\n" +
//                "\n" +
//                "xcFFBF8cr9ABBAD1FzxZdPVeUgR2k+cdcqtmuoWfxwtQTZyNY6NZzExDnDf+2+6c\n" +
//                "9nx/RRi9k3oPFk4phZ+JKSnEvxaWa2PecAyuuKfvCMkwptCNqWVOevweVzbF11VL\n" +
//                "HfURK5S8rvvw7etnh4lKUcEds0I5+tlbEFSV3f0zTfRpXIwlGgc8Y95hKwARAQAB\n" +
//                "/gkDCBw9ODvvUAdvYFDItyV8VaFtcEmnkNjMjfJNL+BpbvaKCjd5wuzt4n5HeYNA\n" +
//                "DfpokYnFFRCzmly/zhElVAyuW0tS1ry1P+6IOKugDQestczlx4NRdVMAmIBkuhtU\n" +
//                "7WEhRzlUnXQmYn6GiLBBc2C1yZWmxJMghv0bA5pHXkjdmKGz+xdCrbmuHxLAFg0y\n" +
//                "BWbau8PvFJ5+DGgJTBhHElj6HDWj+eYHaulQiWDFmO/EASnmL5+Y7zOQRss2Y65W\n" +
//                "UX8vT/vOHr63lFI0qUu7mMJOAJVmyvx3Jx31F4B/7oETDY9XCMTfZrYINTR82uQM\n" +
//                "MYlxynI6OYU5eM5P20gDC/1umCPrb2XkVPyEz+wQq6d75vt7lPqxi6mrMhMD2r7r\n" +
//                "9In3cZSz7+u7bFcyyCsAf7d1pVvyNeXbHgKJB7HZugnp/r1IrLxWpdM//1cThQRQ\n" +
//                "wRicEJS5niSvoD0DT6zoOiUh2XjoeZCtBz7MVNXlLs8RzT4TTP4ens0cSEhMIEJh\n" +
//                "bmsgPGhobGJhbmtAZ21haWwuY29tPsKtBBMBCgAXBQJfHK/QAhsvAwsJBwMVCggC\n" +
//                "HgECF4AACgkQRIlwUZR8Cgz0vQP+MCvW4fduIKp0PDNwtKDExASjJMLwFuCnYwPf\n" +
//                "b3byxZbV5fcgvu1Vsujmgf1ZG5v4I8I6mNccy7mIx6qZXPmibSQQXYbv7VKD5qYX\n" +
//                "9r6l0Jv2EEYv9u+pCKIsbON0k64OLD4Kq3vzzsnpfzvN/I4n5NZFCpGBETWZ9Wnh\n" +
//                "r2uBXCjHwUYEXxyv0AEEAMmzM/nzYfB9M+IyRppmMac1ecOuZ1XFxU7AcywITocF\n" +
//                "i+/5kCKMXzD5OdnOkFk6kDEHacioXMS/wIqAZoZYU6OBP+ngAzREEtDfTNbTOWro\n" +
//                "2iLzYGVk92CJsF1xkedr7qY6THWEibUYOvW4nZvNV67GhBNHpKGt35kxFdtKhiaH\n" +
//                "ABEBAAH+CQMI1mGGxfDxbGhg1UZcXfGcTiPIGo9LCZlWh3DLR3JDLZy9znV2X4j2\n" +
//                "rWK9n9DSK+BPQQIxaUVngD+O/Yi+zfUhcGeXcTwJHeqlQB/YBcV5Tm+4Z/N+kXw9\n" +
//                "DL0iKtHEKGTfZ/rtcq8XxFcTJo21iyGk/i2F4P2Z/I83mvTA1ASh2CQqjk/gnS+c\n" +
//                "e916O8AxZWMJ8eTuRowgCBuCRTYvnvm7RP7qaGpMEAIHaRFLDg9Hfc2xW1PbYwGb\n" +
//                "ltped2PngJ4n1GwWLlBipxcN+LG2F6DWewrQwVgzsGWgpRgldS2vnVRQ0kqXuIdH\n" +
//                "WanHSxReyegVWvJ2lixBDNyKEoigyUQBYphhiROyIWfrYRUM2asnMZQLIMfIrc8a\n" +
//                "cefzdItBUbMjqY7TEo/Zo08uNKa4xw6azKp46IimdrkBNz7d3ZosBYAVJkGtlC0u\n" +
//                "QpdDQc7NYHnj7aSEKQ6hpHSANTmw53ZYs3dtAunoV76SNa/aHnCoEF8dXUlHb8LA\n" +
//                "gwQYAQoADwUCXxyv0AUJDwmcAAIbLgCoCRBEiXBRlHwKDJ0gBBkBCgAGBQJfHK/Q\n" +
//                "AAoJEFVUfSuV5Da/TXYEALiEs5RCvpGsqwF+f1NTA0iovYG4FIhWvAIV2juxfWsj\n" +
//                "huWAzSKWlW3gYhThmq+khhoEOn61d2BtEezTAS29pFk2cXf76SonnJ7bxNVDLyoC\n" +
//                "i8cQWK/ZVN9MVafnuGw21DsMaIW4JQ9HYuCdhU3iaA3AsbPmiie8daCbEhUBHUkZ\n" +
//                "8/wD/0wZlrKsZKN6yrLErjtvvsxKnelMOVHERNKiQBapyYax3Y8w3GIuf6EizxXd\n" +
//                "e7mbWaBzQQrpAYiZatI9k1PXisHhS5lw8qMEA770tJmlsv4ketOlHQ/HRx2qpJM5\n" +
//                "MmqsTmF9UQctys8DATEF9bg2h5Vw+uyENvG94jRALJCFzrbDx8FGBF8cr9ABBADb\n" +
//                "mOv/sIUY5Vk05CZ+g0cJL7XaCOjsHf26/w6l0f0WoNjuA8aDu91S1YlIJN7sEo2F\n" +
//                "rfuHTy9cUKZUAVjHjvOSuU/d6MKK8swNWKGg5u+evfXBS7J5ytF4RwiWZJ7zMJaf\n" +
//                "uteqhYVRC4UwiYbt24Jh4TPcQrddPciG/I/BJQnV9wARAQAB/gkDCKySAj4y8H1u\n" +
//                "YAHKd2Wl29H8xAXWWy/ejjaIM/1eZkKsmpMq9mQhyokF20hj7cC30v6wuwIR0s94\n" +
//                "vof/eMgJMIh/hNPvpY+MQ789KJEpxQRl71s4qDK0ITJq+SttrOHwGV4FMiZE7PpU\n" +
//                "FhKPSU1RcZPT2mzF5+aZS5pcGVBTFWZuj/kJ94yUPh4LXFkOg6MQHOfuNqv6bdVK\n" +
//                "/+fFvR/a6vRIoT6vPHoUO/4e4PN2QutirzhbFKU513XbC3WHV2Q8yC+5rsTRmS9B\n" +
//                "p4cRb8p5OGrHPOy8HFbuB0s67u6MLMzFQFjSIKNWhsVevr+Ie8eG2lhG6aD02b7i\n" +
//                "xJjF8rPfiP2VSJREywmH1xLd0v2G2dOUdf7QlRTguhG29TYDouAjAhZUBU9hG0rT\n" +
//                "3aFdxApSkvyufDpuUsib5Lw+dqQg28Hc/4yyfwGeJNtNwncASl965JZanxGpqyGG\n" +
//                "vPhXaS77U6AAnyh+YUhqx3pJlabwpWYfbxEvv9fCwIMEGAEKAA8FAl8cr9AFCQ8J\n" +
//                "nAACGy4AqAkQRIlwUZR8CgydIAQZAQoABgUCXxyv0AAKCRBCpbKEVzatMkPYBACw\n" +
//                "9SCn+cIX61zydvH7WtzYmTEfs/vvwLOA7UqmTnlmFB0Y1cLYiQG8Z2Q+m+h5sUh4\n" +
//                "7yejB1A/9vG/O06hb2s2Y9QzvaJ1iU+L3HkNKFJ6VaKqfGYZ/CvHHL8i2auDhkXs\n" +
//                "DRgAk3u0d/PILjk3rmaT27vm8gsblrjKe736iJej4mq6A/9IIMVn6VmdvxOiNKvn\n" +
//                "rjUtxUX+R/tBny6LRAczcHejIQn+q3u2vZXgAJNIgFM7EyNMPd6esZEVt3nN59Lj\n" +
//                "H9RshpB7UMsBbFtHb/UTOTumbPUxSYnzLPbM7o0ivSmV8eatk6YvneFTa8svBPrU\n" +
//                "l+EtK+zqedJ4iuTolH1XLZCrJA==\n" +
//                "=o8/v\n" +
//                "-----END PGP PRIVATE KEY BLOCK-----\n";
//
//        String pub = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
//                "Version: Keybase OpenPGP v1.0.0\n" +
//                "Comment: https://keybase.io/crypto\n" +
//                "\n" +
//                "xo0EXxyv0AEEAPUXPFl09V5SBHaT5x1yq2a6hZ/HC1BNnI1jo1nMTEOcN/7b7pz2\n" +
//                "fH9FGL2Teg8WTimFn4kpKcS/FpZrY95wDK64p+8IyTCm0I2pZU56/B5XNsXXVUsd\n" +
//                "9RErlLyu+/Dt62eHiUpRwR2zQjn62VsQVJXd/TNN9GlcjCUaBzxj3mErABEBAAHN\n" +
//                "HEhITCBCYW5rIDxoaGxiYW5rQGdtYWlsLmNvbT7CrQQTAQoAFwUCXxyv0AIbLwML\n" +
//                "CQcDFQoIAh4BAheAAAoJEESJcFGUfAoM9L0D/jAr1uH3biCqdDwzcLSgxMQEoyTC\n" +
//                "8Bbgp2MD32928sWW1eX3IL7tVbLo5oH9WRub+CPCOpjXHMu5iMeqmVz5om0kEF2G\n" +
//                "7+1Sg+amF/a+pdCb9hBGL/bvqQiiLGzjdJOuDiw+Cqt7887J6X87zfyOJ+TWRQqR\n" +
//                "gRE1mfVp4a9rgVwozo0EXxyv0AEEAMmzM/nzYfB9M+IyRppmMac1ecOuZ1XFxU7A\n" +
//                "cywITocFi+/5kCKMXzD5OdnOkFk6kDEHacioXMS/wIqAZoZYU6OBP+ngAzREEtDf\n" +
//                "TNbTOWro2iLzYGVk92CJsF1xkedr7qY6THWEibUYOvW4nZvNV67GhBNHpKGt35kx\n" +
//                "FdtKhiaHABEBAAHCwIMEGAEKAA8FAl8cr9AFCQ8JnAACGy4AqAkQRIlwUZR8Cgyd\n" +
//                "IAQZAQoABgUCXxyv0AAKCRBVVH0rleQ2v012BAC4hLOUQr6RrKsBfn9TUwNIqL2B\n" +
//                "uBSIVrwCFdo7sX1rI4blgM0ilpVt4GIU4ZqvpIYaBDp+tXdgbRHs0wEtvaRZNnF3\n" +
//                "++kqJ5ye28TVQy8qAovHEFiv2VTfTFWn57hsNtQ7DGiFuCUPR2LgnYVN4mgNwLGz\n" +
//                "5oonvHWgmxIVAR1JGfP8A/9MGZayrGSjesqyxK47b77MSp3pTDlRxETSokAWqcmG\n" +
//                "sd2PMNxiLn+hIs8V3Xu5m1mgc0EK6QGImWrSPZNT14rB4UuZcPKjBAO+9LSZpbL+\n" +
//                "JHrTpR0Px0cdqqSTOTJqrE5hfVEHLcrPAwExBfW4NoeVcPrshDbxveI0QCyQhc62\n" +
//                "w86NBF8cr9ABBADbmOv/sIUY5Vk05CZ+g0cJL7XaCOjsHf26/w6l0f0WoNjuA8aD\n" +
//                "u91S1YlIJN7sEo2FrfuHTy9cUKZUAVjHjvOSuU/d6MKK8swNWKGg5u+evfXBS7J5\n" +
//                "ytF4RwiWZJ7zMJafuteqhYVRC4UwiYbt24Jh4TPcQrddPciG/I/BJQnV9wARAQAB\n" +
//                "wsCDBBgBCgAPBQJfHK/QBQkPCZwAAhsuAKgJEESJcFGUfAoMnSAEGQEKAAYFAl8c\n" +
//                "r9AACgkQQqWyhFc2rTJD2AQAsPUgp/nCF+tc8nbx+1rc2JkxH7P778CzgO1Kpk55\n" +
//                "ZhQdGNXC2IkBvGdkPpvoebFIeO8nowdQP/bxvztOoW9rNmPUM72idYlPi9x5DShS\n" +
//                "elWiqnxmGfwrxxy/Itmrg4ZF7A0YAJN7tHfzyC45N65mk9u75vILG5a4ynu9+oiX\n" +
//                "o+JqugP/SCDFZ+lZnb8TojSr5641LcVF/kf7QZ8ui0QHM3B3oyEJ/qt7tr2V4ACT\n" +
//                "SIBTOxMjTD3enrGRFbd5zefS4x/UbIaQe1DLAWxbR2/1Ezk7pmz1MUmJ8yz2zO6N\n" +
//                "Ir0plfHmrZOmL53hU2vLLwT61JfhLSvs6nnSeIrk6JR9Vy2QqyQ=\n" +
//                "=juyG\n" +
//                "-----END PGP PUBLIC KEY BLOCK-----\n";
//
//        TransferRequest request = new TransferRequest();
//        request.setBankCode("MY_BANK");
//        request.setCardName("LLH card");
//        request.setDescription("test transfer");
//        request.setRequestId("request 1");
//        request.setFrom(99999999999L);
//        request.setIsTransfer(true);
//        request.setPartnerCode("HHL_Bank");
//        request.setRequestTime(1595152797916L);
//        request.setTo(1915954019734406L);
//        request.setTypeFee(1);
//        request.setValue(300000);
//        request.setHash("0665385988a192c12beeeaa1544958fdef4dc9b9f2d63c9cc47ce11ea04b8807");
//
//        PGPPublicKey pgpPublicKey = PartnerProcess.readPublicKey(pub);
//        PGPSecretKey pgpSecretKey = PartnerProcess.readSecretKey(pri, pgpPublicKey.getKeyID());
//        String dataSig = new JsonObject()
//                .put("bankCode", request.getBankCode())
//                .put("cardName", request.getCardName())
//                .put("from", request.getFrom())
//                .put("isTransfer", request.getIsTransfer())
//                .put("partnerCode", request.getPartnerCode())
//                .put("requestId", request.getRequestId())
//                .put("requestTime", request.getRequestTime())
//                .put("to", request.getTo())
//                .put("typeFee", request.getTypeFee())
//                .put("value", request.getValue())
//                .put("hash", request.getHash())
//                .toString();
//        String sig = PartnerProcess.signaturePgp(dataSig, pgpSecretKey, "123456".toCharArray());
//    }
}
