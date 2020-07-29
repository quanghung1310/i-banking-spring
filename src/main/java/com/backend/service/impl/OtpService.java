package com.backend.service.impl;

import com.backend.constants.ActionConstant;
import com.backend.dto.OtpDTO;
import com.backend.dto.TransactionDTO;
import com.backend.repository.IOtpRepository;
import com.backend.repository.ITransactionRepository;
import com.backend.service.IOtpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class OtpService implements IOtpService {
    private static final Logger logger = LogManager.getLogger(OtpService.class);

    private IOtpRepository otpRepository;
    private ITransactionRepository transactionRepository;

    @Autowired
    public OtpService(IOtpRepository otpRepository, ITransactionRepository transactionRepository) {
        this.otpRepository = otpRepository;
        this.transactionRepository = transactionRepository;
    }
    @Override
    public OtpDTO saveOtp(OtpDTO otpDTO) {
        return otpRepository.save(otpDTO);
    }

    @Override
    public boolean validateOtp(String logId, long userId, int otp, String action, int session, Timestamp currentTime, long transId) {
//       Step 1: validate otp
        OtpDTO otpDTO = otpRepository.findFirstByUserIdAndOtpAndStatusAndActionAndTransId(userId, otp, ActionConstant.INIT.getValue(), action, transId);

        //1.1. Compare OTP
        if (otpDTO == null) {
            logger.warn("{}| Otp - {} not fount!", logId, userId);
            return false;
        }

        //1.2. OTP het han
        if (currentTime.getTime() - otpDTO.getCreatedAt().getTime() > session ) {
            logger.warn("{}|OTP - {} out of session with - {} milliseconds!", logId, otp, session);
            return false;
        }
        logger.info("{}| Validate otp - {} success!", logId, otp);

        otpDTO.setStatus(ActionConstant.DELETE.getValue());
        otpDTO.setUpdatedAt(currentTime);
        otpRepository.save(otpDTO);

        // update status transaction init - > confirm
        TransactionDTO transactionDTO = transactionRepository.findFirstByTransId(transId);

        transactionDTO.setStatus(ActionConstant.CONFIRM.name());
        transactionDTO.setUpdatedAt(currentTime);
        transactionRepository.save(transactionDTO);
        return true;
    }
}
