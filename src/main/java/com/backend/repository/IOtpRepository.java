package com.backend.repository;

import com.backend.dto.OtpDTO;
import org.springframework.data.repository.CrudRepository;

public interface IOtpRepository extends CrudRepository<OtpDTO, Long> {
    OtpDTO findFirstByUserIdAndOtpAndStatus(long userId, int otp, int status);
}
