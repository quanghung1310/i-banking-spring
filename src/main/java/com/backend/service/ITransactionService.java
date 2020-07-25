package com.backend.service;

import com.backend.model.response.TransactionsResponse;

public interface ITransactionService {
    TransactionsResponse getTransactions(String logId, long cardNumber, String typeTrans);

}
