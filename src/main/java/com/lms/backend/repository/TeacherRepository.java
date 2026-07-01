package com.lms.backend.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TeacherRepository {

    private final Firestore firestore;

    private CollectionReference getTeacherCollection() {
        return firestore.collection("teachers");
    }

    public long countActive() throws Exception {
        return getTeacherCollection()
                .whereEqualTo("deleted", false)
                .get().get()
                .size();
    }
}