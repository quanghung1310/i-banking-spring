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
import java.util.List;

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
        String userName = request.getName().toLowerCase().replaceAll("\\s+","");
        List<UserDTO> users = (List<UserDTO>) userRepository.findAll();
        for (UserDTO user : users) {
            if (user.getUserName().equals(userName)) {
                logger.warn("{}| Username - {} was existed!", logId, userName);
                return null;
            }
        }
        logger.warn("{}| Username - {} is not exist!", logId, userName);
        UserDTO userDTO = userRepository.save(UserProcess.createUser(logId, request, userName));
        Long userId = userDTO.getId();

        if (userId == null) {
            logger.warn("{}| Save user false!", logId);
            return null;
        }
        logger.info("{}| Save user - {}: success!", logId, userId);

        List<AccountPaymentDTO> accounts = (List<AccountPaymentDTO>) accountPaymentRepository.findAll();
        AccountPaymentDTO accountPaymentDTO = accountPaymentRepository.save(UserProcess.createAccountPayment(logId, accounts, userId, request.getAdminId(), request.getCardName(), currentTime, request.getRequestTime()));
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
