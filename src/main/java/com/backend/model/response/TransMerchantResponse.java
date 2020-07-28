package com.backend.model.response;

import com.backend.model.TransactionMerchant;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class TransMerchantResponse {
    private int merchantId;
    private String merchantName;
    private String merchantPhone;
    private String merchantEmail;
    private long totalAMount;
    private List<TransactionMerchant> transactionMerchants;

    @Override
    public String toString() {
        try {
            return DatabindCodec.mapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }
}
