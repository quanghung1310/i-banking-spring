package com.backend.service.impl;

import com.backend.config.PartnerConfig;
import com.backend.constants.ActionConstant;
import com.backend.dto.*;
import com.backend.mapper.TransactionMapper;
import com.backend.mapper.UserMapper;
import com.backend.model.Account;
import com.backend.model.Debt;
import com.backend.model.request.debt.CreateDebtorRequest;
import com.backend.model.request.debt.DeleteDebtRequest;
import com.backend.model.request.debt.PayDebtRequest;
import com.backend.model.request.reminder.CreateReminderRequest;
import com.backend.model.response.DebtorResponse;
import com.backend.model.response.TransactionResponse;
import com.backend.model.response.UserResponse;
import com.backend.process.PartnerProcess;
import com.backend.process.TransactionProcess;
import com.backend.process.UserProcess;
import com.backend.repository.*;
import com.backend.service.IUserService;
import com.backend.util.DataUtil;
import com.backend.util.PGPEncryptionUtil;
import com.backend.util.RSAUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.vertx.core.impl.StringEscapeUtils;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService implements IUserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);
    private static final int TYPE_CREDITOR = 1;
    @Value("${my.bank.id}")
    private long myBankId;

    @Value("${fee.transfer}")
    private long fee;

    @Value("${status.transfer}")
    private String status;

    @Value("${session.request}")
    private int session;

    public static final ObjectWriter OBJECT_WRITER = new ObjectMapper().writer()
            .withDefaultPrettyPrinter();

    private IAccountPaymentRepository accountPaymentRepository;
    private IAccountSavingRepository accountSavingRepository;
    private IUserRepository userRepository;
    private IReminderRepository reminderRepository;
    private IDebtRepository debtRepository;
    private ITransactionRepository transactionRepository;
    private IPartnerRepository partnerRepository;
    private INotifyRepository notifyRepository;

    @Autowired
    public UserService(IAccountPaymentRepository accountPaymentRepository,
                       IAccountSavingRepository accountSavingRepository,
                       IUserRepository userRepository,
                       IReminderRepository reminderRepository,
                       IDebtRepository debtRepository,
                       ITransactionRepository transactionRepository,
                       IPartnerRepository partnerRepository,
                       INotifyRepository notifyRepository) {
        this.accountPaymentRepository = accountPaymentRepository;
        this.accountSavingRepository = accountSavingRepository;
        this.userRepository = userRepository;
        this.reminderRepository = reminderRepository;
        this.debtRepository = debtRepository;
        this.transactionRepository = transactionRepository;
        this.partnerRepository = partnerRepository;
        this.notifyRepository = notifyRepository;
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
        Timestamp currentTime = new Timestamp(request.getRequestTime());

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

        if (reminderDTO.getId() <= 0) {
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
            if (type == 0) {
                reminderDTOS = reminderRepository.findAllByUserIdAndIsActiveOrderByIdDesc(userId, 1);
            } else {
                reminderDTOS = reminderRepository.findAllByUserIdAndTypeAndIsActiveOrderByIdDesc(userId, type, 1);
            }
        }
        //Step 1: validate reminders
        UserDTO userDTO = userRepository.findById(userId).get();
        if (reminderDTOS.size() <= 0) {
            logger.warn("{}| user - {} haven't reminder!", logId, userId);
            return UserMapper.toModelUser(userDTO, null);
        }
        logger.info("{}| user - {} have {} reminder!", logId, userId, reminderDTOS.size());

        for (ReminderDTO reminder : reminderDTOS) {
            AccountPaymentDTO accountPaymentDTO = accountPaymentRepository.findFirstByCardNumber(reminder.getCardNumber());
            String cardName = "";
            if (accountPaymentDTO != null) {
                cardName = accountPaymentDTO.getCardName();
            }
            accounts.add(UserMapper.toModelReminder(reminder, cardName));
        }
        //Step 2: Build response
        return UserMapper.toModelUser(userDTO, accounts);
    }

    @Override
    public UserResponse queryAccount(String logId, long cardNumber, long merchantId, int typeAccount, boolean isBalance) throws Exception {
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
            Optional<UserDTO> userDTO = userRepository.findById(userId);
            return userDTO.map(dto -> UserMapper.toModelUser(dto, accounts)).orElse(null);
        } else {
            //Lien ngan hang
            Optional<PartnerDTO> partner = partnerRepository.findById((int) merchantId);
            if (!partner.isPresent()) {
                logger.warn("{}| Partner with bank id - {} not found!", logId, merchantId);
                return null;
            }
            //Step 2: Encrypt
            String mid = String.valueOf(merchantId);
            String alg = PartnerConfig.getAlg(mid);
            String cardPartner = String.valueOf(cardNumber);
            String url = PartnerConfig.getUrlQueryAccount(mid);

            if (alg.equals("RSA")) {
                // HttpHeaders
                MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

                //RSA
                //2.1: Build json body
                String secretKey = PartnerConfig.getPartnerSecretKey(mid);
                String partnerPub = PartnerConfig.getPartnerPubKey(mid);
                long currentTime = 1595770284249L;//System.currentTimeMillis();
                String partnerCode = PartnerConfig.getPartnerCode(mid);

                //(JSON.stringify(req.body)+ secretKey + time + partnerCode, 'base64')
                JsonObject requestBody = new JsonObject()
                        .put("stk", cardPartner);

                String dataCrypto = requestBody
                        + secretKey
                        + currentTime
                        + partnerCode
                        + "base64";
                String encrypt = RSAUtils.encrypt(dataCrypto, PartnerConfig.getPublicKey(mid));
                String decrypt = RSAUtils.decrypt(encrypt, PartnerConfig.getPrivateKey(mid));


                if (StringUtils.isBlank(encrypt)) {
                    logger.warn("{}| Hash data fail!", logId);
                    return null;
                }

                headers.set("x-partner-code", partnerCode);
                headers.set("x-timestamp", String.valueOf(currentTime));
                headers.set("x-data-encrypted", encrypt);

                RestTemplate restTemplate = new RestTemplate();

                HttpEntity<JsonObject> request = new HttpEntity<>(requestBody, headers);
                ResponseEntity<JsonObject> resp = restTemplate.postForEntity(url, request, JsonObject.class);
                /// TODO: 7/26/2020
                return UserResponse.builder().build();
            } else if (alg.equals("PGP")) {
                String data = new JsonObject().put("account_number", cardNumber).toString();
                String pubKey = StringEscapeUtils.unescapeJava(PartnerConfig.getPartnerPubKey(mid));
                String secretKey = PartnerConfig.getPartnerSecretKey(mid);
                if (StringUtils.isBlank(pubKey)) {
                    logger.warn("{}| Public key of partner - {} not existed!", logId, merchantId);
                    return null;
                }

                PGPPublicKey pgpPublicKey = PartnerProcess.readPublicKey(pubKey);
                String payload = PGPEncryptionUtil.encryptMessage(data, new ByteArrayInputStream(pubKey.getBytes()));
                String signature = DataUtil.signHmacSHA512(StringEscapeUtils.escapeJava(payload), secretKey);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                JsonObject requestBody;
                requestBody = PartnerProcess.getDataPartner(logId, new JsonObject(data), mid);

                if (requestBody == null) {
                    logger.warn("{}| Build request body fail!", logId);
                    requestBody = new JsonObject()
                            .put("payload", payload)
                            .put("signature", signature);
                }
                RestTemplate restTemplate = new RestTemplate();
                HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

                ResponseEntity<String> response = restTemplate.exchange(url,
                        HttpMethod.POST,
                        entity,
                        String.class);
                if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                    logger.info("{}| Response from associate: {}", logId, response.getBody());
                    return null;
                }
                JsonObject accountAssociate = new JsonObject(response.getBody()).getJsonObject("data");
                accounts.add(Account.builder()
                        .cardName(accountAssociate.getString("name", ""))
                        .cardNumber(Long.parseLong(accountAssociate.getString("account_number", "0")))
                        .build());
                return UserResponse.builder()
                        .account(accounts)
                        .build();
            } else {
                logger.warn("{}| alg - {} of merchant id - {} not existed!", logId, alg, merchantId);
                return null;
            }
        }
    }

    @Override
    public DebtorResponse createDebtor(String logId, CreateDebtorRequest request, long userId) {
        long cardNumber = request.getCardNumber();

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

    @Override
    public TransactionResponse payDebt(String logId, PayDebtRequest request, long userId) {
        //Step 0: validate debt
        long debtId = request.getDebtId();
        Timestamp currentTime = new Timestamp(request.getRequestTime());

        DebtDTO debtDTO = debtRepository.findFirstByIdAndActionAndIsActive(debtId, ActionConstant.INIT.getValue(), 1);
        if (debtDTO == null) {
            logger.warn("{}| Debt - {} not found", logId, debtId);
            return null;
        }
        logger.info("{}| Validate debt - {} success!", logId, debtId);

        //Step 1: validate FROM
        AccountPaymentDTO accountFrom = accountPaymentRepository.findFirstByCardNumber(debtDTO.getCardNumber());
        if (accountFrom == null) {
            logger.warn("{}| User - {} not fount!", logId, userId);
            return null;
        }
        logger.info("{}| User - {} is existed!", logId, accountFrom.getId());

        //Step 2: validate TO
        AccountPaymentDTO accountTo = accountPaymentRepository.findFirstByUserId(debtDTO.getUserId());
        if (accountTo == null) {
            logger.warn("{}| Debtor - {} not fount!", logId, debtDTO.getUserId());
            return null;
        }
        logger.info("{}| Debtor  -{} is existed!", logId, debtDTO.getUserId());

        //Step 3: validate balance FROM
        long amountPay = debtDTO.getAmount();
        long currentBalanceFrom = accountFrom.getBalance();
        long currentBalanceTo = accountTo.getBalance();

        if (request.getTypeFee() == 1) { //from tra fee
            if (currentBalanceFrom < amountPay + fee) {
                logger.warn("{}|Balance debtor: {} < {} (amountPay)", logId, currentBalanceFrom, amountPay + fee);
                return null;
            }
            accountFrom.setBalance(currentBalanceFrom - amountPay - fee);
            accountTo.setBalance(currentBalanceTo + amountPay);
        } else {
            if (currentBalanceFrom < amountPay) {
                logger.warn("{}|Balance debtor: {} < {} (amountPay)", logId, currentBalanceFrom, amountPay);
                return null;
            }
            accountFrom.setBalance(currentBalanceFrom - amountPay);
            accountTo.setBalance(currentBalanceTo + amountPay - fee);
        }
        logger.info("{}| User - {} can pay debt!", logId, userId);

        //Step 5: update account of FROM
        accountFrom.setUpdatedAt(currentTime);
        accountPaymentRepository.save(accountFrom);

        //Step 6: update account of TO
        accountTo.setUpdatedAt(currentTime);
        accountPaymentRepository.save(accountTo);

        //Step 7: Update action debt to COMPLETED
        debtDTO.setAction(ActionConstant.COMPLETED.getValue());
        debtDTO.setUpdatedAt(currentTime);
        debtDTO.setContent(request.getContent());
        debtRepository.save(debtDTO);

        //Step 8: insert transaction
        TransactionDTO transactionDTO = TransactionProcess.createTrans(
                accountFrom.getCardNumber(),
                accountTo.getCardNumber(),
                accountTo.getCardName(),
                amountPay,
                request.getTypeFee(),
                2,
                myBankId,
                request.getContent(),
                ActionConstant.COMPLETED.name(),
                currentTime,
                currentTime,
                fee);

        //Save notification
        UserDTO userDTO = userRepository.findById(accountTo.getUserId()).get();
        UserDTO fromDTO = userRepository.findById(userId).get();
        String receiverName = fromDTO.getName();
        NotifyDTO notifyDTO = new NotifyDTO();
        notifyDTO.setUserId(userDTO.getId());
        notifyDTO.setContent("Nhận tiền thanh toán nợ thành công");
        notifyDTO.setTitle("Quý khách đã nhận được số tiền " + debtDTO.getAmount() + "đ thanh toán nợ từ " + receiverName + ". Phí giao dịch: " + fee + "đ, do " + (request.getTypeFee() == 1 ? receiverName : "quý khách") + " thanh toán. Mã giao dịch " + transactionDTO.getTransId() + ".");
        notifyDTO.setIsActive(1);
        notifyDTO.setSeen(false);
        notifyDTO.setCreateAt(currentTime);
        notifyDTO.setUpdateAt(currentTime);
        notifyRepository.save(notifyDTO);

        return TransactionMapper.toModelTransResponse(transactionRepository.save(transactionDTO), accountTo.getCardName());
    }

    public static void main(String[] args) throws InvalidKeySpecException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidParameterSpecException {
        String PATH_TO_CONFIG_FOLDER = "conf\\";
        PartnerConfig.init(PATH_TO_CONFIG_FOLDER + "partner.json");

        String mid = "3";
        String cardPartner = "9001454953559";
        String secretKey = PartnerConfig.getPartnerSecretKey(mid);
        String partnerPub = PartnerConfig.getPartnerPubKey(mid);
        long currentTime = System.currentTimeMillis();
        String partnerCode = PartnerConfig.getPartnerCode(mid);
        String url = PartnerConfig.getUrlQueryAccount(mid);

        JsonObject requestBody = new JsonObject()
                .put("stk", cardPartner);

        String dataCrypto = requestBody
                + secretKey
                + currentTime
                + partnerCode;
        String encrypt = RSAUtils.encrypt(dataCrypto, PartnerConfig.getPublicKey(mid));
        String decrypt = RSAUtils.decrypt(encrypt, PartnerConfig.getPrivateKey(mid));
    }

    @Override
    public String updatePassword(String logId, String newPass, String userName) {
        UserDTO userDTO = userRepository.findFirstByUserName(userName);
        userDTO.setPassword(BCrypt.hashpw(newPass, BCrypt.gensalt()));
        userDTO.setLastPassword(userDTO.getPassword());
        userDTO.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        UserDTO user = userRepository.save(userDTO);
        return user.getPassword();
    }

    @Override
    public String forgotPassword(String logId, String userName) {
        UserDTO userDTO = userRepository.findFirstByUserName(userName);
        String password = DataUtil.generatePass();
        String hashPass = BCrypt.hashpw(password, BCrypt.gensalt());

        userDTO.setPassword(hashPass);
        userDTO.setLastPassword(userDTO.getPassword());
        userDTO.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        userRepository.save(userDTO);

        return new JsonObject()
                .put("password", password)
                .put("hashPassword", hashPass)
                .toString();
    }

    @Override
    public UserDTO getByCardNumber(String logId, long cardNumber) {
        AccountPaymentDTO accountPaymentDTO = accountPaymentRepository.findFirstByCardNumber(cardNumber);
        if (accountPaymentDTO == null) {
            return null;
        }
        Optional<UserDTO> userDTO = userRepository.findById(accountPaymentDTO.getUserId());
        return userDTO.orElse(null);
    }
}
