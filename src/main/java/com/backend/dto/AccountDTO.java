package com.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDTO {

    private int id;

    private String userName;

    private String password;

    private long balance;

    private String customerName;

    private String customerPhone;

    private String customerEmail;

    private boolean isActive;

    private int agency;

    private String openDate;

    private String closeDate;
}
