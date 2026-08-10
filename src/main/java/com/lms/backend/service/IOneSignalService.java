package com.lms.backend.service;

import java.util.List;
import java.util.Map;

public interface IOneSignalService {
    void sendToExternalUserIds(List<String> externalUserIds, String title, String body, Map<String, Object> data);
}
