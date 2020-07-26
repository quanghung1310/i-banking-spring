package com.backend.service;

import com.backend.dto.AccountPaymentDTO;

public interface IAccountPaymentService {
    AccountPaymentDTO updateBalance(String logId, long id, long newBalance);
    AccountPaymentDTO getAccountByUserId(long userId);
}
