package com.backend.controller;

import com.backend.config.PartnerConfig;
import com.backend.constants.ActionConstant;
import com.backend.constants.ErrorConstant;
import com.backend.constants.StringConstant;
import com.backend.dto.*;
import com.backend.model.Account;
import com.backend.model.Partner;
import com.backend.model.Transaction;
import com.backend.model.request.PasswordRequest;
import com.backend.model.request.debt.CreateDebtorRequest;
import com.backend.model.request.debt.DeleteDebtRequest;
import com.backend.model.request.debt.PayDebtRequest;
import com.backend.model.request.employee.RegisterRequest;
import com.backend.model.request.reminder.CreateReminderRequest;
import com.backend.model.request.transaction.TransactionRequest;
import com.backend.model.response.BaseResponse;
import com.backend.model.response.DebtorResponse;
import com.backend.model.response.TransactionResponse;
import com.backend.model.response.UserResponse;
import com.backend.process.PartnerProcess;
import com.backend.process.TransactionProcess;
import com.backend.repository.IReminderRepository;
import com.backend.service.*;
import com.backend.util.DataUtil;
import com.google.gson.Gson;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Controller
public class UserController {
    private static final Logger logger = LogManager.getLogger(UserController.class);

    private static final Gson PARSER = new Gson();

    @Value("${type.account.payment}")
    private int paymentBank;

    @Value("${my.bank.id}")
    private long myBankId;

    @Value("${fee.transfer}")
    private int feeTransfer;

    @Value("${account.payment}")
    private String accountPayment;

    @Value("${account.saving}")
    private String accountSaving;

    @Value("${otp.payment}")
    private String otpPayment;

    @Value("${otp.debt}")
    private String otpDebt;

    @Value("${session.request}")
    private int session;

    private IUserService userService;
    private IReminderRepository reminderRepository;
    private IAccountPaymentService accountPaymentService;
    private JavaMailSender javaMailSender;
    private IOtpService otpService;
    private ITransactionService transactionService;
    private IPartnerService partnerService;
    private INotifyService notifyService;

    @Autowired
    public UserController(IUserService userService,
                          IReminderRepository reminderRepository,
                          IAccountPaymentService accountPaymentService,
                          JavaMailSender javaMailSender,
                          IOtpService otpService,
                          ITransactionService transactionService,
                          IPartnerService partnerService,
                          INotifyService notifyService) {
        this.userService = userService;
        this.reminderRepository = reminderRepository;
        this.accountPaymentService = accountPaymentService;
        this.javaMailSender = javaMailSender;
        this.otpService = otpService;
        this.transactionService = transactionService;
        this.partnerService = partnerService;
        this.notifyService = notifyService;
    }

