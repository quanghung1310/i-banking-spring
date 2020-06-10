package com.backend.dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name="partner")
public class PartnerDTO {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;
    private String partnerCode;
    private String privateKey;
    private String publicKey;
    private String email;
    private String phoneNumber;
    private String password;
}
