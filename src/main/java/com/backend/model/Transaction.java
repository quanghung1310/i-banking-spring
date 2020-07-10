package com.backend.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Transaction {
    public long id;
    public long transId;
    public long userId;
    public long amount;
    public long fee;
    public Integer typeFee;
    public String cardName;
    public long cardNumber;
    public Integer typeTrans;
    public long merchantId;
    public String content;
    public String createdAt;
}
