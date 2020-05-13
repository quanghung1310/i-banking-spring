package com.backend.response;

import com.backend.constant.ErrorConstants;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseResponse {

    private int status = -1;
    private String message;
    private long responseTime;

    public BaseResponse() {
        this.message = "Lỗi";
        this.responseTime = System.currentTimeMillis();
    }

    public BaseResponse(int status) {
        this.status = status;
        this.message = ErrorConstants.getDescVn(status);
        this.responseTime = System.currentTimeMillis();
    }

    public BaseResponse(String message) {
        this.status = ErrorConstants.BAD_FORMAT_DATA;
        this.message = message;
        this.responseTime = System.currentTimeMillis();
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
}
