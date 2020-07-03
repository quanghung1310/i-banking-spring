package com.backend;

import com.backend.constants.ErrorConstant;
import com.backend.model.Account;
import com.backend.model.request.LoginRequest;
import com.backend.model.request.RegisterRequest;
import com.backend.model.response.BaseResponse;
import com.backend.model.response.UserResponse;
import com.backend.service.IUserService;
import com.backend.util.DataUtil;
import com.google.gson.Gson;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class UserController {
    private static final Logger logger = LogManager.getLogger(UserController.class);

    private static final Gson PARSER = new Gson();

    @Autowired
    IUserService userService;

    @GetMapping("/get-accounts/{userId}/{type}")
    public ResponseEntity<String> getCustomers(@PathVariable int userId,
            @PathVariable int type) {
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request data: {}", logId, type);
        BaseResponse response;
        try {
            if (type < 0 || type > 2 || userId < 0) {
                logger.warn("{}| Validate request get users data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            List<Account> accounts = userService.getUsers(logId, type, userId);
            if (accounts == null) {
                logger.warn("{}| Get users fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, accounts.toString());
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
            ResponseEntity<String> responseEntity = new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    //todo
//    @RequestMapping(value={"/login"})
//    public String loginSecurity(Principal principal) {
//        if (principal != null && ((Authentication) principal).isAuthenticated()) {
//            return "redirect:/";
//        }
//        return "login";
//    }

    //todo login tam thoi!!!!
    @PostMapping(value = "/login")
    public ResponseEntity<String> loginTemp(@RequestBody LoginRequest request) {
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

            UserResponse userResponse = userService.login(logId, userName, password);
            if (userResponse == null) {
                logger.warn("{}| Login fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, logId, userResponse.toString());
                return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, userResponse.toString());
            response.setData(new JsonObject(userResponse.toString()));
            logger.info("{}| Response to client: {}", logId, userResponse.toString());
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request login catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
            ResponseEntity<String> responseEntity = new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }
}
