package com.backend.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TransactionMerchant {
    private int merchantId;
    private long senderCard;
    private String senderName;
    private long receiverCard;
    private String receiverName;
    private long amount;
    private String status;
    private String createDate;
    private String content;
    private long fee;
    private int typeFee;
}
