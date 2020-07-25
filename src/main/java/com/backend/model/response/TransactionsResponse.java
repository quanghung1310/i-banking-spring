package com.backend.model.response;

import com.backend.model.Transaction;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class TransactionsResponse {
    private long userId;
    private long cardNumber;
    private String cardName;
    private List<Transaction> transactions;

    @Override
    public String toString() {
        try {
            return DatabindCodec.mapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }
}
