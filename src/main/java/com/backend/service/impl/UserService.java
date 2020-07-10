package com.backend.service.impl;

import com.backend.dto.AccountPaymentDTO;
import com.backend.dto.AccountSavingDTO;
import com.backend.dto.TransactionDTO;
import com.backend.dto.UserDTO;
import com.backend.mapper.UserMapper;
import com.backend.model.Account;
import com.backend.model.request.TransactionRequest;
import com.backend.model.response.TransactionResponse;
import com.backend.model.response.UserResponse;
import com.backend.process.UserProcess;
import com.backend.repository.IAccountPaymentRepository;
import com.backend.repository.IAccountSavingRepository;
import com.backend.repository.ITransactionRepository;
import com.backend.repository.IUserRepository;
import com.backend.service.IUserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements IUserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Autowired
    IAccountPaymentRepository accountPaymentRepository;

    @Autowired
    IAccountSavingRepository accountSavingRepository;

    @Autowired
    IUserRepository userRepository;

    @Autowired
    ITransactionRepository transactionRepository;

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

    @Override
    public UserResponse login(String logId, String userName, String password) {
        List<AccountPaymentDTO> accountPaymentDTOS;
        List<AccountSavingDTO> accountSavingDTOS;
        List<Account> accounts = new ArrayList<>();
        UserDTO userDTO = userRepository.findFirstByUserNameAndPassword(userName, password);
        if (userDTO == null) {
            return UserResponse.builder().build();
        } else {
            long userId = userDTO.getId();
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

            return UserMapper.toModelUser(userDTO, accounts);
        }
    }

    @Override
    public TransactionResponse transaction(String logId, TransactionRequest request) {
        Timestamp currentTime = new Timestamp(request.getRequestTime());
        Long cardNumber = request.getCardNumber();
        List<AccountPaymentDTO> accounts = (List<AccountPaymentDTO>) accountPaymentRepository.findAll();
        String cardName = null;
        for (AccountPaymentDTO account : accounts) {
            if (account.getCardNumber() == cardNumber) {
                cardName = account.getCardName();
            }
        }

        if (cardName == null) {
            logger.warn("{}| Card number is not exist!", logId);
            return null;
        }

        TransactionDTO transactionDTO = transactionRepository.save(UserProcess.createTransaction(logId, currentTime, request, cardName));
        Long transactionId = transactionDTO.getId();
        logger.info("{}| Save transaction - {}:{}", logId, transactionId, transactionId == null ? "false" : "success");

        return TransactionResponse.builder()
                .transId(transactionDTO.getTransId())
                .cardName(transactionDTO.getCardName())
                .cardNumber(transactionDTO.getCardNumber())
                .amount(transactionDTO.getAmount())
                .typeFee(transactionDTO.getTypeFee())
                .fee(transactionDTO.getFee())
                .content(transactionDTO.getContent())
                .createDate(transactionDTO.getCreatedAt())
                .merchantId(transactionDTO.getMerchantId())
                .status(transactionDTO.getStatus())
                .build();
    }
}
