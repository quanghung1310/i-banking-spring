package com.backend.dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name="partners")
public class PartnerDTO {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;
    private String partnerCode;
    @Column(length = 2048)
    private String privateKey;
    @Column(length = 2048)
    private String publicKey;
    private String email;
    private String phoneNumber;
    private String password;
}
