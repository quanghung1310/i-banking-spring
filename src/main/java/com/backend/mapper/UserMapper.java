package com.backend.mapper;

import com.backend.constants.StringConstant;
import com.backend.dto.AccountPaymentDTO;
import com.backend.dto.AccountSavingDTO;
import com.backend.model.Account;
import com.backend.util.DataUtil;

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

    public static Account toModelAccount(AccountSavingDTO accountSavingDTO, AccountPaymentDTO accountPaymentDTO, int type) {
        if (type == 1) {
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
                    .type(type)
                    .updatedAt(DataUtil.convertTimeWithFormat(accountPaymentDTO.getUpdatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                    .balance(accountPaymentDTO.getBalance())
                    .userId(accountPaymentDTO.getUserId())
                    .build();
        } else {
            if (accountSavingDTO == null) {
                return null;
            }
            return Account.builder()
                    .cardName(accountSavingDTO.getCardName())
                    .cardNumber(accountSavingDTO.getCardNumber())
                    .closeDate(DataUtil.convertTimeWithFormat(accountSavingDTO.getCloseDate().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                    .createdAt(DataUtil.convertTimeWithFormat(accountSavingDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                    .description(accountSavingDTO.getDescription())
                    .id(accountSavingDTO.getId())
                    .type(type)
                    .updatedAt(DataUtil.convertTimeWithFormat(accountSavingDTO.getUpdatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                    .balance(accountSavingDTO.getBalance())
                    .userId(accountSavingDTO.getUserId())
                    .build();
        }
    }

}
