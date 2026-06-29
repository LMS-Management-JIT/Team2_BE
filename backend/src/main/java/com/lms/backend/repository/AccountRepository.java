package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.entity.Account;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Getter
@Repository
public class AccountRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "accounts";

    public CollectionReference getAccountCollection() {
        return firestore.collection("accounts");
    }


    public String save(Account account) throws Exception {
        DocumentReference docRef = firestore.collection(COLLECTION).document();
        account.setId(docRef.getId());

        firestore.runTransaction(transaction -> {
            transaction.set(docRef, account);
            return null;
        }).get();

        return docRef.getId();
    }
    public Optional<Account> findByUsername(String username) {
        try {
            ApiFuture<QuerySnapshot> future = getAccountCollection()
                    .whereEqualTo("email", username)
                    .whereEqualTo("deleted", false)
                    .limit(1)
                    .get();

            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                Account account = doc.toObject(Account.class);
                account.setId(doc.getId());
                return Optional.of(account);
            }

            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to search account by username", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to search account by username", e);
        }
    }

    public DocumentSnapshot findById(String id)
            throws ExecutionException, InterruptedException {

        return firestore.collection(COLLECTION)
                .document(id)
                .get()
                .get();
    }

    public List<QueryDocumentSnapshot> findAllActive()
            throws ExecutionException, InterruptedException {

        return firestore.collection(COLLECTION)
                .whereEqualTo("deleted", false)
                .get()
                .get()
                .getDocuments();
    }

    public String update(String accountId, Map<String, Object> updates)
            throws ExecutionException, InterruptedException {

        DocumentReference docRef =
                firestore.collection("accounts").document(accountId);

        ApiFuture<String> future = firestore.runTransaction(transaction -> {

            transaction.update(docRef, updates);

            return "Update thành công";
        });

        return future.get();
    }

    public String softDelete(String accountId, String deletedBy)
            throws ExecutionException, InterruptedException {

        DocumentReference docRef =
                firestore.collection("accounts").document(accountId);

        ApiFuture<String> future = firestore.runTransaction(transaction -> {

            transaction.update(docRef,
                    "deleted", true,
                    "updateDate", Instant.now(),
                    "updateBy", deletedBy
            );

            return "Đã xóa mềm";
        });

        return future.get();
    }
}