package com.backend.service;

import com.backend.dto.ReminderDTO;
import com.backend.dto.UserDTO;
import com.backend.model.Account;
import com.backend.model.request.debt.CreateDebtorRequest;
import com.backend.model.request.debt.DeleteDebtRequest;
import com.backend.model.request.debt.PayDebtRequest;
import com.backend.model.request.reminder.CreateReminderRequest;
import com.backend.model.response.DebtorResponse;
import com.backend.model.response.TransactionResponse;
import com.backend.model.response.UserResponse;
import org.bouncycastle.openpgp.PGPException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.List;
import java.util.Optional;

public interface IUserService {
    List<Account> getUsers(String logId, int type, long userId);

    UserResponse getUser(String logId, String userName);

    ReminderDTO createReminder(String logId, CreateReminderRequest request, long userId, String name);

    UserResponse getReminders(String logId, long userId, int type, Long cardNumber);

    UserResponse queryAccount(String logId, long cardNumber, long merchantId, int typeAccount, boolean isBalance, String token) throws Exception;

    DebtorResponse createDebtor(String logId, CreateDebtorRequest request, long userId);

    DebtorResponse getDebts(String logId, long userId, int action, int type);

    DebtorResponse deleteDebt(String logId, DeleteDebtRequest request, long userId);

    Optional<ReminderDTO> getReminder(long id);

    ReminderDTO saveReminder(ReminderDTO reminderDTO);

    TransactionResponse payDebt(String logId, PayDebtRequest request, long userId);

    String updatePassword(String logId, String newPass, String userName);

    String forgotPassword(String logId, String userName);

    UserDTO getByCardNumber(String logId, long cardNumber);
}
