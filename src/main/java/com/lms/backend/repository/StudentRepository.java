package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.entity.StudentEntity;
import com.lms.backend.mapper.StudentMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
@Getter

public class StudentRepository {

    private final Firestore firestore;

    private CollectionReference getStudentCollection() {
        return firestore.collection("students");
    }

    public long countActive() throws Exception {
        return getStudentCollection()
                .whereEqualTo("deleted", false)
                .get().get()
                .size();
    }


    @Autowired
    private StudentMapper studentMapper;

    // CREATE
    public String save(StudentEntity entity)
            throws ExecutionException, InterruptedException {

        DocumentReference docRef = getStudentCollection().document();
        entity.setId(docRef.getId());

        ApiFuture<String> transactionResult =
                firestore.runTransaction(transaction -> {

                    transaction.set(
                            docRef,
                            studentMapper.studentToFirestoreMap(entity)
                    );

                    return docRef.getId();
                });

        return transactionResult.get();
    }

    // READ ONE
    public StudentEntity findById(String studentId)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot snapshot = getStudentCollection()
                .document(studentId)
                .get()
                .get();

        if (snapshot.exists()) {
            return studentMapper.fromDocument(snapshot);
        }

        return null;
    }

    // READ ALL
    public List<StudentEntity> findAllActive()
            throws ExecutionException, InterruptedException {

        List<StudentEntity> result = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = getStudentCollection()
                .whereEqualTo("deleted", false)
                .get();

        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {

            StudentEntity entity = studentMapper.fromDocument(doc);

            if (entity != null) {
                result.add(entity);
            }
        }

        return result;
    }

    // UPDATE
    public String update(StudentEntity entity)
            throws ExecutionException, InterruptedException {

        DocumentReference docRef =
                getStudentCollection().document(entity.getId());

        ApiFuture<String> transactionResult =
                firestore.runTransaction(transaction -> {

                    transaction.set(
                            docRef,
                            studentMapper.studentToFirestoreMap(entity),
                            SetOptions.merge()
                    );

                    return "Cập nhật học viên thành công!";
                });

        return transactionResult.get();
    }

    // SOFT DELETE
    public String softDelete(StudentEntity entity)
            throws ExecutionException, InterruptedException {

        DocumentReference docRef =
                getStudentCollection().document(entity.getId());

        ApiFuture<String> transactionResult =
                firestore.runTransaction(transaction -> {

                    transaction.set(
                            docRef,
                            studentMapper.studentToFirestoreMap(entity),
                            SetOptions.merge()
                    );

                    return "Đã xóa học viên thành công!";
                });

        return transactionResult.get();
    }



}