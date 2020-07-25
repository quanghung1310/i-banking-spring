package com.backend.mapper;

import com.backend.dto.PartnerDTO;
import com.backend.model.Partner;

public class PartnerMapper {
    public static Partner toModel(PartnerDTO partnerDTO, boolean isSig) {
        if (partnerDTO == null) {
            return null;
        }
        Partner partner = Partner.builder()
                .id(partnerDTO.getId())
                .email(partnerDTO.getEmail())
                .partnerCode(partnerDTO.getPartnerCode())
                .phoneNumber(partnerDTO.getPhoneNumber())
                .name(partnerDTO.getName())
                .build();
        if (isSig) {
            partner.setPublicKey(partnerDTO.getPublicKey());
            partner.setSecretKey(partnerDTO.getPrivateKey());
            partner.setPassword(partnerDTO.getPassword());
        }
        return partner;
    }
}
