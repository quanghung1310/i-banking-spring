package com.backend.model;

import io.vertx.core.json.jackson.DatabindCodec;

public class Reminder {
    public long id;
    public long cardNumber;
    public String cardName;
    public String closeDate;
    public String createdAt;
    public String updatedAt;
    public String description;
    public String type;
    public Long balance;
    public Long userId;
    public long reminderId;
    public String typeReminder;
    public long merchantId;

    @Override
    public String toString() {
        try {
            return DatabindCodec.mapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }
}
