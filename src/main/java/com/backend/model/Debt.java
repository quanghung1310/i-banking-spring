package com.backend.model;

import io.vertx.core.json.jackson.DatabindCodec;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Debt {
    private long id;
    private long partnerId;
    private String partnerEmail;
    private String partnerName;
    private String partnerPhone;
    private long amount;
    private String content;
    private int action;
    private String createdAt;
    private String updatedAt;
    private long transId;

    @Override
    public String toString() {
        try {
            return DatabindCodec.mapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }
}
