package com.backend.service;

import com.backend.dto.ReminderDTO;
import com.backend.dto.UserDTO;
import com.backend.model.Account;
import com.backend.model.request.*;
import com.backend.model.response.DebtorResponse;
import com.backend.model.response.TransactionResponse;
import com.backend.model.response.UserResponse;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    List<Account> getUsers(String logId, int type, long userId);

    UserResponse getUser(String logId, String userName);

    ReminderDTO createReminder(String logId, CreateReminderRequest request, long userId, String name);

    UserResponse getReminders(String logId, long userId, int type, Long cardNumber);

    UserResponse queryAccount(String logId, long cardNumber, long merchantId, int typeAccount, boolean isBalance);

    long createDebtor(String logId, CreateDebtorRequest request);

    DebtorResponse getDebts(String logId, long userId, int action, int type);

    TransactionResponse transaction(String logId, TransactionRequest transactionRequest);

    long insertTransaction(String logId, TransferRequest request, long merchantId, long userId, String cardName);

    long deleteDebt(String logId, DeleteDebtRequest request);

    Optional<ReminderDTO> getReminder(long id);

    ReminderDTO saveReminder(ReminderDTO reminderDTO);

}
