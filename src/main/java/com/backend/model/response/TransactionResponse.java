package com.backend.model.response;

import io.vertx.core.json.jackson.DatabindCodec;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
public class TransactionResponse {
    private Long transId;
    private String cardName;
    private Long cardNumber;
    private Long amount;
    private Integer typeFee;
    private Long fee;
    private String content;
    private Timestamp createDate;
    private Long merchantId;
    private String status;

    @Override
    public String toString() {
        try {
            return DatabindCodec.mapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }
}
