package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.entity.AccountEntity;
import com.lms.backend.mapper.AccountMapper;
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

    private final Firestore firestore;

    private static final String COLLECTION = "accounts";

    private final AccountMapper accountMapper;


    public AccountRepository(Firestore firestore, AccountMapper accountMapper) {
        this.firestore = firestore;
        this.accountMapper = accountMapper;
    }


    public CollectionReference getAccountCollection() {
        return firestore.collection("accounts");
    }


    public String save(AccountEntity account) throws Exception {
        DocumentReference docRef = firestore.collection(COLLECTION).document();
        account.setId(docRef.getId());
        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {
            Map<String, Object> dataToSave = accountMapper.toFirestoreMap(account);
            transaction.set(docRef, dataToSave);
            return docRef.getId();
        });
        return transactionResult.get();
    }
        public Optional<AccountEntity> findByUsername (String username){
            try {
                ApiFuture<QuerySnapshot> future = getAccountCollection()
                        .whereEqualTo("email", username)
                        .whereEqualTo("deleted", false)
                        .limit(1)
                        .get();

                for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                    AccountEntity account = accountMapper.fromDocument(doc);
                    if (account != null) account.setId(doc.getId());
                    return Optional.ofNullable(account);
                }

                return Optional.empty();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Failed to search account by username", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Failed to search account by username", e);
            }
        }

        public DocumentSnapshot findById (String id)
            throws ExecutionException, InterruptedException {

            return firestore.collection(COLLECTION)
                    .document(id)
                    .get()
                    .get();
        }

        public List<QueryDocumentSnapshot> findAllActive ()
            throws ExecutionException, InterruptedException {

            return firestore.collection(COLLECTION)
                    .whereEqualTo("deleted", false)
                    .get()
                    .get()
                    .getDocuments();
        }

        public String update (String accountId, Map < String, Object > updates)
            throws ExecutionException, InterruptedException {

            DocumentReference docRef =
                    firestore.collection("accounts").document(accountId);

            ApiFuture<String> future = firestore.runTransaction(transaction -> {

                transaction.update(docRef, updates);

                return "Update thành công";
            });

            return future.get();
        }

        public String softDelete (String accountId, String deletedBy)
            throws ExecutionException, InterruptedException {

            DocumentReference docRef =
                    firestore.collection("accounts").document(accountId);

            ApiFuture<String> future = firestore.runTransaction(transaction -> {

                transaction.update(docRef,
                        "deleted", true,
                        "update_date", Instant.now(),
                        "update_by", deletedBy
                );

                return "Đã xóa mềm";
            });

            return future.get();
        }
    }