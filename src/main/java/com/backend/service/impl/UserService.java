package com.backend.service.impl;

import com.backend.constants.StringConstant;
import com.backend.dto.AccountPaymentDTO;
import com.backend.dto.UserDTO;
import com.backend.mapper.UserMapper;
import com.backend.model.Account;
import com.backend.model.request.RegisterRequest;
import com.backend.model.response.RegisterResponse;
import com.backend.process.UserProcess;
import com.backend.repository.IAccountPaymentRepository;
import com.backend.repository.IUserRepository;
import com.backend.service.IUserService;
import com.backend.util.DataUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class UserService implements IUserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Autowired
    IUserRepository userRepository;

    @Autowired
    IAccountPaymentRepository accountPaymentRepository;

    @Override
    public RegisterResponse register(String logId, RegisterRequest request) {
        Timestamp currentTime = new Timestamp(request.getRequestTime());
        UserDTO userDTO = userRepository.save(UserProcess.createUser(logId, request));
        Long userId = userDTO.getId();
        logger.info("{}| Save user - {}: {}", logId, userId, userId == null ? "false" : "success");

        AccountPaymentDTO accountPaymentDTO = accountPaymentRepository.save(UserProcess.createAccountPayment(logId, userId, request.getAdminId(), request.getCardName(), currentTime, request.getRequestTime()));
        Long accountId = accountPaymentDTO.getId();
        logger.info("{}| Save account payment - {}:{}", logId, accountId, accountId == null ? "false" : "success");
        Account account = UserMapper.toModelRegister(accountPaymentDTO);

        return RegisterResponse.builder()
                .userName(userDTO.getUserName())
                .password(userDTO.getPassword())
                .createDate(DataUtil.convertTimeWithFormat(userDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                .account(account)
                .build();
    }
}
