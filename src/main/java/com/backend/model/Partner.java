package com.backend.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Partner {
    private Integer id;
    private String partnerCode;
    private String publicKey;
    private String email;
    private String phoneNumber;
}
