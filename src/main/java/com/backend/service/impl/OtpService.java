package com.backend.service.impl;

import com.backend.constants.ActionConstant;
import com.backend.dto.OtpDTO;
import com.backend.repository.IOtpRepository;
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

    @Autowired
    public OtpService(IOtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }
    @Override
    public OtpDTO saveOtp(OtpDTO otpDTO) {
        return otpRepository.save(otpDTO);
    }

    @Override
    public boolean validateOtp(String logId, long userId, int otp, String action, int session, Timestamp currentTime) {
//       Step 1: validate otp
        OtpDTO otpDTO = otpRepository.findFirstByUserIdAndOtpAndStatusAndAction(userId, otp, ActionConstant.INIT.getValue(), action);

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

        //todo update status transaction pending - > success
        return true;
    }
}
