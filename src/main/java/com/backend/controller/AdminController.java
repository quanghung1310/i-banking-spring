package com.backend.controller;

import com.backend.constants.ErrorConstant;
import com.backend.dto.UserDTO;
import com.backend.model.Partner;
import com.backend.model.request.employee.EmployeeRequest;
import com.backend.model.request.employee.RegisterRequest;
import com.backend.model.response.*;
import com.backend.service.IAdminService;
import com.backend.service.IPartnerService;
import com.backend.service.IUserService;
import com.backend.util.DataUtil;
import com.google.gson.Gson;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.Timestamp;
import java.util.List;

@Controller
public class AdminController {
    private static final Logger logger = LogManager.getLogger(AdminController.class);

    private static final Gson PARSER = new Gson();

    @Value("${role.admin}")
    private String ADMIN;

    @Value("${role.employer}")
    private String EMPLOYER;

    @Value("${action.delete}")
    private String DELETE;

    @Value("${action.update}")
    private String UPDATE;

    @Value( "${my.bank.id}" )
    private int myBankId;

    private IUserService userService;
    private IAdminService adminService;
    private IPartnerService partnerService;

    @Autowired
    public AdminController(IAdminService adminService,
                           IUserService userService, IPartnerService partnerService) {
        this.userService = userService;
        this.adminService = adminService;
        this.partnerService = partnerService;
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

    @GetMapping(value = {"/get-employee", "/get-employee/{employerId}"})
    public ResponseEntity<String> register(@PathVariable(required = false) Long employerId) {
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request data: employerId - {}", logId, employerId);
        BaseResponse response;
        try {
            UserResponse user = getUser(logId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            if (!user.getRole().equals(ADMIN)) {
                logger.warn("{}| User - {} not authenticate!", logId, user.getId());
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.UNAUTHORIZED);
            }

            List<EmployeeResponse> employeeResponses = adminService.getEmployee(logId, employerId, EMPLOYER);
            if (employeeResponses == null) {
                logger.warn("{}| Employee not found!", logId);
                response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.OK);
            }
            String result = new JsonObject().put("employees", employeeResponses).toString();
            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, result);;
            logger.info("{}| Response to client: {}", logId, result);
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request register catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = {"/update-employee"})
    public ResponseEntity<String> updateEmployee(@RequestBody EmployeeRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response;
        try {
            if (!request.isValidData()) {
                logger.warn("{}| Validate request update employee data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request update employee success!", logId);

            UserResponse user = getUser(logId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());

            if (!user.getRole().equals(ADMIN)) {
                logger.warn("{}| User - {} not authenticate!", logId, user.getId());
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.UNAUTHORIZED);
            }

            String action = request.getAction();
            UserDTO employee = adminService.findById(request.getId());
            if (employee == null) {
                logger.warn("{}| Employee - {} not existed!", logId, request.getId());
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            String password = request.getPassword();
            if (action.equals(DELETE)) {
                employee.setRole(EMPLOYER + "DEL");
            } else if (action.equals(UPDATE)) {
                if (StringUtils.isNotBlank(request.getEmail())) {
                    employee.setEmail(request.getEmail());
                }
                if (StringUtils.isNotBlank(request.getPassword())) {

                    if (password.length() < 6 || password.length() > 20) {
                        logger.warn("{}| Length new password need: [6 -> 20] character!", logId);
                        response = DataUtil.buildResponse(ErrorConstant.BAD_PASSWORD, request.getRequestId(), null);
                        return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
                    }
                    employee.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                    employee.setLastPassword(employee.getPassword());
                    employee.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                }
                if (StringUtils.isNotBlank(request.getName())) {
                    employee.setName(request.getName());
                }
                if (StringUtils.isNotBlank(request.getPhone())) {
                    employee.setPhone(request.getPhone());
                }
            } else {
                logger.warn("{}| Action - {} not existed!", logId, action);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            EmployeeResponse employeeResponse = adminService.saveEmployee(employee);
            if (employeeResponse == null) {
                logger.warn("{}| Update employee - {} fail!", logId, employee.getId());
                response = DataUtil.buildResponse(ErrorConstant.SYSTEM_ERROR, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, request.getRequestId(), employeeResponse.toString());
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

    @GetMapping(value = {"/get-transaction-merchant/{beginTime}/{endTime}", "/get-transaction-merchant/{beginTime}/{endTime}/{merchantId}"})
    public ResponseEntity<String> getTransactionMerchant(@PathVariable(required = false) Integer merchantId,
                                                         @PathVariable String beginTime,
                                                         @PathVariable String endTime) {
        String logId = DataUtil.createRequestId();
        logger.info("{}| Request data: beginTime - {}, endTime - {}, merchantId - {},", logId, beginTime, endTime, merchantId);
        BaseResponse response;
        try {
            UserResponse user = getUser(logId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            if (!user.getRole().equals(ADMIN)) {
                logger.warn("{}| User - {} not authenticate!", logId, user.getId());
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.UNAUTHORIZED);
            }

            if (StringUtils.isBlank(beginTime) || StringUtils.isBlank(endTime)) {
                logger.warn("{}| Begin time - {} and End time - {} not empty!", logId, beginTime, endTime);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }

            if (merchantId != null) {
                Partner partner = partnerService.findById(merchantId);
                if (partner == null) {
                    logger.warn("{}| Merchant - {} not existed!", logId, merchantId);
                    response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId, null);
                    return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
                }
            } else {
                merchantId = myBankId;
            }
            List<TransMerchantResponse> transMerchantResponses = adminService.controlTransaction(logId, merchantId, beginTime, endTime);
            if (transMerchantResponses.size() <= 0) {
                logger.warn("{}| Get Transaction Merchant - {} Responses not found!", logId, merchantId);
                response = DataUtil.buildResponse(ErrorConstant.NOT_EXISTED, logId, null);
                return new ResponseEntity<>(response.toString(), HttpStatus.OK);
            }

            String result = new JsonObject().put("transactions", transMerchantResponses).toString();
            response = DataUtil.buildResponse(ErrorConstant.SUCCESS, logId, result);;
            logger.info("{}| Response to client: {}", logId, response);
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("{}| Request Get Transaction Merchant catch exception: ", logId, ex);
            response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, logId,null);
            return new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
        }
    }
}
