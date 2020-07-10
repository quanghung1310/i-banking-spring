package com.backend.model.response;

import com.backend.model.Account;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
public class RegisterResponse {
    private String userName;
    private String password;
    private String createDate;
    private Account account;

    @Override
    public String toString() {
        try {
            return DatabindCodec.mapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }
}
