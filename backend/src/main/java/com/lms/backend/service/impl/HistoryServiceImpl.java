package com.lms.backend.service.impl;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.lms.backend.dto.response.HistoryResponse;
import com.lms.backend.service.IHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements IHistoryService {

    private final Firestore firestore;

    @Override
    public List<HistoryResponse> getRecentHistory() throws Exception {

        QuerySnapshot snapshot = firestore
                .collection("history")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .get();

        List<HistoryResponse> result = new ArrayList<>();

        for (DocumentSnapshot doc : snapshot.getDocuments()) {

            HistoryResponse history = new HistoryResponse();

            history.setAction(doc.getString("action"));

            history.setActorId(doc.getString("actorId"));
            history.setActorName(doc.getString("actorName"));

            history.setEntityType(doc.getString("entityType"));
            history.setEntityId(doc.getString("entityId"));
            history.setEntityName(doc.getString("entityName"));

            history.setChanges(
                    (Map<String, Object>) doc.get("changes")
            );

            Timestamp timestamp = doc.getTimestamp("createdAt");

            if (timestamp != null) {
                history.setCreatedAt(String.valueOf(timestamp));
            }

            result.add(history);
        }

        return result;
    }
}