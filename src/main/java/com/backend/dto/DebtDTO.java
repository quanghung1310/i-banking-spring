package com.backend.dto;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name="debts")
public class DebtDTO {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;

    private long userId; //chủ nợ

    private long cardNumber; // stk con nợ

    private long debtorId; //con nợ

    @Column(length = 100)
    private String content;

    private int isActive; //khong can thiet luu?

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private int action; //1. INIT, 2.DELETE, 3.COMPLETED

    private long amount; //so tien nợ
}
