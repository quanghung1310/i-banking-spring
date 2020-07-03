package com.backend.process;

import com.backend.dto.AccountPaymentDTO;
import com.backend.dto.UserDTO;
import com.backend.model.request.RegisterRequest;

import java.sql.Timestamp;
import java.util.Random;

public class UserProcess {
    public static UserDTO createUser(String logId, RegisterRequest request) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 15;
        Random random = new Random();

        String name = request.getName();
        String userName = name.toLowerCase().replaceAll("\\s+","");
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
    public static AccountPaymentDTO createAccountPayment(String logId, long userId, long adminId, String cardName, Timestamp currentTime, long milliseconds) {
        long cardNumber = new Random().nextInt(15);
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
}
