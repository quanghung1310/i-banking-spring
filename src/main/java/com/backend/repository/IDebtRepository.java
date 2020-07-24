package com.backend.repository;

import com.backend.dto.DebtDTO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface IDebtRepository extends CrudRepository<DebtDTO, Long> {

    List<DebtDTO> findAllByUserIdAndActionAndIsActiveOrderByIdDesc(long userId, int action, int isActive);

//    List<DebtDTO> findAllByDebtorIdAndActionAndIsActive(long debtorId, int action, int isActive);

    DebtDTO findFirstByIdAndActionAndIsActive(long id, int action, int isActive);
}
