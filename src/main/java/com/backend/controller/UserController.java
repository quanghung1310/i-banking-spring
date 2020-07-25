package com.backend.controller;

import com.backend.constants.ActionConstant;
import com.backend.constants.ErrorConstant;
import com.backend.dto.AccountPaymentDTO;
import com.backend.dto.ReminderDTO;
import com.backend.dto.UserDTO;
import com.backend.model.Account;
import com.backend.model.request.*;
import com.backend.model.response.BaseResponse;
import com.backend.model.response.DebtorResponse;
import com.backend.model.response.UserResponse;
import com.backend.process.UserProcess;
import com.backend.service.IAccountPaymentService;
import com.backend.repository.IReminderRepository;
import com.backend.service.IUserService;
import com.backend.util.DataUtil;
import com.backend.util.JwtUtil;
import com.google.gson.Gson;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Controller
public class UserController {
    private static final Logger logger = LogManager.getLogger(UserController.class);

    private static final Gson PARSER = new Gson();

    @Value( "${type.account.payment}" )
    private int paymentBank;

    @Value( "${my.bank.id}" )
    private long myBankId;

    @Value( "${fee.transfer}" )
    private int feeTransfer;

    @Value("${account.payment}")
    private String accountPayment;

    @Value("${account.saving}")
    private String accountSaving;

    private IUserService userService;
    private IReminderRepository reminderRepository;
    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;

    @Autowired
    public UserController(IUserService userService,
                          IReminderRepository reminderRepository,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.userService = userService;
        this.reminderRepository = reminderRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<String> generateToken(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword())
            );
        } catch (Exception ex) {
            return new ResponseEntity<>("BAD REQUEST DATA", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(new JsonObject().put("bearerToken", jwtUtil.generateToken(authRequest.getUserName())).toString(), HttpStatus.OK);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
            String password = request.getPassword();
            String userName = request.getUserName();
            if (StringUtils.isBlank(password) || StringUtils.isBlank(userName)) {
                logger.warn("{}| Validate request login data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = ((UserDetails)principal).getUsername();
            String pass     = ((UserDetails)principal).getPassword();

            if (!userName.equals(username) || !password.equals(pass)) {
                logger.warn("{}| userName or password wrong!", logId);
                return new ResponseEntity<>(DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null).toString(), HttpStatus.UNAUTHORIZED);
            }

            UserResponse userResponse = userService.getUser(logId, username);
            if (userResponse == null || userResponse.getId() <= 0) {
                logger.warn("{}| Login fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, userResponse.toString());
            response.setData(new JsonObject(userResponse.toString()));
            logger.info("{}| Response to client: {}", logId, userResponse.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request login catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
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
            UserResponse user = userService.getUser(logId, ((UserDetails)principal).getUsername());

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
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
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
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping({"/get-reminders/{type}/{cardNumber}", "/get-reminders/{type}"})
    public ResponseEntity<String> getReminders(@PathVariable int type,
                                               @PathVariable (required = false) Long cardNumber) {
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request data: type - {}, cardNumber - {}", logId, type, cardNumber);
        BaseResponse response;
        try {
            if (type <= 0 || type > 2) {
                logger.warn("{}| Validate request get reminders data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            UserResponse user = getUser(logId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());

            UserResponse userResponse = userService.getReminders(logId, user.getId(), type, cardNumber);
            return getUserResponseEntity(logId, userResponse);
        } catch (Exception ex) {
            logger.error("{}| Request get users catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/get-account-info/{cardNumber}/{merchantId}")
    public ResponseEntity<String> queryAccount(@PathVariable long cardNumber,
                                               @PathVariable long merchantId) {
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request data: cardNumber - {}, merchantId - {}", logId, cardNumber, merchantId);
        BaseResponse response;
        try {
            if (cardNumber <= 0 || merchantId < 0) {
                logger.warn("{}| Validate request query account data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            //account of my bank
            UserResponse userResponse = userService.queryAccount(logId, cardNumber, merchantId, paymentBank, false);
            return DataUtil.getStringResponseEntity(logId, userResponse);
        } catch (Exception ex) {
            logger.error("{}| Request query account catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
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
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
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
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/transaction")
    public ResponseEntity<String> createTransaction(@RequestBody TransactionRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
            if (!request.isValidData()) {
                logger.warn("{}| Validate request transaction data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request deposit success!", logId);

            UserResponse fromUser = userService.queryAccount(logId, request.getSenderCard(), myBankId, paymentBank, true);
            UserResponse toUser = userService.queryAccount(logId, request.getReceiverCard(), myBankId, paymentBank, true);
            Account senderAccount = fromUser.getAccount().get(0);
            Account receiverAccount = toUser.getAccount().get(0);
            long newSenderBalance;
            long newReceiverBalance;
            long senderBalance = senderAccount.balance;
            long receiverBalance = receiverAccount.balance;
            long senderId = senderAccount.id;
            long receiverId = receiverAccount.id;
            long balanceTransfer = request.getAmount();
            int senderFee = 0;
            int receiverFee = 0;

            if (request.getTypeFee() == 1) {
                senderFee = 2;
            }

            if (request.getTypeFee() == 2) {
                receiverFee = 2;
            }

            newSenderBalance = UserProcess.newBalance(true, senderFee, feeTransfer, balanceTransfer, senderBalance);
            if (newSenderBalance < 0) {
                logger.warn("{}| Current balance account - {} can't transfer!", logId, senderId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_REQUEST, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.BAD_REQUEST);
            }
            newReceiverBalance = UserProcess.newBalance(false, receiverFee, feeTransfer, balanceTransfer, receiverBalance);

            if (request.getTypeTrans() == 2) {
                //remove debt
            }

            //insert transaction
            long transId = userService.insertTransaction(logId, request);
            if (transId == -1) {
                logger.warn("{}| Create transaction: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Create transaction success with transId: {}!", logId, transId);

            //update payment
            AccountPaymentDTO senderAccountPaymentDTO = accountPaymentService.updateBalance(logId, senderId, newSenderBalance);
            AccountPaymentDTO receiverAccountPaymentDTO = accountPaymentService.updateBalance(logId, receiverId, newReceiverBalance);
            if (senderAccountPaymentDTO == null || receiverAccountPaymentDTO == null) {
                logger.warn("{}| Update new balance: fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, request.getRequestId(),null);
                return new ResponseEntity<>(
                        response.toString(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                String dataResponse = new JsonObject().put("transId", transId).toString();
                response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, dataResponse);
                logger.info("{}| Response to client: {}", logId, dataResponse);
                return new ResponseEntity<>(response.toString(), HttpStatus.OK);
            }
        } catch (Exception ex) {
            logger.error("{}| Request transaction catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
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
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
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
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
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
        return userService.getUser(logId, ((UserDetails)principal).getUsername());
    }

    @PostMapping(value = "/pay/debt")
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

//            TransactionResponse transactionResponse = userService.transaction(logId, request);
            long result = userService.payDebt(logId, request);
            //// TODO: 7/20/20 validate result -> response
            JsonObject dateResponse = new JsonObject()
                    .put("transId", result);
            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, request.getRequestId(), dateResponse.toString());
            response.setData(new JsonObject(dateResponse.toString()));
            logger.info("{}| Response to client: {}", logId, response.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request pay debt catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }
}
