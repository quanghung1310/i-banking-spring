package com.backend.repository;

import com.backend.dto.ReminderDTO;
import org.springframework.data.repository.CrudRepository;

public interface IReminderRepository extends CrudRepository<ReminderDTO, Long> {
    ReminderDTO findFirstByCardNumberAndMerchantIdAndTypeAndUserIdAndIsActive(long cardNumber, long merchantId, int type, long userId, int isActive);
}
