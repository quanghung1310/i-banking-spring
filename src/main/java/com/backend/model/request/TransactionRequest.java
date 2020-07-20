package com.backend.model.request;

import com.backend.util.DataUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@Builder
public class TransactionRequest {
    //from (merchant nào) - to (luôn luôn là LH-Bank) - value (bao nhiêu tiền) - typeTrans
    private String requestId = DataUtil.createRequestId();
    private Long requestTime = System.currentTimeMillis();
    private Long cardNumber; //from
    private Integer typeFee; //1: người nhận trả, 2. người mua trả
    private Integer typeTrans; //1: Chuyển tiền  2. Nhận tiền
    private String content;
    private long amount;
    private long merchantId; //merchant của from
    private long userId; //to
    private int otp;
    private long debtId;

    public boolean isValidData() {
        try {
            return !(StringUtils.isBlank(this.requestId)
                    || StringUtils.isBlank(this.content)
                    || cardNumber <= 0
                    || requestTime <= 0
                    || amount < 0
                    || typeFee < 0
                    || typeTrans < 0);
        }
        catch (Exception ex) {
            return false;
        }
    }
}
