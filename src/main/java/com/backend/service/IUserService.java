package com.backend.service;

import com.backend.dto.ReminderDTO;
import com.backend.model.Account;
import com.backend.model.request.debt.CreateDebtorRequest;
import com.backend.model.request.debt.DeleteDebtRequest;
import com.backend.model.request.debt.PayDebtRequest;
import com.backend.model.request.reminder.CreateReminderRequest;
import com.backend.model.request.transaction.TransactionRequest;
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

    DebtorResponse createDebtor(String logId, CreateDebtorRequest request, long userId);

    DebtorResponse getDebts(String logId, long userId, int action, int type);

    DebtorResponse deleteDebt(String logId, DeleteDebtRequest request, long userId);

    Optional<ReminderDTO> getReminder(long id);

    ReminderDTO saveReminder(ReminderDTO reminderDTO);

    TransactionResponse payDebt(String logId, PayDebtRequest request, long userId);
}
