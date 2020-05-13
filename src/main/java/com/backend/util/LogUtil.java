package com.backend.util;

import io.vertx.core.http.HttpServerRequest;
import org.apache.logging.log4j.Logger;

public class LogUtil {
    public static void logHttpRequest(Logger LOGGER, HttpServerRequest serverRequest) {
        LOGGER.info("");
        LOGGER.info(" [WebService] [{}] Path: {}", serverRequest.method(), serverRequest.path());
        LOGGER.info(" Partner ip: {}", serverRequest.remoteAddress().host());
        for (int i = 0; i < serverRequest.headers().size(); i++) {
            LOGGER.info(" Key: {} - Value: {}", serverRequest.headers().entries().get(i).getKey(), serverRequest.headers().entries().get(i).getValue());
        }
    }
}
