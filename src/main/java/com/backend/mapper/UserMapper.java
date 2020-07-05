package com.backend.mapper;

import com.backend.constants.StringConstant;
import com.backend.dto.AccountPaymentDTO;
import com.backend.dto.AccountSavingDTO;
import com.backend.dto.UserDTO;
import com.backend.model.Account;
import com.backend.model.response.UserResponse;
import com.backend.util.DataUtil;

import java.util.List;

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
                .type(1)
                .updatedAt(DataUtil.convertTimeWithFormat(accountPaymentDTO.getUpdatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
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
            }
            account.setCardName(accountPaymentDTO.getCardName());
            account.setCardNumber(accountPaymentDTO.getCardNumber());
            account.setCloseDate(DataUtil.convertTimeWithFormat(accountPaymentDTO.getCloseDate().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss));
            account.setCreatedAt(DataUtil.convertTimeWithFormat(accountPaymentDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss));
            account.setDescription(accountPaymentDTO.getDescription());
            account.setId(accountPaymentDTO.getId());
            account.setType(type);
            account.setUpdatedAt(DataUtil.convertTimeWithFormat(accountPaymentDTO.getUpdatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss));
            account.setUserId(accountPaymentDTO.getUserId());
        } else {
            if (accountSavingDTO == null) {
                return null;
            }
            if (isQueryBalance) {
                account.setBalance(accountPaymentDTO.getBalance());
            }

            account.setCardName(accountSavingDTO.getCardName());
            account.setCardNumber(accountSavingDTO.getCardNumber());
            account.setCloseDate(DataUtil.convertTimeWithFormat(accountSavingDTO.getCloseDate().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss));
            account.setCreatedAt(DataUtil.convertTimeWithFormat(accountSavingDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss));
            account.setDescription(accountSavingDTO.getDescription());
            account.setId(accountSavingDTO.getId());
            account.setType(type);
            account.setUpdatedAt(DataUtil.convertTimeWithFormat(accountSavingDTO.getUpdatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss));
            account.setUserId(accountSavingDTO.getUserId());
        }
        return account;
    }

    public static UserResponse toModelUser(UserDTO userDTO, List<Account> accounts) {
        if (userDTO == null) {
            return null;
        }
        return UserResponse.builder()
                .account(accounts)
                .createdAt(DataUtil.convertTimeWithFormat(userDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                .email(userDTO.getEmail())
                .id(userDTO.getId())
                .name(userDTO.getName())
                .password(userDTO.getPassword())
                .phone(userDTO.getPhone())
                .userName(userDTO.getUserName())
                .build();
    }
}
