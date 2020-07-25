package com.backend.service.impl;

import com.backend.dto.AccountPaymentDTO;
import com.backend.dto.TransactionDTO;
import com.backend.mapper.TransactionMapper;
import com.backend.model.Transaction;
import com.backend.model.response.TransactionsResponse;
import com.backend.repository.IAccountPaymentRepository;
import com.backend.repository.ITransactionRepository;
import com.backend.repository.IUserRepository;
import com.backend.service.ITransactionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService implements ITransactionService {
    private static final Logger logger = LogManager.getLogger(EmployeeService.class);

    @Value( "${trans.send}")
    private String send;

    @Value( "${trans.debt}" )
    private String debt;

    @Value( "${trans.receiver}" )
    private String receiver;

    @Value( "${trans.all}" )
    private String all;

    private IUserRepository userRepository;
    private IAccountPaymentRepository accountPaymentRepository;
    private ITransactionRepository transactionRepository;

    @Autowired
    public TransactionService(IUserRepository userRepository,
                           IAccountPaymentRepository accountPaymentRepository,
                           ITransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountPaymentRepository = accountPaymentRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public TransactionsResponse getTransactions(String logId, long cardNumber, String typeTrans) {
        List<TransactionDTO> transactionDTOS;
        List<Transaction> transactions = new ArrayList<>();
        if (typeTrans.equals(send)) {
            transactionDTOS = transactionRepository.findAllBySenderCard(cardNumber);
            if (transactionDTOS.size()  <= 0) {
                return TransactionsResponse.builder().build();
            }
            transactionDTOS.forEach(transactionDTO -> transactions.add(TransactionMapper.toModelTransaction(
                    transactionDTO,
                    transactionDTO.getReceiverCard(),
                    accountPaymentRepository.findFirstByCardNumber(transactionDTO.getReceiverCard()).getCardName()
            )));
        } else if (typeTrans.equals(receiver)) {

        } else if (typeTrans.equals(debt)){

        } else if (typeTrans.equals(all)) {

        } else {
            return null;
        }
        AccountPaymentDTO paymentDTO = accountPaymentRepository.findFirstByCardNumber(cardNumber);
        return TransactionsResponse.builder()
                .userId(paymentDTO.getUserId())
                .cardNumber(paymentDTO.getCardNumber())
                .cardName(paymentDTO.getCardName())
                .transactions(transactions)
                .build();
    }
}
