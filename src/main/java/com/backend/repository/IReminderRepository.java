package com.backend.repository;

import com.backend.dto.ReminderDTO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface IReminderRepository extends CrudRepository<ReminderDTO, Long> {
    ReminderDTO findFirstByCardNumberAndMerchantIdAndTypeAndUserIdAndIsActive(long cardNumber, long merchantId, int type, long userId, int isActive);

    List<ReminderDTO> findAllByUserIdAndTypeAndIsActive(long userId, int type, int isActive);

    List<ReminderDTO> findAllByUserIdAndTypeAndCardNumberAndIsActive(long userId, int type, long cardNumber, int isActive);
}
