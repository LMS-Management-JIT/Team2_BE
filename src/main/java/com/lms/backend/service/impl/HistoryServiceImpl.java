package com.lms.backend.service.impl;

import com.lms.backend.dto.request.HistoryRequest;
import com.lms.backend.dto.response.HistoryResponse;
import com.lms.backend.entity.HistoryEntity;
import com.lms.backend.exception.DashboardException;
import com.lms.backend.mapper.HistoryMapper;
import com.lms.backend.repository.HistoryRepository;
import com.lms.backend.service.IHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static reactor.netty.http.HttpConnectionLiveness.log;

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
    @Override
    @Async
    public void record(HistoryRequest request) {
        HistoryEntity history = HistoryEntity.builder()
                .action(request.getAction())
                .actorId(request.getActorId())
                .actorName(request.getActorName())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .entityName(request.getEntityName())
                .changes(removeUnchangedFields(request.getChanges()))
                .createdAt(LocalDateTime.now())
                .build();

        try {
            historyRepository.save(history);
        } catch (Exception e) {
            log.error("Không thể ghi lịch sử cho entity {}: {}", request.getEntityId(), e.getMessage());
        }
    }

    private Map<String, HistoryEntity.ChangeDetail> removeUnchangedFields(
            Map<String, HistoryEntity.ChangeDetail> changes) {
        if (changes == null) {
            return Map.of();
        }
        return changes.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}