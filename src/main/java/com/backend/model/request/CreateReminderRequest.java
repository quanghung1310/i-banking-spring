package com.backend.model.request;

import com.backend.util.DataUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class CreateReminderRequest {
    private String requestId = DataUtil.createRequestId();
    private Long requestTime = System.currentTimeMillis();
    private String nameReminisce;
    private long cardNumber;
    private int type; //1: send, 2: debt
    private long userId;
    private int merchantId;

    public boolean isValidData() {
        try {
            return !(StringUtils.isBlank(this.requestId)
                    || this.type <= 0
                    || this.type > 2
                    || this.requestTime <= 0
                    || this.cardNumber <= 0
                    || this.userId <= 0
                    || this.merchantId < 0);

        }
        catch (Exception ex) {
            return false;
        }
    }
}
