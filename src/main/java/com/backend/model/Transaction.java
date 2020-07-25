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
    public long senderCard;
    public Long amount;
    public Integer typeFee;
    public long receiverCard;
    public Integer typeTrans;
    public Long merchantId;
    public String content;
    public String status;
    public String createdAt;
    public String updateAt;
}
