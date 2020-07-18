package com.backend.model.request;

import com.backend.util.DataUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class TransferRequest {
    private String requestId = DataUtil.createRequestId();
    private Long requestTime = System.currentTimeMillis();
    private String signature;
    private String merchantCode;

    public boolean isValidData() {
        try {
            return !(StringUtils.isBlank(this.requestId)
                    || requestTime <= 0
                    || StringUtils.isBlank(this.signature)
                    || StringUtils.isBlank(this.merchantCode));

        }
        catch (Exception ex) {
            return false;
        }
    }
}
