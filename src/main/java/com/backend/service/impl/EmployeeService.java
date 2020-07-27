package com.backend.service.impl;

import com.backend.constants.StringConstant;
import com.backend.dto.AccountPaymentDTO;
import com.backend.dto.UserDTO;
import com.backend.mapper.UserMapper;
import com.backend.model.Account;
import com.backend.model.request.bank.DepositRequest;
import com.backend.model.request.employee.RegisterRequest;
import com.backend.model.response.DepositResponse;
import com.backend.model.response.RegisterResponse;
import com.backend.process.UserProcess;
import com.backend.repository.IAccountPaymentRepository;
import com.backend.repository.ITransactionRepository;
import com.backend.repository.IUserRepository;
import com.backend.service.IEmployeeService;
import com.backend.util.DataUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class EmployeeService implements IEmployeeService {
    private static final Logger logger = LogManager.getLogger(EmployeeService.class);

    private IUserRepository userRepository;
    private IAccountPaymentRepository accountPaymentRepository;
    private ITransactionRepository transactionRepository;

    @Autowired
    public EmployeeService(IUserRepository userRepository,
                           IAccountPaymentRepository accountPaymentRepository,
                           ITransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountPaymentRepository = accountPaymentRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public RegisterResponse register(String logId, RegisterRequest request, long employeeId, String role) {
        Timestamp currentTime = new Timestamp(request.getRequestTime());
        String userName = request.getName().toLowerCase().replaceAll("\\s+","");
        List<UserDTO> users = (List<UserDTO>) userRepository.findAll();
        for (UserDTO user : users) {
            if (user.getUserName().equals(userName)) {
                logger.warn("{}| Username - {} was existed!", logId, userName);
                return null;
            }
        }
        logger.warn("{}| Username - {} is not exist!", logId, userName);
        UserDTO userDTO = userRepository.save(UserProcess.createUser(request, userName, role));
        Long userId = userDTO.getId();

        if (userId == null) {
            logger.warn("{}| Save user false!", logId);
            return null;
        }
        logger.info("{}| Save user - {}: success!", logId, userId);

        List<AccountPaymentDTO> accounts = (List<AccountPaymentDTO>) accountPaymentRepository.findAll();
        AccountPaymentDTO accountPaymentDTO = accountPaymentRepository.save(UserProcess.createAccountPayment(logId, accounts, userId, employeeId, request.getCardName(), currentTime, request.getRequestTime()));
        Long accountId = accountPaymentDTO.getId();
        logger.info("{}| Save account payment - {}:{}", logId, accountId, accountId == null ? "false" : "success");
        Account account = UserMapper.toModelRegister(accountPaymentDTO);

        return RegisterResponse.builder()
                .userName(userDTO.getUserName())
                .password(userDTO.getPassword())
                .createDate(DataUtil.convertTimeWithFormat(userDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                .account(account)
                .build();
    }

    @Override
    public DepositResponse deposit(String logId, DepositRequest request) {
        Timestamp currentTime = new Timestamp(request.getRequestTime());
        //Step 1: userName or cardNumber is existed ?
        String userName = request.getUserName();
        Long cardNumber = request.getCardNumber();
        AccountPaymentDTO accountPaymentDTO;
        if (StringUtils.isNotBlank(userName)) {
            UserDTO userDTO = userRepository.findFirstByUserName(userName);
            accountPaymentDTO = accountPaymentRepository.findFirstByUserId(userDTO.getId());
        } else {
            accountPaymentDTO = accountPaymentRepository.findFirstByCardNumber(cardNumber);
        }
        //Step 2: update db
        if (accountPaymentDTO == null) {
            logger.warn("{}| Account - {} was not existed!", logId, cardNumber == null ? userName : cardNumber);
            return DepositResponse.builder().totalBalance(0).build();
        } else {
            long totalBalance = accountPaymentDTO.getBalance() + request.getBalance();
            accountPaymentDTO.setBalance(totalBalance);
            accountPaymentDTO.setUpdatedAt(currentTime);

            accountPaymentRepository.save(accountPaymentDTO);
            return DepositResponse.builder().totalBalance(totalBalance).build();
        }
    }
}
