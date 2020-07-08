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
public class TransactionResponse {
    private Long id;
    private String transId;
    private Integer typeFee;
    private Long amount;
    private Long fee;
    private String cardName;
    private String cardNumber;
    private String content;
    private Timestamp createDate;
    private Long merchantId;

    @Override
    public String toString() {
        try {
            return DatabindCodec.mapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }
}
