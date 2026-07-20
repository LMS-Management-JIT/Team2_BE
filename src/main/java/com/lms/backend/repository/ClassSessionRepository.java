package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.lms.backend.entity.ClassSessionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class ClassSessionRepository {

    private final Firestore firestore;

    public CollectionReference getClassSessionCollection(String classId) {
        return firestore.collection("classes").document(classId).collection("class_sessions");
    }

    public List<ClassSessionEntity> findByClassId(String classId) throws ExecutionException, InterruptedException {
        List<ClassSessionEntity> result = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = getClassSessionCollection(classId)
                .whereEqualTo("deleted", false)
                .get();

        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            result.add(ClassSessionEntity.builder()
                    .id(doc.getId())
                    .classId(doc.getString("class_id"))
                    .title(doc.getString("title"))
                    .sessionDate(doc.getString("session_date"))
                    .startTime(doc.getString("start_time"))
                    .endTime(doc.getString("end_time"))
                    .status(doc.getString("status"))
                    .deleted(doc.getBoolean("deleted"))
                    .build());
        }

        return result;
    }

    public List<ClassSessionEntity> findInDateRange(String fromDate, String toDate) throws ExecutionException, InterruptedException {
        List<ClassSessionEntity> result = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = firestore.collectionGroup("class_sessions")
                .whereEqualTo("deleted", false)
                .whereGreaterThanOrEqualTo("session_date", fromDate)
                .whereLessThanOrEqualTo("session_date", toDate)
                .get();

        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            result.add(ClassSessionEntity.builder()
                    .id(doc.getId())
                    .classId(doc.getString("class_id"))
                    .title(doc.getString("title"))
                    .sessionDate(doc.getString("session_date"))
                    .startTime(doc.getString("start_time"))
                    .endTime(doc.getString("end_time"))
                    .status(doc.getString("status"))
                    .deleted(doc.getBoolean("deleted"))
                    .build());
        }

        return result;
    }
}