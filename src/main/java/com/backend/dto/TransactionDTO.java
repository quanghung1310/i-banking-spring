package com.backend.dto;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name="transactions")
public class TransactionDTO {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;

    @Column(length = 20)
    private long transId;

    private long senderCard;

    private Long amount;

    private Integer typeFee;

    @Column(length = 20)
    private long receiverCard;

    private Integer typeTrans; //1. deposit, 2.debt

    private Long merchantId;

    @Column(length = 100)
    private String content;

    @Column(length = 30)
    private String status;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}
