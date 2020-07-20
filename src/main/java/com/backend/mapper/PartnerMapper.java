package com.backend.mapper;

import com.backend.dto.PartnerDTO;
import com.backend.model.Partner;

public class PartnerMapper {
    public static Partner toModel(PartnerDTO partnerDTO) {
        if (partnerDTO == null) {
            return null;
        }
        return Partner.builder()
                .id(partnerDTO.getId())
                .email(partnerDTO.getEmail())
                .partnerCode(partnerDTO.getPartnerCode())
                .phoneNumber(partnerDTO.getPhoneNumber())
                .publicKey(partnerDTO.getPublicKey())
                .secretKey(partnerDTO.getPrivateKey())
                .password(partnerDTO.getPassword())
                .build();
    }
}
