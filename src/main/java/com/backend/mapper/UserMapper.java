package com.backend.mapper;

import com.backend.constants.StringConstant;
import com.backend.dto.*;
import com.backend.model.Account;
import com.backend.model.Debt;
import com.backend.model.Transaction;
import com.backend.model.response.EmployeeResponse;
import com.backend.model.response.TransactionResponse;
import com.backend.model.response.UserResponse;
import com.backend.util.DataUtil;

import java.util.List;
import java.util.Optional;

public class UserMapper {
    public static Account toModelRegister(AccountPaymentDTO accountPaymentDTO) {
        if (accountPaymentDTO == null) {
            return null;
        }
        return Account.builder()
                .cardName(accountPaymentDTO.getCardName())
                .cardNumber(accountPaymentDTO.getCardNumber())
                .closeDate(DataUtil.convertTimeWithFormat(accountPaymentDTO.getCloseDate().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                .createdAt(DataUtil.convertTimeWithFormat(accountPaymentDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                .description(accountPaymentDTO.getDescription())
                .id(accountPaymentDTO.getId())
                .type("payment")
                .updatedAt(DataUtil.convertTimeWithFormat(accountPaymentDTO.getUpdatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                .build();
    }

    public static Account toModelReminder(ReminderDTO reminderDTO, String cardName) {
        if (reminderDTO == null) {
            return null;
        }
        return Account.builder()
                .cardName(cardName)
                .cardNumber(reminderDTO.getCardNumber())
                .typeReminder(reminderDTO.getType() == 1 ? "send" : "debt")
                .reminderId(reminderDTO.getId())
                .merchantId(reminderDTO.getMerchantId())
                .reminderName(reminderDTO.getNameReminisce())
                .build();
    }
    public static Account toModelAccount(AccountSavingDTO accountSavingDTO, AccountPaymentDTO accountPaymentDTO, int type, boolean isQueryBalance) {
        Account account = Account.builder().build();
        if (type == 1) {
            if (accountPaymentDTO == null) {
                return null;
            }
            if (isQueryBalance) {
                account.setBalance(accountPaymentDTO.getBalance());
                account.setCloseDate(DataUtil.convertTimeWithFormat(accountPaymentDTO.getCloseDate().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss));
                account.setCreatedAt(DataUtil.convertTimeWithFormat(accountPaymentDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss));
                account.setUpdatedAt(DataUtil.convertTimeWithFormat(accountPaymentDTO.getUpdatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss));
            }
            account.setCardNumber(accountPaymentDTO.getCardNumber());
            account.setCardName(accountPaymentDTO.getCardName());
            account.setDescription(accountPaymentDTO.getDescription());
            account.setId(accountPaymentDTO.getId());
            account.setType("payment");
            account.setUserId(accountPaymentDTO.getUserId());
        } else {
            if (accountSavingDTO == null) {
                return null;
            }
            if (isQueryBalance) {
                account.setBalance(accountPaymentDTO.getBalance());
                account.setCloseDate(DataUtil.convertTimeWithFormat(accountSavingDTO.getCloseDate().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss));
                account.setCreatedAt(DataUtil.convertTimeWithFormat(accountSavingDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss));
                account.setUpdatedAt(DataUtil.convertTimeWithFormat(accountSavingDTO.getUpdatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss));
            }

            account.setCardName(accountSavingDTO.getCardName());
            account.setCardNumber(accountSavingDTO.getCardNumber());
            account.setDescription(accountSavingDTO.getDescription());
            account.setId(accountSavingDTO.getId());
            account.setType("saving");
            account.setUserId(accountSavingDTO.getUserId());
        }
        return account;
    }

    public static UserResponse toModelUser(UserDTO userDTO, List<Account> accounts) {
        UserResponse userResponse = UserResponse.builder().build();
        if (userDTO == null) {
            return null;
        }
        userResponse.setAccount(accounts);
        userResponse.setCreatedAt(DataUtil.convertTimeWithFormat(userDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss));
        userResponse.setEmail(userDTO.getEmail());
        userResponse.setId(userDTO.getId());
        userResponse.setName(userDTO.getName());
        userResponse.setPhone(userDTO.getPhone());
        userResponse.setRole(userDTO.getRole());
        return userResponse;
    }

    public static Debt toModelDebt(DebtDTO debtDTO, Optional<UserDTO> user) {
        if (!user.isPresent()) {
            return null;
        } else {
            UserDTO userDTO = user.get();
            return Debt.builder()
                    .id(debtDTO.getId())
                    .action(debtDTO.getAction())
                    .amount(debtDTO.getAmount())
                    .content(debtDTO.getContent())
                    .createdAt(DataUtil.convertTimeWithFormat(debtDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                    .partnerEmail(userDTO.getEmail())
                    .partnerName(userDTO.getName())
                    .partnerId(userDTO.getId())
                    .partnerPhone(userDTO.getPhone())
                    .updatedAt(DataUtil.convertTimeWithFormat(debtDTO.getUpdatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                    .build();
        }
    }

    public static EmployeeResponse toModelEmployee(UserDTO userDTO) {
        EmployeeResponse employeeResponse = EmployeeResponse.builder().build();
        if (userDTO == null) {
            return null;
        }
        employeeResponse.setCreatedAt(DataUtil.convertTimeWithFormat(userDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss));
        employeeResponse.setEmail(userDTO.getEmail());
        employeeResponse.setId(userDTO.getId());
        employeeResponse.setName(userDTO.getName());
        employeeResponse.setPhone(userDTO.getPhone());
        employeeResponse.setRole(userDTO.getRole());
        employeeResponse.setUserName(userDTO.getUserName());
        return employeeResponse;
    }
}
