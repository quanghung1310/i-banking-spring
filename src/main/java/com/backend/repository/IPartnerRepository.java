package com.backend.repository;

import com.backend.dto.PartnerDTO;
import org.springframework.data.repository.CrudRepository;

public interface IPartnerRepository extends CrudRepository<PartnerDTO, Integer> {

    PartnerDTO findFirstByPartnerCode(String partnerCode);
}
