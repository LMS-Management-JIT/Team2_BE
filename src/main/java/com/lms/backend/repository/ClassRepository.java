package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.entity.ClassEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class ClassRepository {

    @Autowired
    private Firestore firestore;

    private CollectionReference getClassCollection() {
        return firestore.collection("classes");
    }

    // CREATE
    public String save(ClassEntity entity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getClassCollection().document();
        entity.setId(docRef.getId()); // Gán ID tự sinh của Firestore

        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {
            transaction.set(docRef, entity);
            return docRef.getId();
        });
        return transactionResult.get();
    }

    // READ
    public List<ClassEntity> findAllActive() throws ExecutionException, InterruptedException {
        List<ClassEntity> result = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = getClassCollection().whereEqualTo("deleted", false).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        for (QueryDocumentSnapshot doc : documents) {
            ClassEntity entity = doc.toObject(ClassEntity.class);
            entity.setId(doc.getId());
            result.add(entity);
        }
        return result;
    }

    // Lấy 1 Document để kiểm tra tồn tại (Dùng cho Update/Delete)
    public ClassEntity findById(String classId) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = getClassCollection().document(classId).get().get();
        if (snapshot.exists()) {
            ClassEntity entity = snapshot.toObject(ClassEntity.class);
            entity.setId(snapshot.getId());
            return entity;
        }
        return null;
    }

    // UPDATE
    public String update(String classId, ClassEntity updatedEntity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getClassCollection().document(classId);
        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {
            transaction.set(docRef, updatedEntity, SetOptions.merge());
            return "Cập nhật thành công Lớp: " + classId;
        });
        return transactionResult.get();
    }

    // DELETE SOFT
    public String softDelete(String classId, Map<String, Object> updates) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getClassCollection().document(classId);
        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {
            transaction.update(docRef, updates);
            return "Đã xóa mềm thành công!";
        });
        return transactionResult.get();
    }
}