package com.backend.model.request.employee;

import com.backend.util.DataUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class EmployeeRequest {
    private String requestId = DataUtil.createRequestId();
    private Long requestTime = System.currentTimeMillis();
    private String email;
    private String name;
    private String phone;
    private String password;
    private long id;
    private String action;
    public boolean isValidData() {
        try {
            return !(StringUtils.isBlank(this.requestId)
                    || this.id <= 0
                    || this.requestTime <= 0
                    || (StringUtils.isBlank(this.requestId)));

        }
        catch (Exception ex) {
            return false;
        }
    }
}
