package com.lms.backend.service.impl;

import com.lms.backend.dto.response.HistoryResponse;
import com.lms.backend.exception.DashboardException;
import com.lms.backend.mapper.HistoryMapper;
import com.lms.backend.repository.HistoryRepository;
import com.lms.backend.service.IHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements IHistoryService {

    private final HistoryRepository historyRepository;
    private final HistoryMapper historyMapper;

    @Override
    public List<HistoryResponse> getRecentHistories(
            int limit) {
        try {
            return historyRepository
                    .findRecent(limit)
                    .stream()
                    .map(historyMapper::toHistoryResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new DashboardException(
                    "Không thể lấy lịch sử gần đây"
            );
        }
    }
}