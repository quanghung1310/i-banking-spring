package com.backend.model.request.bank;

import com.backend.util.DataUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class QueryAccountRequest {
    private String requestId;
    private Long requestTime;
    private String partnerCode;
    private Long cardNumber;
    private String hash;

    public boolean isValidData() {
        try {
            return !(StringUtils.isBlank(this.requestId)
                    || StringUtils.isBlank(this.hash)
                    || StringUtils.isBlank(this.partnerCode)
                    || this.cardNumber <= 0
                    || this.requestTime <= 0);

        }
        catch (Exception ex) {
            return false;
        }
    }
}
