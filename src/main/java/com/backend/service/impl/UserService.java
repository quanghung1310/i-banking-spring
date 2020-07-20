package com.backend.service.impl;

import com.backend.constants.ActionConstant;
import com.backend.constants.ErrorConstant;
import com.backend.dto.*;
import com.backend.mapper.UserMapper;
import com.backend.model.Account;
import com.backend.model.Debt;
import com.backend.model.Transaction;
import com.backend.model.request.*;
import com.backend.model.response.DebtorResponse;
import com.backend.model.response.TransactionResponse;
import com.backend.model.response.UserResponse;
import com.backend.process.UserProcess;
import com.backend.repository.*;
import com.backend.service.IUserService;
import com.backend.util.DataUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements IUserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Value( "${my.bank.id}" )
    private long myBankId;

    @Value( "${fee.transfer}" )
    private long fee;

    @Value( "${status.transfer}" )
    private String status;

    @Autowired
    IAccountPaymentRepository accountPaymentRepository;

    @Autowired
    IAccountSavingRepository accountSavingRepository;

    @Autowired
    IUserRepository userRepository;

    @Autowired
    IReminderRepository reminderRepository;

    @Autowired
    IDebtRepository debtRepository;

    @Autowired
    ITransactionRepository transactionRepository;

    @Override
    public List<Account> getUsers(String logId, int type, long userId) {
        List<AccountPaymentDTO> accountPaymentDTOS = new ArrayList<>();
        List<AccountSavingDTO> accountSavingDTOS = new ArrayList<>();
        List<Account> accounts;
        switch (type) {
            case 1:
                accountPaymentDTOS = accountPaymentRepository.findAllByUserId(userId);
                accounts = UserProcess.formatToAccounts(logId, accountPaymentDTOS, accountSavingDTOS, true);

                break;
            case 2:
                accountSavingDTOS = accountSavingRepository.findAllByUserId(userId);
                accounts = UserProcess.formatToAccounts(logId, accountPaymentDTOS, accountSavingDTOS, true);

                break;
            default:
                accountPaymentDTOS = accountPaymentRepository.findAllByUserId(userId);
                accountSavingDTOS = accountSavingRepository.findAllByUserId(userId);
                accounts = UserProcess.formatToAccounts(logId, accountPaymentDTOS, accountSavingDTOS, true);
                break;
        }

        return accounts;
    }

    @Override
    public UserResponse login(String logId, String userName, String password) {
        List<AccountPaymentDTO> accountPaymentDTOS;
        List<AccountSavingDTO> accountSavingDTOS;
        List<Account> accounts;
        UserDTO userDTO = userRepository.findFirstByUserNameAndPassword(userName, password);
        if (userDTO == null) {
            return UserResponse.builder().build();
        } else {
            long userId = userDTO.getId();
            accountPaymentDTOS = accountPaymentRepository.findAllByUserId(userId);
            accountSavingDTOS = accountSavingRepository.findAllByUserId(userId);
            accounts = UserProcess.formatToAccounts(logId, accountPaymentDTOS, accountSavingDTOS, true);

            return UserMapper.toModelUser(userDTO, accounts, true);
        }
    }

    @Override
    public long createReminder(String logId, CreateReminderRequest request) {
        ReminderDTO reminderDTO;
        String userName = request.getNameReminisce();
        long cardNumber = request.getCardNumber();
        long merchantId = request.getMerchantId();

        //Step 1: Validate reminder
        ReminderDTO reminder = reminderRepository.findFirstByCardNumberAndMerchantIdAndTypeAndUserIdAndIsActive(
                cardNumber,
                merchantId,
                request.getType(),
                request.getUserId(),
                1
        );
        if (reminder != null) {
            logger.warn("{}| Card number - {} was existed with id: {}", logId, cardNumber, reminder.getId());
            return -1;
        }
        //Step 2: Validate account
        if (merchantId == 0) { //Tài khoản cùng ngân hàng
            AccountPaymentDTO accountPaymentDTO = accountPaymentRepository.findFirstByCardNumber(cardNumber);
            if (accountPaymentDTO == null) {
                logger.warn("{}| Card number - {} not existed!", logId, cardNumber);
                return ErrorConstant.BAD_REQUEST;
            }
            logger.info("{}| Card number - {} is existed!", logId, cardNumber);

            long userId = accountPaymentDTO.getUserId();
            if (StringUtils.isBlank(userName)) {
                userName = userRepository.findById(userId).get().getUserName();
                request.setNameReminisce(userName);
            }

            reminderDTO = UserProcess.createReminder(logId, request, new Timestamp(request.getRequestTime()));
        } else { //Tài khoản liên ngân hàng
            //todo Query account diff bank
            logger.warn("{}| Chưa làm tới, gọi sau đi bạn êi !", logId);
            reminderDTO = null;
            return -2;
        }

        ReminderDTO reminderRes = reminderRepository.save(reminderDTO);
        if (reminderRes == null) {
            logger.warn("{}| Save card number - {} fail!", logId, cardNumber);
            return -2;
        } else {
            return reminderDTO.getId();
        }
    }

    @Override
    public UserResponse getReminders(String logId, long userId, int type, Long cardNumber) {
        List<ReminderDTO> reminderDTOS;
        List<Account> accounts = new ArrayList<>();
        if (cardNumber != null) {
            reminderDTOS = reminderRepository.findAllByUserIdAndTypeAndCardNumberAndIsActive(userId, type, cardNumber, 1);
        } else {
            reminderDTOS = reminderRepository.findAllByUserIdAndTypeAndIsActive(userId, type, 1);
        }
        //Step 1: validate reminders
        UserDTO userDTO = userRepository.findById(userId).get();
        if (reminderDTOS.size() <= 0) {
            logger.warn("{}| user - {} haven't reminder!", logId, userId);
            return UserMapper.toModelUser(userDTO, null, true);
        }
        logger.info("{}| user - {} have {} reminder!", logId, userId, reminderDTOS.size());

        for (ReminderDTO reminder : reminderDTOS) {
            AccountPaymentDTO accountPaymentDTO;
            if (reminder.getMerchantId() == 0) {
                //1.1 reminder same bank
                accountPaymentDTO = accountPaymentRepository.findFirstByCardNumber(reminder.getCardNumber());
                accounts.add(UserMapper.toModelAccount(
                        AccountSavingDTO.builder().build(),
                        accountPaymentDTO,
                        1,
                        false));

            } else {
                //1.2: reminder diff bank
                //todo
                logger.info("{}| Đã làm đâu mà gọi! (reminderId - {})", logId, reminder.getId());
            }
        }
        //Step 2: Build response
        return UserMapper.toModelUser(userDTO, accounts, false);
    }

    @Override
    public UserResponse queryAccount(String logId, long cardNumber, long merchantId, int typeAccount, boolean isBalance) {
        List<Account> accounts = new ArrayList<>();
        long userId = 0;
        if (merchantId == myBankId) {
            //Cung ngan hang
            AccountPaymentDTO accountPaymentDTO;
            accountPaymentDTO = accountPaymentRepository.findFirstByCardNumber(cardNumber);
            accounts.add(UserMapper.toModelAccount(
                    AccountSavingDTO.builder().build(),
                    accountPaymentDTO,
                    typeAccount,
                    isBalance));
            userId = accountPaymentDTO.getUserId();
        } else {
            //Lien ngan hang
            /// TODO: 7/5/2020
            return null;
        }

        UserDTO userDTO = userRepository.findById(userId).get();
        return UserMapper.toModelUser(userDTO, accounts, false);
    }

    @Override
    public long createDebtor(String logId, CreateDebtorRequest request) {
        ReminderDTO reminderDTO;
        long debtorId = request.getDebtorId();
        long cardNumber = request.getCardNumber();
        long userId = request.getUserId();
        long amount = request.getAmount();

        //Step 1: Validate debtor
        Optional<UserDTO> userDTO = userRepository.findById(userId);
        Optional<UserDTO> debt = userRepository.findById(debtorId);

        if (userDTO == null || debt == null) {
            logger.warn("{}| User - {} or debt - {} not found", logId, userId, debtorId);
            return -1;
        }

        AccountPaymentDTO accountPaymentDTO = accountPaymentRepository.findFirstByCardNumberAndUserId(cardNumber, debtorId);

        if (accountPaymentDTO == null) {
            logger.warn("{}| card number - {} not found!", logId, cardNumber);
            return -1;
        }

        DebtDTO debtDTO = UserProcess.createDebt(logId, ActionConstant.INIT.getValue(), request, new Timestamp(request.getRequestTime()));

        DebtDTO debtor = debtRepository.save(debtDTO);

        return debtor.getId();
    }

    @Override
    public DebtorResponse getDebts(String logId, long userId, int action, int type) {
        List<Debt> debts = new ArrayList<>();
        List<DebtDTO> debtDTOS;
        UserDTO userDTO = userRepository.findById(userId).get();
        if (userDTO.getId() <= 0) {
            logger.warn("{}| User - {} not found", logId, userId);
            return null;
        }

        int isActive = 1;
        if (type == 1) {
            //danh sach no minh tao
            debtDTOS = debtRepository.findAllByUserIdAndActionAndIsActive(userId, ActionConstant.INIT.getValue(), isActive);
            debtDTOS.forEach(debtDTO -> debts.add(UserMapper.toModelDebt(debtDTO, userRepository.findById(debtDTO.getDebtorId()).get())));
        } else {
            //danh sach bi nhac no
            debtDTOS = debtRepository.findAllByDebtorIdAndActionAndIsActive(userId, ActionConstant.INIT.getValue(), isActive);
            debtDTOS.forEach(debtDTO -> debts.add(UserMapper.toModelDebt(debtDTO, userRepository.findById(debtDTO.getUserId()).get())));
        }
        if (type == 1) {
            logger.info("{}| User - {} create: {} debt", logId, userId, debts.size());
        } else {
            logger.info("{}| User - {} have: {} debt", logId, userId, debts.size());
        }
        return DebtorResponse.builder().debts(debts).build();
    }

    @Override
    public TransactionResponse transaction(String logId, TransactionRequest request) {
        Timestamp currentTime = new Timestamp(request.getRequestTime());
        Long cardNumber = request.getCardNumber();
        List<AccountPaymentDTO> accounts = (List<AccountPaymentDTO>) accountPaymentRepository.findAll();
        String cardName = null;
        for (AccountPaymentDTO account : accounts) {
            if (account.getCardNumber() == cardNumber) {
                cardName = account.getCardName();
            }
        }

        if (cardName == null) {
            logger.warn("{}| Card number is not exist!", logId);
            return null;
        }

        TransactionDTO transactionDTO = transactionRepository.save(UserProcess.createTransaction(logId, currentTime, request, cardName));
        Long transactionId = transactionDTO.getId();
        logger.info("{}| Save transaction - {}:{}", logId, transactionId, transactionId == null ? "false" : "success");

        return TransactionResponse.builder()
                .transId(transactionDTO.getTransId())
                .cardName(transactionDTO.getCardName())
                .cardNumber(transactionDTO.getCardNumber())
                .amount(transactionDTO.getAmount())
                .typeFee(transactionDTO.getTypeFee())
                .fee(transactionDTO.getFee())
                .content(transactionDTO.getContent())
                .createDate(transactionDTO.getCreatedAt())
                .merchantId(transactionDTO.getMerchantId())
                .status(transactionDTO.getStatus())
                .build();
    }

    @Override
    public long insertTransaction(String logId, TransferRequest request, long merchantId, long userId, String cardName) {
        //Build TransactionRequest
        TransactionRequest transactionRequest = TransactionRequest.builder()
                .requestId(request.getRequestId())
                .requestTime(request.getRequestTime())
                .amount(request.getValue())
                .cardNumber(request.getFrom())
                .content(request.getDescription())
                .merchantId(merchantId)
                .typeFee(request.getTypeFee())
                .typeTrans(request.getIsTransfer() ? 1 : 2)
                .userId(userId)
                .build();
        //Build transactionDTO
        TransactionDTO firstTrans = UserProcess.buildTransaction(new Timestamp(request.getRequestTime()), transactionRequest, cardName, status, fee);
        TransactionDTO transactionDTO = transactionRepository.save(firstTrans);
        Long transactionId = transactionDTO.getId();

        if (transactionId == null) {
            logger.warn("{}| Save transaction - {} fail!", logId, firstTrans.getTransId());
            return -1;
        } else {
            logger.info("{}| Save transaction success with id: {}", logId, transactionId);
            return transactionDTO.getTransId();
        }
    }

    @Override
    public long deleteDebt(String logId, DeleteDebtRequest request) {
        try {
            //Step 1: Validate user
            long userId = request.getUserId();
            Timestamp currentTime = new Timestamp(request.getRequestTime());
            
            Optional<UserDTO> userDTO = userRepository.findById(userId);
            if (userDTO == null) {
                logger.warn("{}| User - {} not found", logId, userId);
                return -1;
            }

            //Step 2: Validate debt
            long debtId = request.getDebtId();
            DebtDTO debtDTO = debtRepository.findFirstByIdAndActionAndIsActive(debtId, ActionConstant.INIT.getValue(), 1);
            if (debtDTO == null) {
                logger.warn("{}| Debt - {} not found", logId, debtId);
                return -1;
            }
            logger.info("{}| Debt - {} can delete", logId, debtId);

            //Step 3: Update isActive = 0
            debtDTO.setAction(ActionConstant.DELETE.getValue());
            debtDTO.setUpdatedAt(currentTime);

            debtRepository.save(debtDTO);

            //Step 4: insert new debt with action = DELETE
            //4.1. Build CreateDebtorRequest
            CreateDebtorRequest createDebtorRequest = new CreateDebtorRequest();
            createDebtorRequest.setAmount(debtDTO.getAmount());
            createDebtorRequest.setCardNumber(debtDTO.getCardNumber());
            createDebtorRequest.setContent(request.getContent());
            createDebtorRequest.setDebtorId(debtDTO.getDebtorId());
            createDebtorRequest.setRequestId(request.getRequestId());
            createDebtorRequest.setRequestTime(request.getRequestTime());
            createDebtorRequest.setUserId(userId);
            //4.2 Build DebtDTO
            DebtDTO debtDelete = UserProcess.createDebt(logId, ActionConstant.DELETE.getValue(), createDebtorRequest, currentTime);
            debtDelete = debtRepository.save(debtDelete);
            
            return debtDelete.getId();
            //Step 5: send notify
            //// TODO: 7/20/20 send notify 
        } catch (Exception e) {
            logger.error("{}| Delete debt in db catch exception: ", logId, e);
            return -2;
        }
    }

    @Override
    public long payDebt(String logId, TransactionRequest request) {
        //Step 0: validate debt
        long debtId = request.getDebtId();
        DebtDTO debtDTO = debtRepository.findFirstByIdAndActionAndIsActive(debtId, ActionConstant.INIT.getValue(), 1);
        if (debtDTO == null) {
            logger.warn("{}| Debt - {} not found", logId, debtId);
            return -1;
        }
        logger.info("{}| Validate debt - {} success!", logId, debtId);

        //Step 1: validate FROM
        AccountPaymentDTO accountFrom = accountPaymentRepository.findFirstByCardNumber(request.getCardNumber());
        if (accountFrom == null) {
            logger.warn("{}| Card number sender - {} not fount!", logId, request.getCardNumber());
            return -1;
        }
        logger.info("{}| Account sender  -{} is existed!", logId, accountFrom.getId());

        //Step 2: validate TO

        //Step 3: validate otp

        //Step 4: validate balance FROM

        //Step 5: update balance of FROM

        //Step 6: update balance of TO

        //Step 7: Update action debt to COMPLETED

        //Step 8: insert transaction

        //Step 9: response transId
        return 0;
    }
}