    @GetMapping(value = {"/get-accounts", "/get-accounts/{type}"})
    public ResponseEntity<String> getCustomers(@PathVariable(name = "type", required = false) String type) {
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request data: type - {}", logId, type);
        BaseResponse response;
        try {
            int typeAccount = 0;
            if (StringUtils.isNotBlank(type)) {
                if (type.equals(accountPayment)) {
                    typeAccount = 1;
                } else if (type.equals(accountSaving)) {
                    typeAccount = 2;
                } else {
                    logger.warn("{}| type not empty!", logId);
                    response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                    return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
                }
            }

            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponse user = userService.getUser(logId, ((UserDetails) principal).getUsername());

            List<Account> accounts = userService.getUsers(logId, typeAccount, user.getId());
            if (accounts == null) {
                logger.warn("{}| Get users fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            JsonObject responseData = new JsonObject().put("accounts", accounts);
            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, responseData.toString());
            response.setData(new JsonObject(responseData.toString()));
            logger.info("{}| Response to client: {}", logId, responseData.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request get users catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/create-reminder")
    public ResponseEntity<String> createReminder(@RequestBody CreateReminderRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
            if (!request.isValidData()) {
                logger.warn("{}| Validate base request data create reminder data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            if (request.getCardNumber() == null
                    || request.getCardNumber() <= 0
                    || request.getType() < 0 || request.getType() > 2
                    || request.getMerchantId() <= 0) {
                logger.warn("{}| Validate request create reminder data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request create reminder success!", logId);

            UserResponse user = getUser(logId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            ReminderDTO reminderDTO = userService.createReminder(logId, request, user.getId(), StringUtils.isNotBlank(request.getNameReminisce()) ? request.getNameReminisce() : user.getName());
            JsonObject reminder = new JsonObject();
            if (reminderDTO == null) {
                logger.warn("{}| Create reminder fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_REQUEST, request.getRequestId(), reminder.toString());
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            } else {
                UserResponse userResponse = userService.getReminders(logId, reminderDTO.getUserId(), reminderDTO.getType(), null);
                return getUserResponseEntity(logId, userResponse);
            }
        } catch (Exception ex) {
            logger.error("{}| Request create reminder catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping({"/get-reminders/{type}/{cardNumber}", "/get-reminders/{type}"})
    public ResponseEntity<String> getReminders(@PathVariable int type,
                                               @PathVariable(required = false) Long cardNumber) {
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request data: type - {}, cardNumber - {}", logId, type, cardNumber);
        BaseResponse response;
        try {
            //type: 1 - send, type: 2 - debt
            if (type < 0 || type > 2) {
                logger.warn("{}| Validate request get reminders data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            UserResponse user = getUser(logId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());

            UserResponse userResponse = userService.getReminders(logId, user.getId(), type, cardNumber);
            return getUserResponseEntity(logId, userResponse);
        } catch (Exception ex) {
            logger.error("{}| Request get users catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/create-debtor")
    public ResponseEntity<String> createDebtor(@RequestBody CreateDebtorRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
            if (!request.isValidData()) {
                logger.warn("{}| Validate request create debtor data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request create debtor success!", logId);

            UserResponse user = getUser(logId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());

            DebtorResponse debtor = userService.createDebtor(logId, request, user.getId());
            if (debtor == null) {
                logger.warn("{}| Create debtor fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, request.getRequestId(), debtor.toString());
                return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, request.getRequestId(), debtor.toString());
            response.setData(new JsonObject(debtor.toString()));
            logger.info("{}| Response to client: {}", logId, response.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request create debtor catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-debts/{action}/{type}")
    public ResponseEntity<String> getDebt(@PathVariable int action,
                                          @PathVariable int type) {
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request data: action - {}, type - {}", logId, action, type);
        BaseResponse response;
        try {
            if (type <= 0 || type > 2 || action < 0) {
                logger.warn("{}| Validate request get debts data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            UserResponse user = getUser(logId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());

            DebtorResponse debtorResponse = userService.getDebts(logId, user.getId(), action, type);
            if (debtorResponse == null) {
                logger.warn("{}| Get debts fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, debtorResponse.toString());
            response.setData(new JsonObject(debtorResponse.toString()));
            logger.info("{}| Response to client: {}", logId, debtorResponse.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request get users catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/transaction")
    public ResponseEntity<String> createTransaction(@RequestBody TransactionRequest request) {
        long transId = -1L;
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        UserResponse fromUser = getUser(logId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        AccountPaymentDTO accountSender = accountPaymentService.getAccountByUserId(fromUser.getId());

        long senderCard = accountSender.getCardNumber();
        try {
            if (!request.isValidData()) {
                logger.warn("{}| Validate request transaction data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request deposit success!", logId);

            //Validate balance
            long newSenderBalance;
            long senderBalance = accountSender.getBalance();//senderAccount.balance;
            long senderId = accountSender.getId(); //senderAccount.id
            long amountTransfer = request.getAmount();

            newSenderBalance = TransactionProcess.newBalance(true, request.getTypeFee() == 1, feeTransfer, amountTransfer, senderBalance);
            if (newSenderBalance < 0) {
                logger.warn("{}| Current balance account - {} can't transfer!", logId, senderId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_REQUEST, request.getRequestId(), null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.BAD_REQUEST);
            }

            if (request.getMerchantId() == myBankId) {
                //todo Validate account to ?
                AccountPaymentDTO accountReceiver = accountPaymentService.findByCardNumber(request.getReceiverCard());
                if (accountReceiver == null) {
                    logger.warn("{}| Account receiver - {} not found!", logId, request.getReceiverCard());
                    response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, request.getRequestId(), null);
                    return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
                }
                //insert transaction
                request.setReceiverName(accountReceiver.getCardName());
                transId = transactionService.insertTransaction(logId, request, senderCard);
            } else {
                //Lien ngan hang
                int merchantId = Math.toIntExact(request.getMerchantId());
                Partner partner = partnerService.findById(merchantId);
                if (partner == null) {
                    logger.warn("{}| Partner with bank id - {} not found!", logId, merchantId);
                    response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, request.getRequestId(), null);
                    return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
                }

                String mid = String.valueOf(merchantId);
                String alg = PartnerConfig.getAlg(mid);
                String url = PartnerConfig.getUrlTransfer(mid);
                String partnerPub = PartnerConfig.getPartnerPubKey(mid);

                if (alg.equals("RSA")) {
                    //RSA
                    //2.1: Build json body
                    String publicKey = PartnerConfig.getPublicKey(mid);
                    String privateKey = PartnerConfig.getPrivateKey(mid);
                    long currentTime = System.currentTimeMillis();
                    String partnerCode = PartnerConfig.getPartnerCode(mid);

                    JsonObject requestBody = new JsonObject()
                            .put("from", String.valueOf(senderCard))
                            .put("description", request.getContent())
                            .put("to", String.valueOf(request.getReceiverCard()))
                            .put("amount", request.getAmount())
                            .put("type", 1); ////1 là bên gửi chịu phí, 2 là bên

                    String dataCrypto = requestBody
                            + PartnerConfig.getPartnerSecretKey(mid)
                            + System.currentTimeMillis()
                            + partnerCode
                            + "base64";
                    String hash = DataUtil.signHmacSHA256(dataCrypto, partnerPub);

//                    String signature = DataUtil.sig
                    if (StringUtils.isBlank(hash)) {
                        logger.warn("{}| Hash data fail!", logId);
                        return null;
                    }

                    RestTemplate restTemplate = new RestTemplate();

                    // HttpHeaders
                    HttpHeaders headers = new HttpHeaders();

                    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    // Request to return JSON format
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set("x-partner-code", partnerCode);
                    headers.set("x-timestamp", String.valueOf(currentTime));
                    headers.set("x-data-encrypted", hash);

                    HttpEntity<JsonObject> entity = new HttpEntity<>(requestBody, headers);
                    ResponseEntity<JsonObject> resp = restTemplate.postForEntity(url, entity, JsonObject.class);
                    /// TODO: 7/26/2020
                    return null;
                } else if (alg.equals("PGP")) {
                    //Sig
                    String payload = "";
                    //hash
                    String signature = "";
                    //Create request data
                    JsonObject data = new JsonObject()
                            .put("account_number", request.getReceiverCard())
                            .put("value", amountTransfer);
                    JsonObject requestBody = PartnerProcess.getDataPartner(logId, data, mid);

                    if (requestBody == null) {
                        logger.warn("{}| Build request body fail!", logId);
                        requestBody = new JsonObject()
                                .put("payload", payload)
                                .put("signature", signature);
                    }
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    RestTemplate restTemplate = new RestTemplate();
                    HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

//                    ResponseEntity<String> responseEntity = restTemplate.exchange(url,
//                            HttpMethod.POST,
//                            entity,
//                            String.class);
//                    logger.info("{}| Response from associate: {}", logId, responseEntity.getBody());

//                    if (responseEntity.getStatusCode() != HttpStatus.OK || responseEntity.getBody() == null) {
//                        logger.warn("{}| Call transfer PGP Bank API fail!", logId);
//                        response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, request.getRequestId(), null);
//                        return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
//                    }
                    //insert transaction
                    transId = transactionService.insertTransaction(logId, request, senderCard);
                } else {
                    logger.warn("{}| alg - {} of merchant id - {} not existed!", logId, alg, merchantId);
                    return null;
                }
            }
            if (transId == -1) {
                logger.warn("{}| Create transaction: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Create transaction success with transId: {}!", logId, transId);
            String dataResponse = new JsonObject().put("transId", transId).toString();
            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, dataResponse);
            logger.info("{}| Response to client: {}", logId, dataResponse);
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request transaction catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/delete-debt")
    public ResponseEntity<String> deleteDebt(@RequestBody DeleteDebtRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
            if (!request.isValidData()) {
                logger.warn("{}| Validate request delete debt data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request delete debt success!", logId);

            UserResponse user = getUser(logId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            DebtorResponse result = userService.deleteDebt(logId, request, user.getId());
            if (result == null) {
                logger.warn("{}| Data delete not found: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                response = DataUtil.buildResponse(ErrorConstant.SUCCESS, request.getRequestId(), result.toString());
                logger.info("{}| Response to client: {}", logId, response.toString());
                return new ResponseEntity<>(response.toString(), HttpStatus.OK);
            }
        } catch (Exception ex) {
            logger.error("{}| Request transaction catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/update-reminder")
    public ResponseEntity<String> updateReminder(@RequestBody CreateReminderRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
            //Step 1: Validate base request
            if (!request.isValidData()) {
                logger.warn("{}| Validate request update/delete reminder data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            //Step 2: Validate request update/delete
            if (request.getReminderId() == null
                    || (!request.getAction().equals(ActionConstant.DELETE.name()) && !request.getAction().equals(ActionConstant.UPDATE.name()))) {
                logger.warn("{}| Reminder id - {} or action - {} are not empty!", logId, request.getReminderId(), request.getAction());
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid base data request update reminder success!", logId);

            //Step 2: Validate reminder
            Optional<ReminderDTO> reminderDTO = reminderRepository.findById(request.getReminderId());
            if (!reminderDTO.isPresent()) {
                logger.warn("{}| Reminder - {} not found!", logId, request.getReminderId());
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid request update reminder success!", logId);

            if (request.getAction().equals(ActionConstant.UPDATE.name())) {
                if (StringUtils.isBlank(request.getNameReminisce()) && (request.getCardNumber() == null)) {
                    logger.warn("{}| Data update bad format: name - {}, cardNumber - {}, reminderId - {}", logId, request.getNameReminisce(), request.getCardNumber(), request.getReminderId());
                    response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                    return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
                }
                reminderDTO.get().setCardNumber(request.getCardNumber());
                reminderDTO.get().setNameReminisce(request.getNameReminisce());
            } else {
                reminderDTO.get().setIsActive(0);
            }
            ReminderDTO reminder = reminderRepository.save(reminderDTO.get());
            UserResponse userResponse = userService.getReminders(logId, reminder.getUserId(), reminder.getType(), reminder.getCardNumber());
            return getUserResponseEntity(logId, userResponse);
        } catch (Exception ex) {
            logger.error("{}| Request create reminder catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<String> getUserResponseEntity(String logId, UserResponse userResponse) {
        BaseResponse response;
        if (userResponse == null) {
            logger.warn("{}| Get reminders fail!", logId);
            response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, null);
            return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, userResponse.toString());
        response.setData(new JsonObject(userResponse.toString()));
        logger.info("{}| Response to client: {}", logId, userResponse.toString());
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    private UserResponse getUser(String logId, Object principal) {
        return userService.getUser(logId, ((UserDetails) principal).getUsername());
    }

    @PostMapping(value = "/pay-debt")
    public ResponseEntity<String> payDebt(@RequestBody PayDebtRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request pay debt data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
            if (!request.isValidData()) {
                logger.warn("{}| Validate request pay debt data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request pay debt success!", logId);

            UserResponse user = getUser(logId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            TransactionResponse result = userService.payDebt(logId, request, user.getId());
            if (result == null) {
                logger.warn("{}| Pay debt - {}: fail!", logId, request.getDebtId());
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, request.getRequestId(), result.toString());
            logger.info("{}| Response to client: {}", logId, response.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request pay debt catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/send-otp/{action}/{transId}")
    public ResponseEntity<String> sendOtp(@PathVariable String action,
                                          @PathVariable long transId) {
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request data: action - {}", logId, action);
        int otp = new Random().nextInt(900000);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        BaseResponse response;
        try {
            if (!action.equals(otpPayment) && !action.equals(otpDebt)) {
                logger.warn("{}| Action - {} not existed!", logId, action);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            Transaction transactionDTO = transactionService.getByTransIdAndType(transId, action.equals(otpPayment) ? 1 : 2, ActionConstant.INIT.name());
            if (transactionDTO == null) {
                logger.warn("{}| Transaction - {} not existed!", logId, transId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            SimpleMailMessage msg = new SimpleMailMessage();
            UserResponse user = getUser(logId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            msg.setTo(user.getEmail());
            msg.setSubject("Your one-time passcode to view the message");
            msg.setText("Here is your one-time passcode\n" + otp);

            OtpDTO otpDTO = new OtpDTO();
            otpDTO.setCreatedAt(currentTime);
            otpDTO.setOtp(otp);
            otpDTO.setStatus(ActionConstant.INIT.getValue());
            otpDTO.setUpdatedAt(currentTime);
            otpDTO.setAction(action);
            otpDTO.setUserId(user.getId());
            otpDTO.setTransId(transId);
            javaMailSender.send(msg);

            OtpDTO otpDto = otpService.saveOtp(otpDTO);

            if (otpDto == null) {
                logger.warn("{}| Save otp - {} fail!", logId, otp);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            JsonObject result = new JsonObject().put("otp", otp)
                    .put("id", otpDto.getId())
                    .put("createDate", DataUtil.convertTimeWithFormat(otpDto.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                    .put("transId", transId);
            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, result.toString());
            logger.info("{}| Response to client: {}", logId, response.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request pay debt catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<String> validateOtp(@RequestBody String requestBody) {
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request validate otp data: {}", logId, requestBody);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        BaseResponse response;
        try {
            JsonObject request = new JsonObject(requestBody);
            String action = request.getString("action", "");
            long transId = request.getLong("transId", 0L);

            int otp = request.getInteger("otp", 0);
            if ((!action.equals(otpPayment) && !action.equals(otpDebt) && otp == 0) || StringUtils.isBlank(action)) {
                logger.warn("{}| Validate base request data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            TransactionDTO transactionDTO = transactionService.getByTransIdAndTypeAndAction(transId, action.equals(otpPayment) ? 1 : 2, ActionConstant.INIT.name());
            if (transactionDTO.getId() <= 0) {
                logger.warn("{}| Transaction - {} not existed!", logId, transId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            //Step 1: validate otp
            UserResponse user = getUser(logId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            boolean validateOtp = otpService.validateOtp(logId, user.getId(), otp, action, session, currentTime, transId);
            JsonObject result = new JsonObject().put("result", validateOtp);
            if (!validateOtp) {
                logger.warn("{}| Validate otp - {} fail!", logId, otp);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, result.toString());
                return new ResponseEntity<>(response.toString(), HttpStatus.OK);
            }

            //Step 2: Update balance
            long amountTransfer = transactionDTO.getAmount();
            AccountPaymentDTO accountSender = accountPaymentService.getAccountByUserId(user.getId());
            AccountPaymentDTO accountReceiver;
            long receiverBalance = 0;
            long senderCard = transactionDTO.getSenderCard() == accountSender.getCardNumber() ? transactionDTO.getReceiverCard() : transactionDTO.getSenderCard();

            int merchantId = transactionDTO.getMerchantId();
            if (merchantId == myBankId) { //Cung ngan hang -> cong tien cho receiver
                accountReceiver = accountPaymentService.findByCardNumber(transactionDTO.getReceiverCard());
                receiverBalance = accountReceiver.getBalance();
                //2.1 Update receiver
                long newReceiverBalance = TransactionProcess.newBalance(false, transactionDTO.getTypeFee() == 2, feeTransfer, amountTransfer, receiverBalance);

                //update payment
                AccountPaymentDTO receiverAccountPaymentDTO = accountPaymentService.updateBalance(logId, accountReceiver.getId(), newReceiverBalance);

                if (receiverAccountPaymentDTO == null) {
                    logger.warn("{}| Update new balance: fail!", logId);
                    response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, null);
                    return new ResponseEntity<>(
                            response.toString(),
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else { //khac ngan hang -> goi api cho partner
                Partner partner = partnerService.findById(merchantId);
                if (partner == null) {
                    logger.warn("{}| Partner with bank id - {} not found!", logId, merchantId);
                    response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, logId, null);
                    return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
                }

                String mid = String.valueOf(merchantId);
                String alg = PartnerConfig.getAlg(mid);
                String url = PartnerConfig.getUrlTransfer(mid);

                if (alg.equals("RSA")) {
                    //todo : call api
                } else if (alg.equals("PGP")) {
                    //Sig
                    String payload = "";
                    //hash
                    String signature = "";
                    //Create request data
                    JsonObject data = new JsonObject()
                            .put("account_number", senderCard)
                            .put("value", amountTransfer);
                    JsonObject requestData = PartnerProcess.getDataPartner(logId, data, mid);

                    if (requestData == null) {
                        logger.warn("{}| Build request body fail!", logId);
                        requestData = new JsonObject()
                                .put("payload", payload)
                                .put("signature", signature);
                    }
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    RestTemplate restTemplate = new RestTemplate();
                    HttpEntity<String> entity = new HttpEntity<>(requestData.toString(), headers);

                    ResponseEntity<String> responseEntity = restTemplate.exchange(url,
                            HttpMethod.POST,
                            entity,
                            String.class);
                    logger.info("{}| Response from associate transfer: {}", logId, responseEntity.getBody());

                    if (responseEntity.getStatusCode() != HttpStatus.OK || responseEntity.getBody() == null) {
                        logger.warn("{}| Call transfer PGP Bank API fail!", logId);
                        response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, null);
                        return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                } else {
                    logger.warn("{}| alg - {} of merchant id - {} not existed!", logId, alg, merchantId);
                    return null;
                }
            }
            //2.2 Update sender
            long newSenderBalance = TransactionProcess.newBalance(true, transactionDTO.getTypeFee() == 1, feeTransfer, amountTransfer, accountSender.getBalance());

            if (newSenderBalance < 0) {
                logger.warn("{}| Current balance account - {} can't transfer!", logId, accountSender.getId());
                response = DataUtil.buildResponse(ErrorConstant.BAD_REQUEST, logId, null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.BAD_REQUEST);
            }
            AccountPaymentDTO senderAccountPaymentDTO = accountPaymentService.updateBalance(logId, accountSender.getId(), newSenderBalance);

            if (senderAccountPaymentDTO == null) {
                logger.warn("{}| Update new balance: fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            //Step 3: Update transaction success
            transactionDTO.setStatus(ActionConstant.COMPLETED.name());
            Transaction transaction = transactionService.saveTransaction(transactionDTO);

            if (!transaction.getStatus().equals( ActionConstant.COMPLETED.name())) {
                logger.warn("{}| Update transaction - {} with status - {}: fail!", logId, transactionDTO.getTransId(), ActionConstant.COMPLETED.name());
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, new JsonObject().put("result", false).toString());
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, result.toString());
            logger.info("{}| Response to client: {}", logId, response.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request transaction catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/create-account-saving")
    public ResponseEntity<String> createAccountSaving(@RequestBody RegisterRequest request) {
        return null;
    }

    @PostMapping(value = "/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody PasswordRequest request) {
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request validate otp data: {}", logId, request);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        BaseResponse response;
        try {
            if (!request.isValidData()) {
                logger.warn("{}| Validate request pay debt data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            String newPass = request.getPassword();
            if (newPass.length() < 6 || newPass.length() > 20) {
                logger.warn("{}| Length new password need: [6 -> 20] character!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_PASSWORD, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            String userName = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

            String password = userService.updatePassword(logId, newPass, userName);

            if (StringUtils.isBlank(password)) {
                logger.warn("{}| Update password - {}: fail!", logId, newPass);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            String dataResponse = new JsonObject()
                    .put("userName", userName)
                    .put("newPassword", password).toString();

            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, dataResponse);
            logger.info("{}| Response to client: {}", logId, dataResponse);
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request pay debt catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = {"/forgot-password/{userName}", "/forgot-password/{userName}/{email}"})
    public ResponseEntity<String> forgotPassword(@PathVariable(name = "email", required = false) String email,
                                                 @PathVariable(name = "userName") String userName) {
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request data: email - {}", logId, email);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        BaseResponse response;
        try {
            UserResponse user = userService.getUser(logId, userName);

            if (user == null) {
                logger.warn("{}| User of user name - {}: not found!", logId, userName);
                response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            String userEmail = user.getEmail();

            JsonObject result = new JsonObject(userService.forgotPassword(logId, userName));

            SimpleMailMessage msg = new SimpleMailMessage();
            if (StringUtils.isNotBlank(email)) {
                userEmail = email;
            }
            msg.setTo(userEmail);
            msg.setSubject("Your new password to view the message");
            msg.setText("Here is your new password \n" + result.getString("password", ""));

            javaMailSender.send(msg);


            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, result.toString());
            logger.info("{}| Response to client: {}", logId, response.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request pay debt catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }
}
