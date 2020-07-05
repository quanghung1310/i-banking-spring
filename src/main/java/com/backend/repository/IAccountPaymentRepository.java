package com.backend.repository;

import com.backend.dto.AccountPaymentDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.List;

public interface IAccountPaymentRepository extends CrudRepository<AccountPaymentDTO, Long> {
    AccountPaymentDTO findFirstByCardNumber(long cardNumber);

    AccountPaymentDTO findFirstByUserId(long userId);

    List<AccountPaymentDTO> findAllByUserId(long userId);

    AccountPaymentDTO findFirstByCardNumberAndUserId(long cardNumber, long userId);
}
