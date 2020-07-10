package com.backend.service;

import com.backend.model.Account;
import com.backend.model.request.TransactionRequest;
import com.backend.model.response.TransactionResponse;
import com.backend.model.response.UserResponse;

import java.util.List;

public interface IUserService {
    List<Account> getUsers(String logId, int type, long userId);

    UserResponse login(String logId, String userName, String password);

    TransactionResponse transaction(String logId, TransactionRequest transactionRequest);
}
