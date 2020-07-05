package com.backend.repository;

import com.backend.dto.DebtDTO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface IDebtRepository extends CrudRepository<DebtDTO, Long> {

    List<DebtDTO> findAllByUserIdAndAction(long userId, int action);

    List<DebtDTO> findAllByDebtorIdAndAction(long debtorId, int action);
}
