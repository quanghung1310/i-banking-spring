package com.backend.service;

import com.backend.model.response.NotifyResponse;

public interface INotifyService {
    NotifyResponse getNotification(String logId, long userId);
}
