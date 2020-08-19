package com.backend.repository;

import com.backend.dto.DebtDTO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface IDebtRepository extends CrudRepository<DebtDTO, Long> {

    List<DebtDTO> findAllByUserIdAndActionOrderByIdDesc(long userId, int action);

    List<DebtDTO> findAllByCardNumberAndActionOrderByIdDesc(long cardNumber, int action);

    DebtDTO findFirstByIdAndActionAndIsActive(long id, int action, int isActive);

    DebtDTO findFirstByTransIdAndActionAndIsActive(long transId, int action, int isActive);
}
