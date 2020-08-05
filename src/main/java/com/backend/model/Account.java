package com.backend.model;

import io.vertx.core.json.jackson.DatabindCodec;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Account {
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
    public String reminderName;

    @Override
    public String toString() {
        try {
            return DatabindCodec.mapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }
}
