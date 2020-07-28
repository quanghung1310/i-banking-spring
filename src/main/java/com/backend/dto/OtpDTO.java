package com.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="otp")
public class OtpDTO {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;

    private long otp;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private String action; //payment: otp payment, debt: otp pay debt

    private int status; //1. INIT, 2.DELETE

    private long userId; //người nhận được otp (người cần chuyển tiền)

    @Column(length = 20)
    private long transId;
}
