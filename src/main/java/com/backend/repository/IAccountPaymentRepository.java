package com.backend.repository;

import com.backend.dto.AccountPaymentDTO;
import org.springframework.data.repository.CrudRepository;

public interface IAccountPaymentRepository extends CrudRepository<AccountPaymentDTO, Long> {
}
