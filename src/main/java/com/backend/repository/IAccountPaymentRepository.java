package com.backend.repository;

import com.backend.dto.AccountPaymentDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.List;

public interface IAccountPaymentRepository extends CrudRepository<AccountPaymentDTO, Long> {
    AccountPaymentDTO findFirstByCardNumber(long cardNumber);

//    @Query("select a from account_payment a, user_bank u where u.id = a.user_id and u.user_name = ?1")
//    AccountPaymentDTO getAccountByUserName(String userName);
    AccountPaymentDTO findFirstByUserId(long userId);

    List<AccountPaymentDTO> findAllByUserId(long userId);
}
