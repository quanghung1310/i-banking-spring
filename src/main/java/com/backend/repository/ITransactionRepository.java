package com.backend.repository;

import com.backend.dto.TransactionDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface ITransactionRepository extends CrudRepository<TransactionDTO, Long> {
    TransactionDTO findFirstByTransId(long transId);

    List<TransactionDTO> findAllBySenderCardAndTypeTransOrderByCreatedAtDesc(long senderCard, int typeTrans);

    List<TransactionDTO> findAllByReceiverCardAndTypeTransOrderByCreatedAtDesc(long receiverCard, int typeTrans);

    List<TransactionDTO> findAllBySenderCardOrReceiverCardOrderByCreatedAtDesc(long senderCard, long receiverCard);

    @Query(value = "select * from transactions " +
                    " where (sender_card = :cardNumber or receiver_card = :cardNumber) and type_trans = :typeDebt " +
                    " order by created_at desc", nativeQuery = true)
    List<TransactionDTO> findAllByCardNumberAndTypeTrans(@Param("cardNumber") long cardNumber,
                                                         @Param("typeDebt") int typeDebt);

    @Query(value = "SELECT * from transactions where merchant_id = :merchantId and created_at >= cast(:beginTime as timestamp) and created_at <= cast(:endTime as timestamp)", nativeQuery = true)
    List<TransactionDTO> findAllByMerchantIdAndCreatedAtBetween(@Param("merchantId") int merchantId,
                                                                @Param("beginTime") String beginTime,
                                                                @Param("endTime") String endTime);

    @Query(value = "SELECT * from transactions where merchant_id != :merchantId and created_at >= cast(:beginTime as timestamp) and created_at <= cast(:endTime as timestamp)", nativeQuery = true)
    List<TransactionDTO> findAllByNotMyBankAndCreatedAtBetween(@Param("merchantId") int merchantId,
                                                                @Param("beginTime") String beginTime,
                                                                @Param("endTime") String endTime);

    TransactionDTO findByTransIdAndTypeTransAndStatus(long transId, int typeTrans, String status);
}
