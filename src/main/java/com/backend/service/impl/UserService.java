package com.backend.service.impl;

import com.backend.dto.AccountPaymentDTO;
import com.backend.dto.AccountSavingDTO;
import com.backend.mapper.UserMapper;
import com.backend.model.Account;
import com.backend.repository.IAccountPaymentRepository;
import com.backend.repository.IAccountSavingRepository;
import com.backend.service.IUserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements IUserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Autowired
    IAccountPaymentRepository accountPaymentRepository;

    @Autowired
    IAccountSavingRepository accountSavingRepository;

    @Override
    public List<Account> getUsers(String logId, int type, long userId) {
        List<AccountPaymentDTO> accountPaymentDTOS;
        List<AccountSavingDTO> accountSavingDTOS;
        List<Account> accounts = new ArrayList<>();
        switch (type) {
            case 1:
                accountPaymentDTOS = accountPaymentRepository.findAllByUserId(userId);
                accountPaymentDTOS.forEach(account -> accounts.add(UserMapper.toModelAccount(
                        AccountSavingDTO.builder().build(),
                        account,
                        1)));
                break;
            case 2:
                accountSavingDTOS = accountSavingRepository.findAllByUserId(userId);
                accountSavingDTOS.forEach(account -> accounts.add(UserMapper.toModelAccount(
                        account,
                        AccountPaymentDTO.builder().build(),
                        2)));
                break;
            default:
                accountPaymentDTOS = accountPaymentRepository.findAllByUserId(userId);
                accountPaymentDTOS.forEach(account -> accounts.add(UserMapper.toModelAccount(
                        AccountSavingDTO.builder().build(),
                        account,
                        1)));

                accountSavingDTOS = accountSavingRepository.findAllByUserId(userId);
                accountSavingDTOS.forEach(account -> accounts.add(UserMapper.toModelAccount(
                        account,
                        AccountPaymentDTO.builder().build(),
                        2)));
                break;
        }

        return accounts;
    }
}
