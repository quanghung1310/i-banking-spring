package com.backend.util;


import com.backend.constants.ErrorConstant;
import com.backend.model.response.BaseResponse;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class DataUtil {
    private static final Logger logger = LogManager.getLogger(DataUtil.class);

    public static String createRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static BaseResponse buildResponse(int resultCode, String requestId, String responseBody) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setResultCode(resultCode);
        baseResponse.setMessage(ErrorConstant.getMessage(resultCode));
        baseResponse.setResponseTime(System.currentTimeMillis());
        baseResponse.setRequestId(requestId);
        if (responseBody != null) {
            baseResponse.setData(new JsonObject(responseBody));
        }

        return baseResponse;
    }

    public static String convertTimeWithFormat(long timeInMillisecond, String format) {
        try {
            DateFormat f = new SimpleDateFormat(format);
            return f.format(timeInMillisecond);
        } catch (Exception e) {
            return "";
        }
    }
}
