package com.backend.repository;

import com.backend.dto.TransactionDTO;
import org.springframework.data.repository.CrudRepository;

public interface ITransactionRepository extends CrudRepository<TransactionDTO, Long> {
    TransactionDTO findFirstByTransId(String transId);

    TransactionDTO findAllByCardNumber(String cardNumber);
    
}
