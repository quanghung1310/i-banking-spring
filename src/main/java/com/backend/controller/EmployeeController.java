package com.backend.controller;

import com.backend.constants.ErrorConstant;
import com.backend.model.request.bank.DepositRequest;
import com.backend.model.request.employee.RegisterRequest;
import com.backend.model.response.BaseResponse;
import com.backend.model.response.DepositResponse;
import com.backend.model.response.RegisterResponse;
import com.backend.model.response.UserResponse;
import com.backend.service.IEmployeeService;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class EmployeeController {
    private static final Logger logger = LogManager.getLogger(EmployeeController.class);

    private static final Gson PARSER = new Gson();

    @Value( "${type.account.payment}" )
    private int paymentBank;

    private IUserService userService;
    private IEmployeeService employeeService;

    @Value("${role.employer}")
    private String EMPLOYER;

    @Autowired
    public EmployeeController(IEmployeeService employeeService,
                            IUserService userService) {
        this.userService = userService;
        this.employeeService = employeeService;
    }

    @PostMapping(value = "/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
            if (!request.isValidData()) {
                logger.warn("{}| Validate request register data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request register success!", logId);

            UserResponse user = getUser(logId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            if (!user.getRole().equals(EMPLOYER)) {
                logger.warn("{}| User - {} not authenticate!", logId, user.getId());
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.UNAUTHORIZED);
            }
            RegisterResponse registerResponse = employeeService.register(logId, request, user.getId());

            if (registerResponse == null || registerResponse.getAccount() == null) {
                logger.warn("{}| Register fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, request.getRequestId(), registerResponse.toString());
                return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, request.getRequestId(), registerResponse.toString());
            response.setData(new JsonObject(registerResponse.toString()));
            logger.info("{}| Response to client: {}", logId, response.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request register catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(),null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/deposit")
    public ResponseEntity<String> deposit(@RequestBody DepositRequest request) {
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

            DepositResponse depositResponse = employeeService.deposit(logId, request);

            if (depositResponse.getTotalBalance() == 0L) {
                logger.warn("{}| Deposit fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_REQUEST, request.getRequestId(), depositResponse.toString());
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, request.getRequestId(), depositResponse.toString());
            response.setData(new JsonObject(depositResponse.toString()));
            logger.info("{}| Response to client: {}", logId, response.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request deposit catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(),null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    private UserResponse getUser(String logId, Object principal) {
        return userService.getUser(logId, ((UserDetails)principal).getUsername());
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
}
