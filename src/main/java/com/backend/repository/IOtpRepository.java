package com.backend.repository;

import com.backend.dto.OtpDTO;
import org.springframework.data.repository.CrudRepository;

public interface IOtpRepository extends CrudRepository<OtpDTO, Long> {
    OtpDTO findFirstByUserIdAndOtpAndStatusAndActionAndTransId(long userId, long otp, int status, String action, long transId);
}
