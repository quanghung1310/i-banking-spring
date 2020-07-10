package com.backend.dto;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name="account_savings")
public class AccountSavingDTO {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private long cardNumber;

    @Column(length = 100)
    private String cardName;

    private long balance;

    private Timestamp openDate;

    private Timestamp closeDate;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    @Column(length = 100)
    private String description;

    private long userId;

}
