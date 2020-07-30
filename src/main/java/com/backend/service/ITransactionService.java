package com.backend.service;

import com.backend.dto.TransactionDTO;
import com.backend.model.Transaction;
import com.backend.model.request.transaction.TransactionRequest;
import com.backend.model.response.TransactionsResponse;

public interface ITransactionService {
    TransactionsResponse getTransactions(String logId, long cardNumber, String typeTrans);

    long insertTransaction(String logId, TransactionRequest request);

    Transaction saveTransaction(TransactionDTO transactionDTO);

    Transaction getByTransIdAndType(long transId, int type, String status);

    TransactionDTO getByTransIdAndTypeAndAction(long transId, int type, String statuc);
}
