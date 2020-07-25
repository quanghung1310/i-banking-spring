package com.backend.repository;

import com.backend.dto.TransactionDTO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ITransactionRepository extends CrudRepository<TransactionDTO, Long> {
    TransactionDTO findFirstByTransId(long transId);

    List<TransactionDTO> findAllBySenderCard(long senderCard);

    List<TransactionDTO> findAllByReceiverCard(long receiverCard);

    TransactionDTO findFirstBySenderCardAndReceiverCard(long senderCard, long receiverCard);
}
