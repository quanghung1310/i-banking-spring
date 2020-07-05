package com.backend.model;

import io.vertx.core.json.jackson.DatabindCodec;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Debt {
    private long creditorId;
    private String creditEmail;
    private String creditName;
    private String creditPhone;
    private long amount;
    private String content;
    private int action;
    private String createdAt;
    private String updatedAt;

    @Override
    public String toString() {
        try {
            return DatabindCodec.mapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }
}
