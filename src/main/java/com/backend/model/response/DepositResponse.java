package com.backend.model.response;

import io.vertx.core.json.jackson.DatabindCodec;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DepositResponse {
    private long totalBalance;

    @Override
    public String toString() {
        try {
            return DatabindCodec.mapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }
}
