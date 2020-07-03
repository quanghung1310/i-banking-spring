package com.backend.service;

import com.backend.model.request.RegisterRequest;
import com.backend.model.response.RegisterResponse;

public interface IUserService {
    RegisterResponse register(String logId, RegisterRequest request);
}
