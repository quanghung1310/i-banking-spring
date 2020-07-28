package com.backend.mapper;

import com.backend.constants.StringConstant;
import com.backend.dto.TransactionDTO;
import com.backend.model.Transaction;
import com.backend.model.TransactionMerchant;
import com.backend.model.response.TransactionResponse;
import com.backend.util.DataUtil;

public class TransactionMapper {

    private static int TRANS_SENDER = 1;
    private static int TRANS_RECEIVER = 2;

    public static TransactionResponse toModelTransResponse(TransactionDTO transactionDTO, String cardName) {
        if (transactionDTO == null) {
            return null;
        }
        return TransactionResponse.builder()
                .amount(transactionDTO.getAmount())
                .cardName(cardName)
                .content(transactionDTO.getContent())
                .createDate(DataUtil.convertTimeWithFormat(transactionDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                .fee(transactionDTO.getFee())
                .merchantId((long) transactionDTO.getMerchantId())
                .receiverCard(transactionDTO.getReceiverCard())
                .status(transactionDTO.getStatus())
                .transId(transactionDTO.getTransId())
                .typeFee(transactionDTO.getTypeFee())
                .build();
    }

    public static Transaction toModelTransaction(TransactionDTO transactionDTO, long cardNumber, String cardName) {
        if (transactionDTO == null) {
            return null;
        }
        return Transaction.builder()
                .amount(transactionDTO.getAmount())
                .content(transactionDTO.getContent())
                .cardNumber(cardNumber)
                .cardName(cardName)
                .status(transactionDTO.getStatus())
                .transId(transactionDTO.getTransId())
                .typeFee(transactionDTO.getTypeFee())
                .merchantId(transactionDTO.getMerchantId())
                .createDate(DataUtil.convertTimeWithFormat(transactionDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                .fee(transactionDTO.getFee())
                .build();
    }

    public static Transaction toModelTransactionDebt(TransactionDTO transactionDTO, long cardNumber, String cardName, int result) {
        if (transactionDTO == null) {
            return null;
        }
        return Transaction.builder()
                .amount(transactionDTO.getAmount()*result)
                .content(transactionDTO.getContent())
                .cardNumber(cardNumber)
                .cardName(cardName)
                .status(transactionDTO.getStatus())
                .transId(transactionDTO.getTransId())
                .typeFee(transactionDTO.getTypeFee())
                .merchantId(transactionDTO.getMerchantId())
                .createDate(DataUtil.convertTimeWithFormat(transactionDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                .fee(transactionDTO.getFee())
                .build();
    }

    public static TransactionMerchant toModelTransMerchant(TransactionDTO transactionDTO, long cardNumber, String cardName, int type) {
        if (transactionDTO == null) {
            return null;
        }

        //auto lh-bank la receiver
        TransactionMerchant transactionMerchant = TransactionMerchant.builder()
                .amount(transactionDTO.getAmount())
                .createDate(DataUtil.convertTimeWithFormat(transactionDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                .merchantId(transactionDTO.getMerchantId())
                .receiverCard(cardNumber)
                .receiverName(cardName)
                .senderCard(transactionDTO.getSenderCard())
                .senderName(transactionDTO.getCardName())
                .status(transactionDTO.getStatus())
                .content(transactionDTO.getContent())
                .fee(transactionDTO.getFee())
                .typeFee(transactionDTO.getTypeFee())
                .build();
        if (type == TRANS_SENDER) {
            transactionMerchant.setSenderCard(cardNumber);
            transactionMerchant.setSenderName(cardName);
            transactionMerchant.setReceiverCard(transactionDTO.getReceiverCard());
            transactionMerchant.setReceiverName(transactionDTO.getCardName());
        }
        return transactionMerchant;
    }
}
