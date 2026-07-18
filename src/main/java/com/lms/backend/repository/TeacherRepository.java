package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.entity.TeacherEntity;
import com.lms.backend.mapper.TeacherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class TeacherRepository {

    private final Firestore firestore;
    private final TeacherMapper teacherMapper;

    public CollectionReference getTeacherCollection() {
        return firestore.collection("teachers");
    }

    public long countActive() throws Exception {
        return getTeacherCollection().whereEqualTo("deleted", false).get().get().size();
    }

    // CREATE
    public String save(TeacherEntity entity) throws ExecutionException, InterruptedException {

        DocumentReference docRef = getTeacherCollection().document();
        entity.setId(docRef.getId());

        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {

            transaction.set(docRef, teacherMapper.teacherToFirestoreMap(entity));

            return docRef.getId();
        });

        return transactionResult.get();
    }

    // READ ONE
    public TeacherEntity findById(String teacherId) throws ExecutionException, InterruptedException {

        DocumentSnapshot snapshot = getTeacherCollection().document(teacherId).get().get();

        if (snapshot.exists()) {
            return teacherMapper.fromDocument(snapshot);
        }

        return null;
    }

    // READ ALL
    public List<TeacherEntity> findAllActive() throws ExecutionException, InterruptedException {

        List<TeacherEntity> result = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = getTeacherCollection().whereEqualTo("deleted", false).get();

        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {

            TeacherEntity entity = teacherMapper.fromDocument(doc);

            if (entity != null) {
                result.add(entity);
            }
        }

        return result;
    }

    // UPDATE
    public String update(TeacherEntity entity) throws ExecutionException, InterruptedException {

        DocumentReference docRef = getTeacherCollection().document(entity.getId());

        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {

            transaction.set(docRef, teacherMapper.teacherToFirestoreMap(entity), SetOptions.merge());

            return "Cập nhật giảng viên thành công!";
        });

        return transactionResult.get();
    }


}