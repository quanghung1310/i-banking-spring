package com.backend.dto;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Setter
public class PartnerDTO {
    private long id;

    private String partnerCode;

    private String privateKey;

    private String publicKey;

    private String secretKey;

    private boolean isAcive;
}
