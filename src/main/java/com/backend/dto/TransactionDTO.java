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
    private Long id;

    @Column(length = 20)
    private Long transId;

    private Long userId;

    private long amount;

    private long fee;

    private Integer typeFee;

    @Column(length = 50)
    private String cardName;

    @Column(length = 20)
    private Long cardNumber;

    private Integer typeTrans;

    private long merchantId;

    @Column(length = 100)
    private String content;

    @Column(length = 30)
    private String status;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}
