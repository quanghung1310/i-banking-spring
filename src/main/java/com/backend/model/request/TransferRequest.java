package com.backend.model.request;

import com.backend.util.DataUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class TransferRequest {
    private Boolean isTransfer;
    private String bankCode;
    private long from;
    private String hash;
    private String merchantCode;
    private String requestId;
    private long requestTime;
    private long to;
    private long value;
    private String signature;
    private String description;

    public boolean isValidData() {
        try {
            return !(this.isTransfer == null
                    || StringUtils.isBlank(this.bankCode)
                    || this.from <= 0
                    || StringUtils.isBlank(this.hash)
                    || StringUtils.isBlank(this.merchantCode)
                    || StringUtils.isBlank(this.requestId)
                    || this.requestTime <= 0
                    || this.to <= 0
                    || this.value <= 0
                    || StringUtils.isBlank(this.signature));
        }
        catch (Exception ex) {
            return false;
        }
    }
}
