package com.backend.service.impl;

import com.backend.constants.ErrorConstant;
import com.backend.dto.AccountPaymentDTO;
import com.backend.dto.AccountSavingDTO;
import com.backend.dto.ReminderDTO;
import com.backend.dto.UserDTO;
import com.backend.mapper.UserMapper;
import com.backend.model.Account;
import com.backend.model.request.CreateReminderRequest;
import com.backend.model.response.UserResponse;
import com.backend.process.UserProcess;
import com.backend.repository.IAccountPaymentRepository;
import com.backend.repository.IAccountSavingRepository;
import com.backend.repository.IReminderRepository;
import com.backend.repository.IUserRepository;
import com.backend.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements IUserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Autowired
    IAccountPaymentRepository accountPaymentRepository;

    @Autowired
    IAccountSavingRepository accountSavingRepository;

    @Autowired
    IUserRepository userRepository;

    @Autowired
    IReminderRepository reminderRepository;

    @Override
    public List<Account> getUsers(String logId, int type, long userId) {
        List<AccountPaymentDTO> accountPaymentDTOS = new ArrayList<>();
        List<AccountSavingDTO> accountSavingDTOS = new ArrayList<>();
        List<Account> accounts;
        switch (type) {
            case 1:
                accountPaymentDTOS = accountPaymentRepository.findAllByUserId(userId);
//                accountPaymentDTOS.forEach(account -> accounts.add(UserMapper.toModelAccount(
//                        AccountSavingDTO.builder().build(),
//                        account,
//                        1,
//                        true)));
                accounts = UserProcess.formatToAccounts(logId, accountPaymentDTOS, accountSavingDTOS, true);

                break;
            case 2:
                accountSavingDTOS = accountSavingRepository.findAllByUserId(userId);
//                accountSavingDTOS.forEach(account -> accounts.add(UserMapper.toModelAccount(
//                        account,
//                        AccountPaymentDTO.builder().build(),
//                        2,
//                        true)));
                accounts = UserProcess.formatToAccounts(logId, accountPaymentDTOS, accountSavingDTOS, true);

                break;
            default:
                accountPaymentDTOS = accountPaymentRepository.findAllByUserId(userId);
//                accountPaymentDTOS.forEach(account -> accounts.add(UserMapper.toModelAccount(
//                        AccountSavingDTO.builder().build(),
//                        account,
//                        1,
//                        true)));

                accountSavingDTOS = accountSavingRepository.findAllByUserId(userId);
//                accountSavingDTOS.forEach(account -> accounts.add(UserMapper.toModelAccount(
//                        account,
//                        AccountPaymentDTO.builder().build(),
//                        2,
//                        true)));
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
//            accountPaymentDTOS.forEach(account -> accounts.add(UserMapper.toModelAccount(
//                    AccountSavingDTO.builder().build(),
//                    account,
//                    1,
//                    true)));

            accountSavingDTOS = accountSavingRepository.findAllByUserId(userId);
//            accountSavingDTOS.forEach(account -> accounts.add(UserMapper.toModelAccount(
//                    account,
//                    AccountPaymentDTO.builder().build(),
//                    2,
//                    true)));
            accounts = UserProcess.formatToAccounts(logId, accountPaymentDTOS, accountSavingDTOS, true);

            return UserMapper.toModelUser(userDTO, accounts);
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
            return UserMapper.toModelUser(userDTO, null);
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
        return UserMapper.toModelUser(userDTO, accounts);
    }
}
