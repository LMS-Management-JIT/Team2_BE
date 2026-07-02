package com.lms.backend.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.lms.backend.entity.HistoryEntity;
import com.lms.backend.mapper.HistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class HistoryRepository {

    private final Firestore firestore;
    private final HistoryMapper historyMapper;

    private CollectionReference getHistoryCollection() {
        return firestore.collection("history");
    }
    public List<HistoryEntity> findRecent(int limit)
            throws Exception {
        return getHistoryCollection()
                .orderBy(
                        "created_at",
                        Query.Direction.DESCENDING
                )
                .limit(limit)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(historyMapper::fromDocument)
                .collect(Collectors.toList());
    }
}