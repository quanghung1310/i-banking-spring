package com.backend.service.impl;

import com.backend.dto.OtpDTO;
import com.backend.repository.IOtpRepository;
import com.backend.service.IOtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OtpService implements IOtpService {
    private IOtpRepository otpRepository;

    @Autowired
    public OtpService(IOtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }
    @Override
    public OtpDTO saveOtp(OtpDTO otpDTO) {
        return otpRepository.save(otpDTO);
    }
}
