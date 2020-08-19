package com.backend.model.request.partner;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class GenerateQueryAccountRequest {
    private String requestId;
    private Long requestTime;
    private String partnerCode;
    private Long cardNumber;
    private String secretKey;

    public boolean isValidData() {
        try {
            return !(StringUtils.isBlank(this.requestId)
                    || StringUtils.isBlank(this.partnerCode)
                    || this.cardNumber <= 0
                    || this.requestTime <= 0
                    || StringUtils.isBlank(this.secretKey));
        }
        catch (Exception ex) {
            return false;
        }
    }
}
