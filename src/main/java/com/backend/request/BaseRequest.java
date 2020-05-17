package com.backend.request;

import com.backend.constant.StringConstant;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
@Builder
public class BaseRequest {
    private static final Logger logger = LogManager.getLogger(BaseRequest.class);

    private String requestId;
    private String initiator;
    private String requestType;

    public static BaseRequest parseBaseRequest(String logId, JsonObject baseData) {
        try {
            return new BaseRequest(
                    baseData.getString(StringConstant.REQUEST_ID, ""),
                    baseData.getString(StringConstant.INITIATOR, ""),
                    baseData.getString(StringConstant.REQUEST_TYPE, ""));
        }
        catch (Exception ex) {
            logger.error("{}| Parse data to BaseRequest catch exception: ", logId, ex);
            return null;
        }
    }

    public static boolean isValidBaseDataRequest(String logId, JsonObject baseData) {
        try {
            BaseRequest request = parseBaseRequest(logId, baseData);
            if (request == null
                || request.initiator.isEmpty()
                || request.requestId.isEmpty()
                || request.requestType.isEmpty()) {
                logger.warn("{}| Data reuqest is empty!", logId);
                return false;
            }
            return true;
        }
        catch (Exception ex) {
            logger.error("{}| Parse data to BaseRequest catch exception: ", logId, ex);
            return false;
        }
    }
}
