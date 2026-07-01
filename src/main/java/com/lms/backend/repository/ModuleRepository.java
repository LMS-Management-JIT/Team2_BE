package com.lms.backend.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ModuleRepository {

    private final Firestore firestore;

    private CollectionReference getModuleCollection() {
        return firestore.collection("modules");
    }

    public long countActive() throws Exception {
        return getModuleCollection()
                .whereEqualTo("deleted", false)
                .get().get()
                .size();
    }
}