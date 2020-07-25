package com.backend.model.request;

import com.backend.util.DataUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class DeleteDebtRequest {
    private String requestId = DataUtil.createRequestId();
    private Long requestTime = System.currentTimeMillis();
    private long debtId;
    private String content;

    public boolean isValidData() {
        try {
            return !(StringUtils.isBlank(this.requestId)
                    || this.debtId <= 0
                    || this.requestTime <= 0
                    || StringUtils.isBlank(this.content));
        }
        catch (Exception ex) {
            return false;
        }
    }
}
