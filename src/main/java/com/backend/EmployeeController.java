package com.backend;

import com.backend.constants.ErrorConstant;
import com.backend.model.request.RegisterRequest;
import com.backend.model.response.BaseResponse;
import com.backend.model.response.RegisterResponse;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class EmployeeController {
    private static final Logger logger = LogManager.getLogger(EmployeeController.class);

    private static final Gson PARSER = new Gson();

    @Autowired
    IUserService userService;

    @PostMapping(value = "/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response = new BaseResponse();
        try {
            if (!request.isValidData()) {
                logger.warn("{}| Validate request register data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request register success!", logId);

            RegisterResponse registerResponse = userService.register(logId, request);

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
            ResponseEntity<String> responseEntity = new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping(value = "/deposit")
    public ResponseEntity<String> deposit(@RequestBody RegisterRequest request) {
        String logId = request.getRequestId();
        logger.info("{}| Request data: {}", logId, PARSER.toJson(request));
        BaseResponse response = new BaseResponse();
        try {
            if (!request.isValidData()) {
                logger.warn("{}| Validate request register data: Fail!", logId);
                response = DataUtil.buildResponse(ErrorConstant.BAD_FORMAT_DATA, request.getRequestId(), null);
                return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
            }
            logger.info("{}| Valid data request register success!", logId);

            RegisterResponse registerResponse = userService.register(logId, request);

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
            ResponseEntity<String> responseEntity = new ResponseEntity<>(
                    response.toString(),
                    HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }
}
