package com.lms.backend.service;

import com.lms.backend.dto.request.HistoryRequest;
import com.lms.backend.dto.response.HistoryResponse;

import java.util.List;

public interface IHistoryService {
    List<HistoryResponse> getRecentHistories(int limit);
    void record(HistoryRequest request);
}