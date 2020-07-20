package com.backend.service;

import com.backend.dto.PartnerDTO;
import com.backend.model.Partner;

import java.util.List;

public interface IPartnerService {
    List<Partner> getAll();

    Partner findByPartnerCode(String partnerCode);
}
