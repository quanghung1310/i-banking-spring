package com.backend.model.request;

import com.backend.util.DataUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class LoginRequest {
    private String requestId = DataUtil.createRequestId();
    private Long requestTime = System.currentTimeMillis();
    private String userName;
    private String password;

    public boolean isValidData() {
        try {
            return !(StringUtils.isBlank(this.requestId)
                    || StringUtils.isBlank(this.userName)
                    || StringUtils.isBlank(this.password)
                    || requestTime <= 0);

        }
        catch (Exception ex) {
            return false;
        }
    }
}
