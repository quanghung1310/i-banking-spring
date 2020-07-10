package com.backend.model.request;

import com.backend.util.DataUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class TransactionRequest {
    private String requestId = DataUtil.createRequestId();
    private Long requestTime = System.currentTimeMillis();
    private Long cardNumber;
    private Integer typeFee;
    private Integer typeTrans;
    private String content;
    private long amount;
    private long userId;
    private long merchantId;

    public boolean isValidData() {
        try {
            return !(StringUtils.isBlank(this.requestId)
                    || StringUtils.isBlank(this.content)
                    || cardNumber <= 0
                    || requestTime <= 0
                    || amount < 0
                    || typeFee < 0
                    || userId < 0
                    || typeTrans < 0);
        }
        catch (Exception ex) {
            return false;
        }
    }
}
