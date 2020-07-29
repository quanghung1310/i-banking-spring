package com.backend.service;

import com.backend.dto.UserDTO;
import com.backend.model.request.employee.RegisterRequest;
import com.backend.model.response.EmployeeResponse;
import com.backend.model.response.RegisterResponse;
import com.backend.model.response.TransMerchantResponse;

import java.sql.Timestamp;
import java.util.List;

public interface IAdminService {
    RegisterResponse registerEmployee(String logId, RegisterRequest request, String role);

    List<EmployeeResponse> getEmployee(String logId, Long employerId, String employer);

    UserDTO findById(long id);

    EmployeeResponse saveEmployee(UserDTO userDTO);

    List<TransMerchantResponse> controlTransaction(String logId, int merchantId, String beginTime, String endTime);
}
