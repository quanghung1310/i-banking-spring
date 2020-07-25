package com.backend.repository;

import com.backend.dto.TransactionDTO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ITransactionRepository extends CrudRepository<TransactionDTO, Long> {
    TransactionDTO findFirstByTransId(long transId);

    List<TransactionDTO> findAllBySenderCardAndTypeTransOrderByCreatedAtDesc(long senderCard, int typeTrans);

    List<TransactionDTO> findAllByReceiverCardAndTypeTransOrderByCreatedAtDesc(long receiverCard, int typeTrans);

    TransactionDTO findFirstBySenderCardAndReceiverCard(long senderCard, long receiverCard);

    List<TransactionDTO> findAllBySenderCardOrReceiverCardAndTypeTransOrderByCreatedAtDesc(long senderCard, long receiverCard, int typeTrans);
}
