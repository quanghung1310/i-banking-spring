package com.backend.process;

import com.backend.constants.ActionConstant;
import com.backend.constants.StatusConstant;
import com.backend.dto.*;
import com.backend.mapper.UserMapper;
import com.backend.model.Account;
import com.backend.model.request.debt.CreateDebtorRequest;
import com.backend.model.request.employee.RegisterRequest;
import com.backend.model.request.transaction.TransactionRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserProcess {
    private static final Logger logger = LogManager.getLogger(UserProcess.class);

    public static UserDTO createUser(RegisterRequest request, String userName, String role) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 15;
        Random random = new Random();

        String name = request.getName();
        String password = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return UserDTO.builder()
                .name(name)
                .phone(request.getPhone())
                .email(request.getEmail())
                .userName(userName)
                .password(password)
                .lastPassword(password)
                .createdAt(new Timestamp(request.getRequestTime()))
                .updatedAt(new Timestamp(request.getRequestTime()))
                .role(role)
                .build();
    }
    public static AccountPaymentDTO createAccountPayment(String logId, List<AccountPaymentDTO> accounts, long userId, long adminId, String cardName, Timestamp currentTime, long milliseconds) {
        boolean flagCard = false;
        long cardNumber = 0L;
        while (!flagCard) {
            cardNumber = 1000000000000000L + (long)(new Random().nextDouble() * 999999999999999L);
            for (AccountPaymentDTO account : accounts) {
                if (account.getCardNumber() == cardNumber) {
                    continue;
                }
            }
            flagCard = true;
        }
        return AccountPaymentDTO.builder()
                .userId(userId)
                .cardName(cardName)
                .cardNumber(cardNumber)
                .balance(0L)
                .openDate(currentTime)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .closeDate(new Timestamp(milliseconds + (31536000000L*4)))
                .admin(adminId)
                .build();
    }


    public static List<Account> formatToAccounts(String logId, List<AccountPaymentDTO> accountPaymentDTOS, List<AccountSavingDTO> accountSavingDTOS, boolean isQueryBalance) {
        List<Account> accounts = new ArrayList<>();

        if (accountPaymentDTOS.size() > 0) {
            accountPaymentDTOS.forEach(account -> accounts.add(UserMapper.toModelAccount(
                    AccountSavingDTO.builder().build(),
                    account,
                    1,
                    isQueryBalance)));
            logger.info("{}| Mapping account payment to account success with size: {}", logId, accountPaymentDTOS.size());
        }
        if (accountSavingDTOS.size() > 0) {
            accountSavingDTOS.forEach(account -> accounts.add(UserMapper.toModelAccount(
                    account,
                    AccountPaymentDTO.builder().build(),
                    2,
                    isQueryBalance)));
            logger.info("{}| Mapping account saving to account success with size: {}", logId, accountSavingDTOS.size());
        }
        return accounts;
    }

    public static DebtDTO createDebt(int action, CreateDebtorRequest request, Timestamp currentTime, long userId, long debtorId) {
        return DebtDTO.builder()
                .createdAt(currentTime)
                .action(action)
                .cardNumber(request.getCardNumber())
                .content(request.getContent())
                .isActive(1)
                .userId(userId)
                .updatedAt(currentTime)
                .amount(request.getAmount())
                .build();
    }

}
