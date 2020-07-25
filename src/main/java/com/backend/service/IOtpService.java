package com.backend.service;

import com.backend.dto.OtpDTO;

import java.sql.Timestamp;

public interface IOtpService {
    OtpDTO saveOtp(OtpDTO otpDTO);

    boolean validateOtp(String logId, long userId, int otpCode, String action, int session, Timestamp currentTime);
}
