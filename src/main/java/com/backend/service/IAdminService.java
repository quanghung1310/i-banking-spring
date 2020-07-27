package com.backend.service;

import com.backend.model.request.employee.RegisterRequest;
import com.backend.model.response.RegisterResponse;

public interface IAdminService {
    RegisterResponse registerEmployee(String logId, RegisterRequest request, String role);

}
