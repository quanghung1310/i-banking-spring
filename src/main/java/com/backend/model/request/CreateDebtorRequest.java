package com.backend.model.request;

import com.backend.util.DataUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class CreateDebtorRequest {
    private String requestId = DataUtil.createRequestId();
    private Long requestTime = System.currentTimeMillis();
    private long debtorId;
    private long cardNumber;
    private long amount;
    private long userId;
    private String content;

    public boolean isValidData() {
        try {
            return !(StringUtils.isBlank(this.requestId)
                    || this.debtorId <= 0
                    || this.amount <= 0
                    || this.requestTime <= 0
                    || this.cardNumber <= 0
                    || this.userId <= 0
                    || StringUtils.isBlank(this.content));

        }
        catch (Exception ex) {
            return false;
        }
    }
}
