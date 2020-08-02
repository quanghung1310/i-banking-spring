package com.backend.service.impl;

import com.backend.constants.StringConstant;
import com.backend.dto.AccountPaymentDTO;
import com.backend.dto.PartnerDTO;
import com.backend.dto.TransactionDTO;
import com.backend.dto.UserDTO;
import com.backend.mapper.TransactionMapper;
import com.backend.mapper.UserMapper;
import com.backend.model.TransactionMerchant;
import com.backend.model.request.employee.RegisterRequest;
import com.backend.model.response.EmployeeResponse;
import com.backend.model.response.RegisterResponse;
import com.backend.model.response.TransMerchantResponse;
import com.backend.process.UserProcess;
import com.backend.repository.IAccountPaymentRepository;
import com.backend.repository.IPartnerRepository;
import com.backend.repository.ITransactionRepository;
import com.backend.repository.IUserRepository;
import com.backend.service.IAdminService;
import com.backend.util.DataUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminService implements IAdminService {
    private static final Logger logger = LogManager.getLogger(AdminService.class);

    @Value("${role.employer}")
    private String EMPLOYER;

    @Value( "${my.bank.id}" )
    private long myBankId;

    private int TRANS_SENDER = 1;
    private int TRANS_RECEIVER = 2;

    private IUserRepository userRepository;
    private ITransactionRepository transactionRepository;
    private IAccountPaymentRepository accountPaymentRepository;
    private IPartnerRepository partnerRepository;

    @Autowired
    public AdminService(IUserRepository userRepository,
                        ITransactionRepository transactionRepository,
                        IAccountPaymentRepository accountPaymentRepository, IPartnerRepository partnerRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.accountPaymentRepository = accountPaymentRepository;
        this.partnerRepository = partnerRepository;
    }


    @Override
    public RegisterResponse registerEmployee(String logId, RegisterRequest request, String role) {
        String userName = request.getName().toLowerCase().replaceAll("\\s+","");
        List<UserDTO> users = userRepository.findAllByRole(role);
        for (UserDTO user : users) {
            if (user.getUserName().equals(userName)) {
                logger.warn("{}| Username - {} was existed!", logId, userName);
                return null;
            }
        }
        logger.warn("{}| Username - {} is not exist!", logId, userName);

        String password = DataUtil.generatePass();
        logger.info("{}| Generate password for username - {}: {}", logId, userName, password);

        UserDTO userDTO = userRepository.save(UserProcess.createUser(request, userName, role, BCrypt.hashpw(password, BCrypt.gensalt())));
        Long userId = userDTO.getId();

        if (userId == null) {
            logger.warn("{}| Save user false!", logId);
            return null;
        }
        logger.info("{}| Save user - {}: success!", logId, userId);

        return RegisterResponse.builder()
                .userName(userDTO.getUserName())
                .password(password)
                .createDate(DataUtil.convertTimeWithFormat(userDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                .build();
    }

    @Override
    public List<EmployeeResponse> getEmployee(String logId, Long employerId, String role) {
        List<EmployeeResponse> employeeResponses = new ArrayList<>();
        if (employerId == null) {
            List<UserDTO> userDTOS = userRepository.findAllByRole(role);
            userDTOS.forEach(userDTO -> employeeResponses.add(UserMapper.toModelEmployee(userDTO)));

        } else {
            List<UserDTO> userDTOS = userRepository.findAllByRoleAndId(role, employerId);
            if (userDTOS.size() <= 0) {
                logger.warn("{}| Employee - {} not found!", logId, employerId);
                return null;
            }
            userDTOS.forEach(userDTO -> employeeResponses.add(UserMapper.toModelEmployee(userDTO)));
        }
        return employeeResponses;
    }

    @Override
    public UserDTO findById(long id) {
        Optional<UserDTO> userDTO = userRepository.findById(id);
        return userDTO.orElse(null);
    }

    @Override
    public EmployeeResponse saveEmployee(UserDTO userDTO) {
        UserDTO userDTO1 = userRepository.save(userDTO);
        return UserMapper.toModelEmployee(userDTO1);
    }

    @Override
    public List<TransMerchantResponse> controlTransaction(String logId, int merchantId, String beginTime, String endTime) {
        Map<Integer, TransMerchantResponse> transactionMerchantMap = new TreeMap<>();
        List<TransactionDTO> transactionDTOS;
        if (merchantId == myBankId) {
            transactionDTOS = transactionRepository.findAllByNotMyBankAndCreatedAtBetween(merchantId, beginTime, endTime);
        }  else {
            transactionDTOS = transactionRepository.findAllByMerchantIdAndCreatedAtBetween(merchantId, beginTime, endTime);
        }
        if (transactionDTOS.size() <= 0) {
            logger.warn("{}| Merchant - {} not found transaction!", logId, merchantId);
            return null;
        }

        transactionDTOS.forEach(transactionDTO -> {
            TransMerchantResponse transMerchantResponse = transactionMerchantMap
                    .getOrDefault(transactionDTO.getMerchantId(), TransMerchantResponse.builder().build());

            List<TransactionMerchant> transaction = transMerchantResponse.getTransactionMerchants() == null
                    ? new ArrayList<>()
                    : transMerchantResponse.getTransactionMerchants();
            AccountPaymentDTO accountPaymentDTO = accountPaymentRepository.findFirstByCardNumber(transactionDTO.getReceiverCard());
            long cardNumber;
            int type = TRANS_RECEIVER;

            if (accountPaymentDTO == null) {
                accountPaymentDTO = accountPaymentRepository.findFirstByCardNumber(transactionDTO.getSenderCard());
                cardNumber = accountPaymentDTO.getCardNumber();
                type = TRANS_SENDER;
            } else {
                cardNumber = accountPaymentDTO.getCardNumber();
            }
            transaction.add(TransactionMapper.toModelTransMerchant(transactionDTO, cardNumber, accountPaymentDTO.getCardName(), type));

            PartnerDTO partnerDTO = partnerRepository.findById(transactionDTO.getMerchantId()).get();

            if (transMerchantResponse.getMerchantId() == 0) {
                transMerchantResponse.setMerchantEmail(partnerDTO.getEmail());
                transMerchantResponse.setMerchantId(partnerDTO.getId());
                transMerchantResponse.setMerchantName(partnerDTO.getName());
                transMerchantResponse.setMerchantPhone(partnerDTO.getPhoneNumber());
            }
            transMerchantResponse.setTransactionMerchants(transaction);
            transMerchantResponse.setTotalAMount(transMerchantResponse.getTotalAMount() + transactionDTO.getAmount());

            transactionMerchantMap.put(transactionDTO.getMerchantId(), transMerchantResponse);
        });
        return new ArrayList<>(transactionMerchantMap.values());
    }
}
