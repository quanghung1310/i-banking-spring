package com.backend.request;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.Getter;

@Getter
public class AccountBankReq {

    private Long requestTime;
    private String partnerCode;
    private String numberAccount;
    private Integer typeAccount;
    private String hash;
    private String description;

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
