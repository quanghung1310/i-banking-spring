package com.backend.model.request.transaction;

import com.backend.util.DataUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class TransactionRequest {
    //from (merchant nào) - to (luôn luôn là LH-Bank) - value (bao nhiêu tiền) - typeTrans
    private String requestId = DataUtil.createRequestId();
    private Long requestTime = System.currentTimeMillis();
    private long receiverCard; //to
    private Integer typeFee; //1: người nhận trả, 2. người chuyển trả
    private String content;
    private long amount;
    private Long merchantId; //merchant của to

    public boolean isValidData() {
        try {
            return !(StringUtils.isBlank(this.requestId)
                    || StringUtils.isBlank(this.content)
                    || receiverCard <= 0
                    || requestTime <= 0
                    || amount <= 0
                    || typeFee <= 0
                    || merchantId < 0);
        }
        catch (Exception ex) {
            return false;
        }
    }
}
