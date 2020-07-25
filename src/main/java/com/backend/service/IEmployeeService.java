package com.backend.service;

import com.backend.model.request.DepositRequest;
import com.backend.model.request.RegisterRequest;
import com.backend.model.response.DepositResponse;
import com.backend.model.response.RegisterResponse;
import com.backend.model.response.TransactionsResponse;

public interface IEmployeeService {
    RegisterResponse register(String logId, RegisterRequest request);

    DepositResponse deposit(String logId, DepositRequest request);
}
