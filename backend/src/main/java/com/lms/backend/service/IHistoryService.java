package com.lms.backend.service;

import com.lms.backend.dto.response.HistoryResponse;

import java.util.List;

public interface IHistoryService {

    List<HistoryResponse> getRecentHistory() throws Exception;

}