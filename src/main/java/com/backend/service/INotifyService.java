package com.backend.service;

import com.backend.model.response.NotifyResponse;

public interface INotifyService {
    NotifyResponse getNotification(String logId, long userId);
    void saveNotification(String logId, long userId, String title, String content);
}
