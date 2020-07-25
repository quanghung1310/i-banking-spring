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
    private String nameReminisce; //init
    private Long cardNumber;    //init
    private Integer type; //init 1: send, 2: debt
    private Integer merchantId;//init
    private Long reminderId;
    private String action; //UPDATE, DELETE
    public boolean isValidData() {
        try {
            return !(StringUtils.isBlank(this.requestId)
                    || this.requestTime <= 0);
        }
        catch (Exception ex) {
            return false;
        }
    }
}
