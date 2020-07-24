package com.backend.model;

import io.vertx.core.json.jackson.DatabindCodec;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Partner {
    private Integer id;
    private String partnerCode;
    private String publicKey;
    private String email;
    private String phoneNumber;
    private String secretKey;
    private String password;
    private String name;

    @Override
    public String toString() {
        try {
            return DatabindCodec.mapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }
}
