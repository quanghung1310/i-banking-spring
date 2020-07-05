package com.backend.service;

import com.backend.model.Account;
import com.backend.model.request.CreateDebtorRequest;
import com.backend.model.request.CreateReminderRequest;
import com.backend.model.response.UserResponse;

import java.util.List;

public interface IUserService {
    List<Account> getUsers(String logId, int type, long userId);

    UserResponse login(String logId, String userName, String password);

    long createReminder(String logId, CreateReminderRequest request);

    UserResponse getReminders(String logId, long userId, int type, Long cardNumber);

    UserResponse queryAccount(String logId, long cardNumber, long merchantId);

    long createDebtor(String logId, CreateDebtorRequest request);

}
