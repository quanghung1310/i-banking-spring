package com.backend.model.response;

import com.backend.model.Account;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserResponse {
    private long id;
    private String email;
    private String name;
    private String phone;
    private String createdAt;
    private List<Account> account;

    @Override
    public String toString() {
        try {
            return DatabindCodec.mapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }
}
