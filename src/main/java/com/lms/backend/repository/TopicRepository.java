package com.lms.backend.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TopicRepository {

    private final Firestore firestore;

    public CollectionReference getTopicCollection() {
        return firestore.collection("topics");
    }

    public CollectionReference getLessonCollection() {
        return firestore.collection("lessons");
    }

    public Firestore getFirestore() {
        return firestore;
    }
}