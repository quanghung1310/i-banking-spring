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

    private int action; //1: otp payment, 2: otp pay debt
}
