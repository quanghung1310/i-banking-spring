package com.backend.process;

import com.backend.constants.ActionConstant;
import com.backend.dto.*;
import com.backend.mapper.UserMapper;
import com.backend.model.Account;
import com.backend.model.request.CreateDebtorRequest;
import com.backend.model.request.CreateReminderRequest;
import com.backend.model.request.RegisterRequest;
import com.backend.model.request.TransactionRequest;
import com.backend.service.impl.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserProcess {
    private static final Logger logger = LogManager.getLogger(UserProcess.class);


    public static UserDTO createUser(String logId, RegisterRequest request, String userName) {
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

    public static ReminderDTO createReminder(String logId, CreateReminderRequest request, Timestamp currentTime) {
        return ReminderDTO.builder()
                .createdAt(currentTime)
                .isActive(1)
                .nameReminisce(request.getNameReminisce())
                .userId(request.getUserId())
                .cardNumber(request.getCardNumber())
                .merchantId(request.getMerchantId())
                .updatedAt(currentTime)
                .type(request.getType())
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

    public static DebtDTO createDebt(String logId, CreateDebtorRequest request, Timestamp currentTime) {
        return DebtDTO.builder()
                .createdAt(currentTime)
                .action(ActionConstant.INIT.getValue())
                .cardNumber(request.getCardNumber())
                .content(request.getContent())
                .debtorId(request.getDebtorId())
                .isActive(1)
                .userId(request.getUserId())
                .updatedAt(currentTime)
                .amount(request.getAmount())
                .build();
    }
}
