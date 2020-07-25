package com.backend.model.request;

import com.backend.util.DataUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class RegisterRequest {
    private String requestId = DataUtil.createRequestId();
    private Long requestTime = System.currentTimeMillis();
    private String email;
    private String name;
    private String phone;
    private String cardName;

    public boolean isValidData() {
        try {
            return !(StringUtils.isBlank(this.requestId)
                    || StringUtils.isBlank(this.email)
                    || StringUtils.isBlank(this.name)
                    || StringUtils.isBlank(this.phone)
                    || StringUtils.isBlank(this.cardName)
                    || requestTime <= 0);

        }
        catch (Exception ex) {
            return false;
        }
    }
}
