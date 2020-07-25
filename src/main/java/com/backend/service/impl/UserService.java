package com.backend.service.impl;

import com.backend.constants.ActionConstant;
import com.backend.dto.*;
import com.backend.mapper.UserMapper;
import com.backend.model.Account;
import com.backend.model.Debt;
import com.backend.model.request.*;
import com.backend.model.response.DebtorResponse;
import com.backend.model.response.UserResponse;
import com.backend.process.UserProcess;
import com.backend.repository.*;
import com.backend.service.IUserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements IUserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);
    private static final int TYPE_CREDITOR = 1;
    @Value( "${my.bank.id}" )
    private long myBankId;

    @Value( "${fee.transfer}" )
    private long fee;

    @Value( "${status.transfer}" )
    private String status;

    private IAccountPaymentRepository accountPaymentRepository;
    private IAccountSavingRepository accountSavingRepository;
    private IUserRepository userRepository;
    private IReminderRepository reminderRepository;
    private IDebtRepository debtRepository;
    private ITransactionRepository transactionRepository;

    @Autowired
    public UserService(IAccountPaymentRepository accountPaymentRepository,
            IAccountSavingRepository accountSavingRepository,
            IUserRepository userRepository,
            IReminderRepository reminderRepository,
            IDebtRepository debtRepository,
            ITransactionRepository transactionRepository) {
        this.accountPaymentRepository   = accountPaymentRepository;
        this.accountSavingRepository    = accountSavingRepository;
        this.userRepository             = userRepository;
        this.reminderRepository         = reminderRepository;
        this.debtRepository             = debtRepository;
        this.transactionRepository      = transactionRepository;
    }

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
    public UserResponse getUser(String logId, String userName) {
        return UserMapper.toModelUser(userRepository.findFirstByUserName(userName), new ArrayList<>());
    }

    @Override
    public ReminderDTO createReminder(String logId, CreateReminderRequest request, long userId, String nameReminisce) {
        long cardNumber = request.getCardNumber();
        long merchantId = request.getMerchantId();
        Timestamp currentTime =  new Timestamp(request.getRequestTime());

        //Step 1: Validate reminder
        ReminderDTO reminder = reminderRepository.findFirstByCardNumberAndMerchantIdAndTypeAndUserId(
                cardNumber,
                merchantId,
                request.getType(),
                userId
        );

        if (reminder != null) {
            if (reminder.getIsActive() == 0) { //reminder was deleted
                logger.warn("{}| Card number - {} can add reminders", logId, cardNumber);
                reminder.setIsActive(1);
                reminder.setUpdatedAt(currentTime);
            } else {
                logger.warn("{}| Card number - {} was existed with id: {}", logId, cardNumber, reminder.getId());
                return null;
            }
        } else {
            reminder = new ReminderDTO();
            reminder.setUserId(userId);
            reminder.setMerchantId(merchantId);
            reminder.setCardNumber(cardNumber);
            reminder.setIsActive(1);
            reminder.setUpdatedAt(currentTime);
            reminder.setCreatedAt(currentTime);
            reminder.setType(request.getType());
        }
        reminder.setNameReminisce(nameReminisce);

        ReminderDTO reminderDTO = reminderRepository.save(reminder);

        if (reminderDTO == null) {
            logger.warn("{}| Save card number - {} fail!", logId, cardNumber);
            return null;
        } else {
            return reminderDTO;
        }
    }

    @Override
    public UserResponse getReminders(String logId, long userId, int type, Long cardNumber) {
        List<ReminderDTO> reminderDTOS;
        List<Account> accounts = new ArrayList<>();
        if (cardNumber != null) {
            reminderDTOS = reminderRepository.findAllByUserIdAndTypeAndCardNumberAndIsActive(userId, type, cardNumber, 1);
        } else {
            reminderDTOS = reminderRepository.findAllByUserIdAndTypeAndIsActiveOrderByIdDesc(userId, type, 1);
        }
        //Step 1: validate reminders
        UserDTO userDTO = userRepository.findById(userId).get();
        if (reminderDTOS.size() <= 0) {
            logger.warn("{}| user - {} haven't reminder!", logId, userId);
            return UserMapper.toModelUser(userDTO, null);
        }
        logger.info("{}| user - {} have {} reminder!", logId, userId, reminderDTOS.size());

        for (ReminderDTO reminder : reminderDTOS) {
            accounts.add(UserMapper.toModelReminder(
                    reminder));
        }
        //Step 2: Build response
        return UserMapper.toModelUser(userDTO, accounts);
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
        return UserMapper.toModelUser(userDTO, accounts);
    }

    @Override
    public DebtorResponse createDebtor(String logId, CreateDebtorRequest request, long userId) {
        long cardNumber = request.getCardNumber();
        List<DebtDTO> debtDTOS;
        int isActive = 1;

        //Step 1: Validate debtor
        AccountPaymentDTO accountPaymentDTO = accountPaymentRepository.findFirstByCardNumber(cardNumber);

        if (accountPaymentDTO == null) {
            logger.warn("{}| card number - {} not found!", logId, cardNumber);
            return null;
        }

        DebtDTO debtDTO = UserProcess.createDebt(ActionConstant.INIT.getValue(), request, new Timestamp(request.getRequestTime()), userId, accountPaymentDTO.getUserId());
        debtRepository.save(debtDTO);

       return getDebts(logId, userId, ActionConstant.INIT.getValue(), TYPE_CREDITOR);
    }

    @Override
    public DebtorResponse getDebts(String logId, long userId, int action, int type) {
        List<Debt> debts = new ArrayList<>();
        List<DebtDTO> debtDTOS;

        if (type == TYPE_CREDITOR) {
            //danh sach no minh tao
            debtDTOS = debtRepository.findAllByUserIdAndActionOrderByIdDesc(userId, action);
            debtDTOS.forEach(debtDTO -> debts.add(UserMapper.toModelDebt(debtDTO,
                    userRepository.findById(accountPaymentRepository.findFirstByCardNumber(debtDTO.getCardNumber()).getUserId()))));
        } else {
            //danh sach bi nhac no
            AccountPaymentDTO accountPaymentDTO = accountPaymentRepository.findFirstByUserId(userId);
            debtDTOS = debtRepository.findAllByCardNumberAndActionOrderByIdDesc(accountPaymentDTO.getCardNumber(), action);
            debtDTOS.forEach(debtDTO -> debts.add(UserMapper.toModelDebt(debtDTO, userRepository.findById(debtDTO.getUserId()))));
        }
        if (type == 1) {
            logger.info("{}| User - {} create: {} debt", logId, userId, debts.size());
        } else {
            logger.info("{}| User - {} have: {} debt", logId, userId, debts.size());
        }
        return DebtorResponse.builder().debts(debts).build();
    }

    @Override
    public long insertTransaction(String logId, TransactionRequest request) {
        //Build transactionDTO
        TransactionDTO firstTrans = UserProcess.buildTransaction(new Timestamp(request.getRequestTime()), request, fee);
        TransactionDTO transactionDTO = transactionRepository.save(firstTrans);
        Long transactionId = transactionDTO.getTransId();

        if (transactionId == null) {
            logger.warn("{}| Save transaction - {} fail!", logId, firstTrans.getTransId());
            return -1;
        } else {
            logger.info("{}| Save transaction success with id: {}", logId, transactionId);
            return transactionId;
        }
    }

    @Override
    public DebtorResponse deleteDebt(String logId, DeleteDebtRequest request, long userId) {
        try {
            Timestamp currentTime = new Timestamp(request.getRequestTime());
            int type = 1; //nợ do bản thân tạo
            //Step 1: Validate debt
            long debtId = request.getDebtId();
            DebtDTO debtDTO = debtRepository.findFirstByIdAndActionAndIsActive(debtId, ActionConstant.INIT.getValue(), 1);
            if (debtDTO == null) {
                logger.warn("{}| Debt - {} not found", logId, debtId);
                return null;
            }
            logger.info("{}| Debt - {} can delete", logId, debtId);

            if (userId != debtDTO.getUserId()) {
                type = 2;// nợ do người khác tạo
            }
            //Step 2: Update isActive = 0
            debtDTO.setAction(ActionConstant.DELETE.getValue());
            debtDTO.setUpdatedAt(currentTime);
            debtDTO.setContent(request.getContent());
            debtRepository.save(debtDTO);

            return getDebts(logId, userId, ActionConstant.DELETE.getValue(), type);
            //Step 5: send notify
            //// TODO: 7/20/20 send notify 
        } catch (Exception e) {
            logger.error("{}| Delete debt in db catch exception: ", logId, e);
            return null;
        }
    }

    @Override
    public Optional<ReminderDTO> getReminder(long id) {
        return reminderRepository.findById(id);
    }

    @Override
    public ReminderDTO saveReminder(ReminderDTO reminderDTO) {
        return reminderRepository.save(reminderDTO);
    }
}
