package com.backend.process;

import com.backend.dto.AccountPaymentDTO;
import com.backend.dto.ReminderDTO;
import com.backend.dto.UserDTO;
import com.backend.model.request.CreateReminderRequest;
import com.backend.model.request.RegisterRequest;

import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

public class UserProcess {
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

    public static ReminderDTO createReminder(String logId, CreateReminderRequest request, Timestamp curretnTime) {
        return ReminderDTO.builder()
                .createdAt(curretnTime)
                .isActive(1)
                .nameReminisce(request.getNameReminisce())
                .userId(request.getUserId())
                .cardNumber(request.getCardNumber())
                .merchantId(request.getMerchantId())
                .updatedAt(curretnTime)
                .type(request.getType())
                .build();
    }
}
