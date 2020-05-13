package com.backend.dto;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "PARTNER")
public class PartnerDTO {
    @Id
    @GeneratedValue
    private long id;

    private String partnerCode;

    private String privateKey;

    private String publicKey;

    private String secretKey;

    private boolean isAcive;
}
