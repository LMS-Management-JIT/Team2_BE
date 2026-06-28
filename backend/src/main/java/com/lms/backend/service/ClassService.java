package com.lms.backend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.dto.request.ClassRequest;
import com.lms.backend.dto.response.ClassResponse;
import com.lms.backend.entity.ClassEntity;
import com.lms.backend.mapper.ClassMapper;
import com.lms.backend.repository.ClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ClassService {

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private ClassMapper classMapper;

    // CREATE
    public String createClass(ClassRequest request) throws ExecutionException, InterruptedException {
        ClassEntity entity = classMapper.toEntity(request);
        entity.setCreateDate(new Date());
        entity.setCreatedBy(request.getRequestedBy());
        entity.setDeleted(false);

        DocumentReference docRef = classRepository.getClassCollection().document();
        entity.setId(docRef.getId());

        ApiFuture<String> transactionResult = classRepository.getFirestore().runTransaction(transaction -> {

            transaction.set(docRef, entity);

            return docRef.getId();
        });

        return transactionResult.get();
    }

    // READ
    public List<ClassResponse> getAllClasses() throws ExecutionException, InterruptedException {
        List<ClassResponse> responseList = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = classRepository.getClassCollection()
                .whereEqualTo("deleted", false).get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        for (QueryDocumentSnapshot doc : documents) {
            ClassEntity entity = doc.toObject(ClassEntity.class);
            entity.setId(doc.getId());
            responseList.add(classMapper.toResponse(entity));
        }

        return responseList;
    }

    // UPDATE
    public String updateClass(String classId, ClassRequest request) throws ExecutionException, InterruptedException {
        DocumentReference docRef = classRepository.getClassCollection().document(classId);

        ApiFuture<String> transactionResult = classRepository.getFirestore().runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(docRef).get();
            if (!snapshot.exists()) {
                throw new Exception("Lớp học không tồn tại, không thể cập nhật!"); // Kích hoạt Rollback
            }

            ClassEntity updatedEntity = classMapper.toEntity(request);
            updatedEntity.setUpdateDate(new Date());
            updatedEntity.setUpdateBy(request.getRequestedBy());

            transaction.set(docRef, updatedEntity, SetOptions.merge());

            return "Cập nhật thành công Lớp: " + classId;
        });

        return transactionResult.get();
    }

    // DELETE SOFT
    public String deleteClass(String classId, String deletedBy) throws ExecutionException, InterruptedException {
        DocumentReference docRef = classRepository.getClassCollection().document(classId);

        ApiFuture<String> transactionResult = classRepository.getFirestore().runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(docRef).get();
            if (!snapshot.exists()) {
                throw new Exception("Lớp học không tồn tại!");
            }

            transaction.update(docRef,
                    "deleted", true,
                    "updateDate", new Date(),
                    "updateBy", deletedBy
            );

            return "Đã xóa mềm thành công!";
        });

        return transactionResult.get();
    }
}