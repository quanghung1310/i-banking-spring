package com.backend.model.request.bank;

import com.backend.util.DataUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class DepositRequest {
    private String requestId = DataUtil.createRequestId();
    private Long requestTime = System.currentTimeMillis();
    private String userName;
    private Long cardNumber;
    private long balance;

    public boolean isValidData() {
        try {
            return !(StringUtils.isBlank(this.requestId)
                    || balance <= 1000
                    || balance > 20000000
                    || requestTime <= 0
                    || (StringUtils.isBlank(this.userName) && cardNumber == null));

        }
        catch (Exception ex) {
            return false;
        }
    }
}
