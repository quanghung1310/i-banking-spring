package com.backend.model.request;

import com.backend.util.DataUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class PayDebtRequest {
    private String requestId = DataUtil.createRequestId();
    private Long requestTime = System.currentTimeMillis();
    private long debtId;
    private int typeFee; //1. from trả, 2. to trả
    private String content;

    public boolean isValidData() {
        try {
            return !(StringUtils.isBlank(this.requestId)
                    || requestTime <= 0
                    || debtId < 0
                    || typeFee <= 0);
        }
        catch (Exception ex) {
            return false;
        }
    }
}
