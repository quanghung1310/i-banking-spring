package com.backend.repository;

import com.backend.dto.TransactionDTO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ITransactionRepository extends CrudRepository<TransactionDTO, Long> {
    TransactionDTO findFirstByTransId(Long transId);

    List<TransactionDTO> findAllBySenderCard(Long senderCard);

    List<TransactionDTO> findAllByReceiverCard(Long receiverCard);

    TransactionDTO findFirstBySenderCardAndReceiverCard(Long senderCard, Long receiverCard);
}
