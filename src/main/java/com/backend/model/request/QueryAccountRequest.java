package com.backend.model.request;

import com.backend.util.DataUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public class QueryAccountRequest {
    private String requestId;
    private Long requestTime;
    private String bankCode;
    private long accountNumber;
    private String hash;

    public boolean isValidData() {
        try {
            return !(StringUtils.isBlank(this.requestId)
                    || StringUtils.isBlank(this.bankCode)
                    || this.accountNumber <= 0
                    || requestTime <= 0);

        }
        catch (Exception ex) {
            return false;
        }
    }
}
