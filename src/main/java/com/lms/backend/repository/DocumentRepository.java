package com.lms.backend.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentRepository {

    private static final String DOCUMENT_COLLECTION = "documents";

    private final Firestore firestore;

    public DocumentRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public CollectionReference getDocumentCollection() {
        return firestore.collection(DOCUMENT_COLLECTION);
    }

    public Firestore getFirestore() {
        return firestore;
    }
}