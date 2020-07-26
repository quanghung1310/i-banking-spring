package com.backend.controller;

import com.backend.constants.ErrorConstant;
import com.backend.model.request.AuthRequest;
import com.backend.model.response.BaseResponse;
import com.backend.model.response.TransactionsResponse;
import com.backend.model.response.UserResponse;
import com.backend.service.IAccountPaymentService;
import com.backend.service.ITransactionService;
import com.backend.service.IUserService;
import com.backend.util.DataUtil;
import com.backend.util.JwtUtil;
import io.vertx.core.json.JsonObject;
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

@Controller
public class IndexController {
    private static final Logger logger = LogManager.getLogger(UserController.class);

    @Value("${role.customer}")
    private String CUSTOMER;

    @Value("${role.employer}")
    private String EMPLOYER;

    @Value("${role.admin}")
    private String ADMIN;

    private IUserService userService;
    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
    private IAccountPaymentService accountPaymentService;
    private ITransactionService transactionService;

    @Autowired
    public IndexController(IUserService userService,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           IAccountPaymentService accountPaymentService,
                           ITransactionService transactionService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.accountPaymentService = accountPaymentService;
        this.transactionService = transactionService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<String> generateToken(@RequestBody AuthRequest authRequest) {
        String logId = DataUtil.createRequestId();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword())
            );
        } catch (Exception ex) {
            return new ResponseEntity<>("BAD REQUEST DATA", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        UserResponse user = userService.getUser(logId, authRequest.getUserName());
        return new ResponseEntity<>(new JsonObject()
                .put("bearerToken", jwtUtil.generateToken(authRequest.getUserName()))
                .put("role", user.getRole())
                .toString(), HttpStatus.OK);
    }

    @GetMapping(value = "/get-profile")
    public ResponseEntity<String> getProfile() {
        String logId = DataUtil.createRequestId();
        BaseResponse response;
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = ((UserDetails)principal).getUsername();
            UserResponse userResponse = userService.getUser(logId, username);
            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, userResponse.toString());
            response.setData(new JsonObject(userResponse.toString()));
            logger.info("{}| Response to client: {}", logId, userResponse.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request get profile catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = {"/get-transactions/{typeTrans}", "/get-transactions/{typeTrans}/{cardNumber}"})
    public ResponseEntity<String> getTransactions(@PathVariable(required = false) Long cardNumber,
                                                  @PathVariable String typeTrans) {
        //typeTrans: "send": Trans chuyển tiền, "receiver": Trans nhận tiền, "debt": Trans thanh toán nhắc nợ, "all": Lấy hết
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request data: cardNumber - {}", logId, cardNumber);
        BaseResponse response;
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponse user = getUser(logId, principal);
            if (user.getRole().equals(CUSTOMER)) {
                cardNumber = accountPaymentService.getAccountByUserId(user.getId()).getCardNumber();
            } else if (user.getRole().equals(EMPLOYER)) {
                if (cardNumber == null) {
                    logger.warn("{}| Card number not null!", logId);
                    response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                    return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
                }
            } else {

            }
            TransactionsResponse transactionsResponse = transactionService.getTransactions(logId, cardNumber, typeTrans);
            if (transactionsResponse == null) {
                logger.warn("{}| Card number - {} not found transactions!", logId, cardNumber);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, transactionsResponse.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request get transactions catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }
    private UserResponse getUser(String logId, Object principal) {
        return userService.getUser(logId, ((UserDetails)principal).getUsername());
    }

}
