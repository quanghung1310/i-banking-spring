package com.backend.process;

import com.backend.constants.ActionConstant;
import com.backend.dto.TransactionDTO;
import com.backend.model.request.transaction.TransactionRequest;

import java.sql.Timestamp;
import java.util.Random;

public class TransactionProcess {

    public static TransactionDTO buildTransaction(Timestamp currentTime, TransactionRequest request, long senderCard, long fee) {
        return createTrans(
                senderCard,
                request.getReceiverCard(),
                request.getAmount(),
                request.getTypeFee(),
                1, //send/receiver
                request.getMerchantId(),
                request.getContent(),
                ActionConstant.INIT.toString(),
                currentTime,
                currentTime,
                fee);
    }

    public static long newBalance(boolean isTransfer, boolean isPayFee, long fee, long amount, long currentBalance) {
        long balance;
        if (isTransfer) { //chuyển tiền
            balance = currentBalance - amount;
        } else { //nhận tiền
            balance = currentBalance + amount;
        }
        if (isPayFee) { //true: tự trả fee
            balance -= fee;
        }
        return balance;
    }

    public static TransactionDTO createTrans(long senderCard, long receiverCard, long amount, int typeFee, int typeTrans, long merchantId,
                                             String content, String status, Timestamp create, Timestamp update, long fee) {
        return TransactionDTO.builder()
                .transId(1000000000L + (long)(new Random().nextDouble() * 999999999L))
                .senderCard(senderCard)
                .receiverCard(receiverCard)
                .amount(amount)
                .typeFee(typeFee)
                .typeTrans(typeTrans)
                .merchantId((int) merchantId)
                .content(content)
                .status(status)
                .createdAt(create)
                .updatedAt(update)
                .fee(fee)
                .build();
    }

}
