package com.backend.model.request;

import com.backend.util.DataUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayDebtRequest {
    private String requestId = DataUtil.createRequestId();
    private Long requestTime = System.currentTimeMillis();
    private long debtId;
    private long userId;
    private String content;
}
