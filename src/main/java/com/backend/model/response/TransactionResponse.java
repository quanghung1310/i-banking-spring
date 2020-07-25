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
    private long transId;
    private long receiverCard;
    private String cardName;
    private Long amount;
    private Integer typeFee;
    private Long fee;
    private String content;
    private String status;
    private Long merchantId;
    private String createDate;

    @Override
    public String toString() {
        try {
            return DatabindCodec.mapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }
}
