package com.backend.mapper;

import com.backend.constants.StringConstant;
import com.backend.dto.AccountPaymentDTO;
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
}
