package com.backend.service.impl;

import com.backend.dto.AccountPaymentDTO;
import com.backend.dto.TransactionDTO;
import com.backend.mapper.TransactionMapper;
import com.backend.model.Transaction;
import com.backend.model.request.transaction.TransactionRequest;
import com.backend.model.response.TransactionsResponse;
import com.backend.process.UserProcess;
import com.backend.repository.IAccountPaymentRepository;
import com.backend.repository.ITransactionRepository;
import com.backend.service.ITransactionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
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

    @Value( "${fee.transfer}" )
    private long fee;

    @Value( "${my.bank.id}" )
    private long myBankId;

    private IAccountPaymentRepository accountPaymentRepository;
    private ITransactionRepository transactionRepository;

    @Autowired
    public TransactionService(IAccountPaymentRepository accountPaymentRepository,
                           ITransactionRepository transactionRepository) {
        this.accountPaymentRepository = accountPaymentRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public TransactionsResponse getTransactions(String logId, long cardNumber, String typeTrans) {
        int typeTransfer = 1;
        int typeDebt = 2;
        List<TransactionDTO> transactionDTOS;
        List<Transaction> transactions = new ArrayList<>();
        AccountPaymentDTO paymentDTO = accountPaymentRepository.findFirstByCardNumber(cardNumber);

        if (typeTrans.equals(send)) {
            transactionDTOS = transactionRepository.findAllBySenderCardAndTypeTransOrderByCreatedAtDesc(cardNumber, typeTransfer);
            if (transactionDTOS.size() <= 0) {
                logger.warn("{}| card number - {} not found transaction!", logId, cardNumber);
                return TransactionsResponse.builder().build();
            }
            transactionDTOS.forEach(transactionDTO -> {
                AccountPaymentDTO accountPaymentDTO = accountPaymentRepository.findFirstByCardNumber(transactionDTO.getReceiverCard());
                transactions.add(TransactionMapper.toModelTransaction(
                        transactionDTO,
                        transactionDTO.getReceiverCard(),
                        accountPaymentDTO == null ? transactionDTO.getCardName() : accountPaymentDTO.getCardName()));
            });
        } else if (typeTrans.equals(receiver)) {
            transactionDTOS = transactionRepository.findAllByReceiverCardAndTypeTransOrderByCreatedAtDesc(cardNumber, typeTransfer);
            if (transactionDTOS.size() <= 0) {
                logger.warn("{}| card number - {} not found transaction!", logId, cardNumber);
                return TransactionsResponse.builder().build();
            }

            transactionDTOS.forEach(transactionDTO -> {
                AccountPaymentDTO accountPaymentDTO = accountPaymentRepository.findFirstByCardNumber(transactionDTO.getSenderCard());
                transactions.add(TransactionMapper.toModelTransaction(
                        transactionDTO,
                        transactionDTO.getSenderCard(),
                        accountPaymentDTO == null ? transactionDTO.getCardName() : accountPaymentDTO.getCardName()));
            });
        } else if (typeTrans.equals(debt)){
            transactionDTOS = transactionRepository.findAllBySenderCardOrReceiverCardAndTypeTransOrderByCreatedAtDesc(cardNumber, cardNumber, typeDebt);
            if (transactionDTOS.size() <= 0) {
                logger.warn("{}| card number - {} not found transaction!", logId, cardNumber);
                return TransactionsResponse.builder().build();
            }
            transactionDTOS.forEach(transactionDTO -> {
                int result = 1;
                long card = transactionDTO.getSenderCard();
                if (cardNumber == transactionDTO.getSenderCard()) {
                    result = -1;
                    card = transactionDTO.getReceiverCard();
                }
                transactions.add(TransactionMapper.toModelTransactionDebt(
                    transactionDTO,
                        card,
                    accountPaymentRepository.findFirstByCardNumber(card).getCardName(),
                    result
            ));
            });
        } else if (typeTrans.equals(all)) {
            TransactionsResponse transaction = TransactionsResponse.builder()
                    .userId(paymentDTO.getUserId())
                    .cardNumber(paymentDTO.getCardNumber())
                    .cardName(paymentDTO.getCardName())
                    .build();
            transactionDTOS = transactionRepository.findAllBySenderCardOrReceiverCardOrderByCreatedAtDesc(cardNumber, cardNumber);
            if (transactionDTOS.size() <= 0) {
                logger.warn("{}| card number - {} not found transaction!", logId, cardNumber);
                return TransactionsResponse.builder().build();
            }
            transactionDTOS.forEach(transactionDTO -> {
                if (transactionDTO.getTypeTrans() == typeTransfer) { //giao dich binh thuong
                    if (cardNumber == transactionDTO.getSenderCard()) { //chuyen tien
                        List<Transaction> transactionArrayList = new ArrayList<>();
                        AccountPaymentDTO accountPaymentDTO=  accountPaymentRepository.findFirstByCardNumber(transactionDTO.getReceiverCard());
                        transactionArrayList.add(TransactionMapper.toModelTransaction(
                                transactionDTO,
                                transactionDTO.getReceiverCard(),
                                accountPaymentDTO == null ? transactionDTO.getCardName() : accountPaymentDTO.getCardName()));
                        transaction.setSenders(transactionArrayList);
                    } else { //nhan tien
                        List<Transaction> transactionArrayList = new ArrayList<>();
                        AccountPaymentDTO accountPaymentDTO = accountPaymentRepository.findFirstByCardNumber(transactionDTO.getSenderCard());
                        transactionArrayList.add(TransactionMapper.toModelTransaction(
                                transactionDTO,
                                transactionDTO.getSenderCard(),
                                accountPaymentDTO == null ? transaction.getCardName() : accountPaymentDTO.getCardName()
                        ));
                        transaction.setReceivers(transactionArrayList);
                    }
                } else if (transactionDTO.getTypeTrans() == typeDebt) {
                    int result = 1;
                    long card = transactionDTO.getSenderCard();
                    if (cardNumber == transactionDTO.getSenderCard()) {
                        result = -1;
                        card = transactionDTO.getReceiverCard();
                    }
                    transactions.add(TransactionMapper.toModelTransactionDebt(
                            transactionDTO,
                            card,
                            accountPaymentRepository.findFirstByCardNumber(card).getCardName(),
                            result
                    ));
                }
                transaction.setDebts(transactions);
            });
            return transaction;
        } else {
            logger.warn("{}| Bad request get transaction: typeTrans - {} not existed!", logId, typeTrans);
            return null;
        }
        return TransactionsResponse.builder()
                .userId(paymentDTO.getUserId())
                .cardNumber(paymentDTO.getCardNumber())
                .cardName(paymentDTO.getCardName())
                .transactions(transactions)
                .build();
    }


    @Override
    public long insertTransaction(String logId, TransactionRequest request) {
        //Build transactionDTO
        TransactionDTO firstTrans = UserProcess.buildTransaction(new Timestamp(request.getRequestTime()), request, fee);
        TransactionDTO transactionDTO = transactionRepository.save(firstTrans);
        long transactionId = transactionDTO.getTransId();

        if (transactionId <= 0) {
            logger.warn("{}| Save transaction - {} fail!", logId, firstTrans.getTransId());
            return -1;
        } else {
            logger.info("{}| Save transaction success with id: {}", logId, transactionId);
            return transactionId;
        }
    }

    @Override
    public Transaction saveTransaction(TransactionDTO transactionDTO) {
        TransactionDTO transactionDTO1 = transactionRepository.save(transactionDTO);
        AccountPaymentDTO accountPaymentDTO = accountPaymentRepository.findFirstByCardNumber(transactionDTO1.getReceiverCard());
        return TransactionMapper.toModelTransaction(transactionDTO1, accountPaymentDTO.getCardNumber(), accountPaymentDTO.getCardName());
    }

    @Override
    public Transaction getByTransIdAndType(long transId, int type) {
        TransactionDTO transactionDTO = transactionRepository.findByTransIdAndTypeTrans(transId, type);
        long cardNumber = transactionDTO.getReceiverCard();
        String cardName = transactionDTO.getCardName();
        if (transactionDTO.getMerchantId() == myBankId) {
            AccountPaymentDTO accountPaymentDTO = accountPaymentRepository.findFirstByCardNumber(cardNumber);
            cardName = accountPaymentDTO.getCardName();
        }
        return TransactionMapper.toModelTransaction(transactionDTO, cardNumber, cardName);
    }
}
