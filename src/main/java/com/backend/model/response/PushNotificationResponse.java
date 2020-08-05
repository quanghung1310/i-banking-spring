package com.backend.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushNotificationResponse {
    private int status;
    private String message;

    public PushNotificationResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
