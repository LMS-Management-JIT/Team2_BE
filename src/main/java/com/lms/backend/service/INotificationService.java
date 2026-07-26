package com.lms.backend.service;

import com.lms.backend.dto.request.SendNotificationRequest;
import com.lms.backend.dto.response.AccountOptionResponse;

import java.util.List;
import java.util.Map;

public interface INotificationService {
    String sendNotification(SendNotificationRequest request);
    List<Map<String, Object>> getUserInbox(String accountId, String type);
    List<AccountOptionResponse> getSelectableAccounts(String roleFilter);
    List<Map<String, Object>> getMyNotifications(String type);
    void markNotificationAsRead(String type, String notificationDocId);
    void markAllNotificationsAsRead();
}