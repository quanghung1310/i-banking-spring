package com.backend.util;

import com.backend.response.BaseResponse;
import io.vertx.core.http.HttpServerResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResponseUtil {

    private static final Logger LOGGER = LogManager.getLogger(ResponseUtil.class);

    public static final int HTTP_OK = 200;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_ACCEPTED = 202;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_METHOD_NOT_ALLOWED = 405;
    public static final int HTTP_REQUEST_TIME_OUT = 408;
    public static final int HTTP_GATEWAY_TIME_OUT = 504;

    public static String buildResponse(String requestId, int resultCode) {
        return new BaseResponse(requestId, resultCode).toString();
    }

    public static void responseError(String logId, String requestId, HttpServerResponse response, int httpCode, int resultCode) {
        responseToClient(logId, response, buildResponse(requestId, resultCode), httpCode);
    }

    public static void responseToClient(String logId, HttpServerResponse response, String responseStr, int httpCode) {
        try {
            if (response.closed()) {
                LOGGER.warn("{}| Response to client Fail (Response closed) => {}", logId, responseStr);
                return;
            }
            if (response.ended()) {
                LOGGER.warn("{}| Response to client Fail (Response ended) => {}", logId, responseStr);
                return;
            }
            LOGGER.info("{}| Response to client -> {}", logId, responseStr);
            response.setChunked(true)
                    .setStatusCode(httpCode)    // warning: status code must set before body
                    .putHeader("Content-Type", "application/json")
                    .write(responseStr, "UTF-8")
                    .end();
        } catch (Exception e) {
            LOGGER.error("{}| Response to client Fail (Response catch Exception) => {}", logId, responseStr, e);
        }
    }
}