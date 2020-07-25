package com.backend.model.request;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class TransferRequest {
    private Boolean isTransfer; //true. from chuyển cho to (update balance = to.balance + value), false. to chuyển cho from (update balance = to.balance - value)
    private String bankCode;
    private long from;
    private String hash;
    private String partnerCode;
    private String requestId;
    private long requestTime;
    private long to; //account của LH-Bank
    private long value;
    private String signature;
    private String description;
    private int typeFee; //1. from trả, 2. to trả
    private String cardName;

    public boolean isValidData() {
        try {
            return !(this.isTransfer == null
                    || StringUtils.isBlank(this.bankCode)
                    || this.from <= 0
                    || StringUtils.isBlank(this.hash)
                    || StringUtils.isBlank(this.partnerCode)
                    || StringUtils.isBlank(this.requestId)
                    || StringUtils.isBlank(this.cardName)
                    || this.requestTime <= 0
                    || this.to <= 0
                    || this.value <= 0
                    || this.typeFee < 0
                    || StringUtils.isBlank(this.signature));
        }
        catch (Exception ex) {
            return false;
        }
    }
}
