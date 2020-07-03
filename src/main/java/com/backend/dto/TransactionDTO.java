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
    private String transId;

    private long amount;

    private long fee;

    private Integer typeFee;

    @Column(length = 50)
    private String cardName;

    @Column(length = 20)
    private String cardNumber;

    private Integer typeTrans;

    private long merchantId;

    @Column(length = 100)
    private String content;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}
