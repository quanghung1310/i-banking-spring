package com.backend.repository;

import com.backend.dto.AccountSavingDTO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface IAccountSavingRepository extends CrudRepository<AccountSavingDTO, Long> {
    List<AccountSavingDTO> findAllByUserId(long userId);
}
