package com.lms.backend.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ClassRepository {

    @Autowired
    private Firestore firestore;

    public CollectionReference getClassCollection() {
        return firestore.collection("classes");
    }
    public Firestore getFirestore() {
        return firestore;
    }
}