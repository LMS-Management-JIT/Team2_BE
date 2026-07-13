package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.entity.ProfileEntity;
import com.lms.backend.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class ProfileRepository {

    private final Firestore firestore;


    private final StudentMapper studentMapper;

    public CollectionReference getProfileCollection() {
        return firestore.collection("profiles");
    }

    // CREATE
    public String save(ProfileEntity entity) throws ExecutionException, InterruptedException {

        DocumentReference docRef = getProfileCollection().document();
        entity.setId(docRef.getId());

        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {

            transaction.set(docRef, studentMapper.profileToFirestoreMap(entity));

            return docRef.getId();
        });

        return transactionResult.get();
    }

    // READ ONE
    public ProfileEntity findById(String profileId) throws ExecutionException, InterruptedException {

        DocumentSnapshot snapshot = getProfileCollection().document(profileId).get().get();

        if (snapshot.exists()) {
            return studentMapper.profileFromDocument(snapshot);
        }

        return null;
    }

    // READ ALL
    public List<ProfileEntity> findAllActive() throws ExecutionException, InterruptedException {

        List<ProfileEntity> result = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = getProfileCollection().whereEqualTo("deleted", false).get();

        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {

            ProfileEntity entity = studentMapper.profileFromDocument(doc);

            if (entity != null) {
                result.add(entity);
            }
        }

        return result;
    }

    // UPDATE
    public String update(ProfileEntity entity) throws ExecutionException, InterruptedException {

        DocumentReference docRef = getProfileCollection().document(entity.getId());

        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {

            transaction.set(docRef, studentMapper.profileToFirestoreMap(entity), SetOptions.merge());

            return "Cập nhật Profile thành công!";
        });

        return transactionResult.get();
    }

    // SOFT DELETE
    public String softDelete(ProfileEntity entity) throws ExecutionException, InterruptedException {

        DocumentReference docRef = getProfileCollection().document(entity.getId());

        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {

            transaction.set(docRef, studentMapper.profileToFirestoreMap(entity), SetOptions.merge());

            return "Đã xóa mềm Profile!";
        });

        return transactionResult.get();
    }

    public boolean existsByPhoneNumber(String phoneNumber) throws ExecutionException, InterruptedException {

        return !getProfileCollection().whereEqualTo("phone_number", phoneNumber).whereEqualTo("deleted", false).get().get().isEmpty();
    }

    public boolean existsByPhoneNumberExceptId(String phoneNumber, String profileId) throws ExecutionException, InterruptedException {

        List<QueryDocumentSnapshot> docs = getProfileCollection().whereEqualTo("phone_number", phoneNumber).whereEqualTo("deleted", false).get().get().getDocuments();

        for (QueryDocumentSnapshot doc : docs) {

            if (!doc.getId().equals(profileId)) {
                return true;
            }
        }

        return false;
    }
}