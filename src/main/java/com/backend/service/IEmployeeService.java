package com.backend.service;

import com.backend.model.request.bank.DepositRequest;
import com.backend.model.request.employee.RegisterRequest;
import com.backend.model.response.DepositResponse;
import com.backend.model.response.RegisterResponse;

public interface IEmployeeService {
    RegisterResponse register(String logId, RegisterRequest request, long employeeId);

    DepositResponse deposit(String logId, DepositRequest request);
}
