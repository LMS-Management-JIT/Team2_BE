package com.lms.backend.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
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
}