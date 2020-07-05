package com.backend.repository;

import com.backend.dto.DebtDTO;
import org.springframework.data.repository.CrudRepository;

public interface IDebtRepository extends CrudRepository<DebtDTO, Long> {
}
