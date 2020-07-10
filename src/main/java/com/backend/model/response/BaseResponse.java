package com.backend.model.response;



import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BaseResponse {
    private String requestId;
    private int resultCode;
    private String message;
    private long responseTime;
    private JsonObject data;

    @Override
    public String toString() {
        try {
            return DatabindCodec.mapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }
}
