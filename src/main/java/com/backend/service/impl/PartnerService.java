package com.backend.service.impl;

import com.backend.dto.PartnerDTO;
import com.backend.mapper.PartnerMapper;
import com.backend.model.Partner;
import com.backend.repository.IPartnerRepository;
import com.backend.service.IPartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PartnerService implements IPartnerService {
    @Autowired
    IPartnerRepository partnerRepository;

    @Override
    public List<Partner> getAll() {
        List<Partner> result = new ArrayList<>();
        partnerRepository.findAll().forEach(partnerDTO -> result.add(PartnerMapper.toModel(partnerDTO, false)));

        return result;
    }

    @Override
    public Partner findByPartnerCode(String partnerCode) {
        return PartnerMapper.toModel(partnerRepository.findFirstByPartnerCode(partnerCode), true);
    }

    @Override
    public Partner findById(int id) {
        Optional<PartnerDTO> partnerDTO = partnerRepository.findById(id);
        return partnerDTO.map(dto -> PartnerMapper.toModel(dto, false)).orElse(null);
    }
}
