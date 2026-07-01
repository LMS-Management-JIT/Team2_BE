package com.lms.backend.repository;

import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ModuleRepository {

    private final Firestore firestore;  // chỉ Repository mới inject Firestore

    public long countActive() throws Exception {
        return firestore.collection("modules")
                .whereEqualTo("deleted", false)
                .get().get()
                .size();
    }
}