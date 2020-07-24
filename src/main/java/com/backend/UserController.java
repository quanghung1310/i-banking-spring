package com.backend;

import com.backend.constants.ActionConstant;
import com.backend.constants.ErrorConstant;
import com.backend.dto.ReminderDTO;
import com.backend.model.Account;
import com.backend.model.request.*;
import com.backend.model.response.BaseResponse;
import com.backend.model.response.DebtorResponse;
import com.backend.model.response.TransactionResponse;
import com.backend.model.response.UserResponse;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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

    @Value("${account.payment}")
    private String accountPayment;

    @Value("${account.saving}")
    private String accountSaving;

    IUserService userService;
    IReminderRepository reminderRepository;
    UserDetailsService userDetailsService;
    AuthenticationManager authenticationManager;
    JwtUtil jwtUtil;

    @Autowired
    public UserController(IUserService userService,
                          IReminderRepository reminderRepository,
                          @Qualifier("USER_DETAIL") UserDetailsService userDetailsService,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.userService = userService;
        this.reminderRepository = reminderRepository;
        this.userDetailsService = userDetailsService;
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

    @GetMapping({"/get-accounts/{type}", "/get-accounts"})
    public ResponseEntity<String> getCustomers(@PathVariable(value = "type", required = false) String type) {
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
                logger.warn("{}| Validate request create reminder data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request create reminder success!", logId);

            ReminderDTO reminderDTO = userService.createReminder(logId, request);
            JsonObject reminder = new JsonObject();
            if (reminderDTO == null) {
                logger.warn("{}| Create reminder fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_REQUEST, request.getRequestId(), reminder.toString());
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            } else {
                UserResponse userResponse = userService.getReminders(logId, reminderDTO.getUserId(), reminderDTO.getType(), reminderDTO.getCardNumber());
                return getUserResponseEntity(logId, userResponse);
            }
//            reminder.put("reminderId", reminderId);
//            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, request.getRequestId(), reminder.toString());
//            response.setData(new JsonObject(reminder.toString()));
//            logger.info("{}| Response to client: {}", logId, response.toString());
//            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request create reminder catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping({"/get-reminders/{userId}/{type}/{cardNumber}", "/get-reminders/{userId}/{type}"})
    public ResponseEntity<String> getReminders(@PathVariable long userId,
                                               @PathVariable int type,
                                               @PathVariable (name = "cardNumber", required = false) Long cardNumber) {
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request data: userId - {}, type - {}, cardNumber - {}", logId, userId, type, cardNumber);
        BaseResponse response;
        try {
            if (type <= 0 || type > 2 || userId < 0) {
                logger.warn("{}| Validate request get reminders data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            UserResponse userResponse = userService.getReminders(logId, userId, type, cardNumber);
            return getUserResponseEntity(logId, userResponse);
        } catch (Exception ex) {
            logger.error("{}| Request get users catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/get-account/{cardNumber}/{merchantId}")
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
            if (userResponse == null) {
                logger.warn("{}| query account fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, userResponse.toString());
                return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, userResponse.toString());
            response.setData(new JsonObject(userResponse.toString()));
            logger.info("{}| Response to client: {}", logId, userResponse.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
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

            long debtId = userService.createDebtor(logId, request);
            JsonObject reminder = new JsonObject();
            if (debtId == -2) {
                logger.warn("{}| Create debtor fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, request.getRequestId(), reminder.toString());
                return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if (debtId <= 0) {
                logger.warn("{}| Create debtor fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_REQUEST, request.getRequestId(), reminder.toString());
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            reminder.put("debtId", debtId);
            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, request.getRequestId(), reminder.toString());
            response.setData(new JsonObject(reminder.toString()));
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

    @GetMapping("/get-debts/{userId}/{action}/{type}")
    public ResponseEntity<String> getDebt(@PathVariable long userId,
                                               @PathVariable int action,
                                               @PathVariable int type) {
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request data: userId - {}, type - {}", logId, userId, type);
        BaseResponse response;
        try {
            if (type <= 0 || type > 2 || userId < 0 || action < 0) {
                logger.warn("{}| Validate request get debts data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            DebtorResponse debtorResponse = userService.getDebts(logId, userId, action, type);
            if (debtorResponse == null) {
                logger.warn("{}| Get debts fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, debtorResponse.toString());
                return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, debtorResponse.toString());
            response.setData(new JsonObject(debtorResponse.toString()));
            logger.info("{}| Response to client: {}", logId, debtorResponse.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request get users catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
            ResponseEntity<String> responseEntity = new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping(value = "/transaction")
    public ResponseEntity<String> createTransaction(@RequestBody TransactionRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
            if (!request.isValidData()) {
                logger.warn("{}| Validate request deposit data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request deposit success!", logId);

            TransactionResponse transactionResponse = userService.transaction(logId, request);

            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, request.getRequestId(), transactionResponse.toString());
            response.setData(new JsonObject(transactionResponse.toString()));
            logger.info("{}| Response to client: {}", logId, response.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request transaction catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
            ResponseEntity<String> responseEntity = new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
            return responseEntity;
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

            long result = userService.deleteDebt(logId, request);
            if (result == -1) {
                logger.warn("{}| Data delete not found: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            } else if (result == -2) {
                logger.warn("{}| Delete database: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                response = DataUtil.buildResponse(ErrorConstant.SUCCESS, request.getRequestId(), new JsonObject().put("debtId", result)
                                                                                                                    .put("action", ActionConstant.DELETE).toString());
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
}
