package com.backend.dto;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name="debt")
public class DebtDTO {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;

    private long userId;

    private long cardNumber;

    private long debtorId;

    @Column(length = 100)
    private String content;

    private int isActive;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private int action;

    private long amount;
}
