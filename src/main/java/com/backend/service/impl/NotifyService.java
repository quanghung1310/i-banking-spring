package com.backend.service.impl;

import com.backend.dto.NotifyDTO;
import com.backend.model.Notify;
import com.backend.model.response.NotifyResponse;
import com.backend.repository.INotifyRepository;
import com.backend.service.INotifyService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotifyService implements INotifyService {
    private static final Logger logger = LogManager.getLogger(NotifyService.class);

    private INotifyRepository notifyRepository;

    public NotifyService(INotifyRepository notifyRepository) {
        this.notifyRepository = notifyRepository;
    }

    @Override
    public NotifyResponse getNotification(String logId, long userId) {
        List<Notify> notifies = new ArrayList<>();
        final int[] totalNew = {0};
        NotifyResponse notifyResponse = NotifyResponse.builder()
                                                    .build();
        List<NotifyDTO> notifyDTOS = notifyRepository.findAllByUserIdAndIsActiveOrderByCreateAtDesc(userId, 1);
        int total = notifyDTOS.size();
        if (total <= 0) {
            logger.warn("{}| User - {} not have notify!", logId, userId);
        } else {
            notifyDTOS.forEach(notifyDTO -> {
                boolean isSeen = notifyDTO.isSeen();
                Notify notify = Notify.builder()
                        .id(notifyDTO.getId())
                        .content(notifyDTO.getContent())
                        .createAt(notifyDTO.getCreateAt())
                        .isSeen(isSeen)
                        .title(notifyDTO.getTitle())
                        .build();
                notifies.add(notify);
                 if (!isSeen) {
                     totalNew[0] += 1;
                 }
            });
        }
        notifyResponse.setNotifies(notifies);
        notifyResponse.setTotalNotify(total);
        notifyResponse.setTotalNotifyNew(totalNew[0]);
        return notifyResponse;
    }
}
