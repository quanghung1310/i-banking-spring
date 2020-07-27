package com.backend.controller;

import com.backend.constants.ErrorConstant;
import com.backend.model.request.employee.RegisterRequest;
import com.backend.model.response.BaseResponse;
import com.backend.model.response.RegisterResponse;
import com.backend.model.response.UserResponse;
import com.backend.service.IAdminService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class AdminController {
    private static final Logger logger = LogManager.getLogger(AdminController.class);

    private static final Gson PARSER = new Gson();

    @Value("${role.admin}")
    private String ADMIN;

    @Value("${role.employer}")
    private String EMPLOYER;

    private IUserService userService;
    private IAdminService adminService;

    @Autowired
    public AdminController(IAdminService adminService,
                              IUserService userService) {
        this.userService = userService;
        this.adminService = adminService;
    }


    private UserResponse getUser(String logId, Object principal) {
        return userService.getUser(logId, ((UserDetails)principal).getUsername());
    }

    @PostMapping(value = "/register-employee")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
            if (!request.isValidData()) {
                logger.warn("{}| Validate request register employee data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request register employee success!", logId);

            UserResponse user = getUser(logId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            if (!user.getRole().equals(ADMIN)) {
                logger.warn("{}| User - {} not authenticate!", logId, user.getId());
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.UNAUTHORIZED);
            }
            RegisterResponse registerResponse = adminService.registerEmployee(logId, request, EMPLOYER);

            if (registerResponse == null) {
                logger.warn("{}| Register fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, request.getRequestId(), null);
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
}
