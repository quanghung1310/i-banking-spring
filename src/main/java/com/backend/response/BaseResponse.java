package com.backend.response;

import com.backend.constant.ErrorConstants;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseResponse {
    private String requestId;
    private int resultCode;
    private String message;
    private JsonObject data;

    public BaseResponse(String requestId, int resultCode) {
        this.requestId = requestId;
        this.resultCode = resultCode;
        this.message = ErrorConstants.getDescVn(resultCode);
    }

    public JsonObject toJson() {
        return new JsonObject(this.toString());
    }

    @Override
    public String toString() {
        try {
            return DatabindCodec.mapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }

    public BaseResponse(String requestId, int resultCode, JsonObject data) {
        this.requestId = requestId;
        this.resultCode = resultCode;
        this.message = ErrorConstants.getDescVn(resultCode);
        this.data = data;
    }
}
