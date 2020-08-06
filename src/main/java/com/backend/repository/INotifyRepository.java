package com.backend.repository;

import com.backend.dto.NotifyDTO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface INotifyRepository extends CrudRepository<NotifyDTO, Long> {
    List<NotifyDTO> findAllByUserIdAndIsActiveOrderByCreateAtDesc(long userId, int isActive);

    List<NotifyDTO> findAllByUserIdAndIsActive(long userId, int active);

    NotifyDTO findByIdAndUserIdAndIsActive(long id, long userId, int active);
}
