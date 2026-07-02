package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.lms.backend.entity.RoleEntity;
import com.lms.backend.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class RoleRepository {

    private final Firestore firestore;

    private final RoleMapper roleMapper;

    private static final String COLLECTION = "roles";

    private CollectionReference getRoleCollection() {
        return firestore.collection(COLLECTION);
    }

    public String save(RoleEntity role) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getRoleCollection().document();
        role.setId(docRef.getId());

        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {
            Map<String, Object> dataToSave = roleMapper.toFirestoreMap(role);
            transaction.set(docRef, dataToSave);
            return docRef.getId();
        });
        return transactionResult.get();
    }

    public List<RoleEntity> findAllActive() throws ExecutionException, InterruptedException {
        List<RoleEntity> result = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = getRoleCollection()
                .whereEqualTo("deleted", false)
                .get();

        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            RoleEntity role = roleMapper.fromDocument(doc);
            if (role != null) {
                role.setId(doc.getId());
                result.add(role);
            }
        }

        return result;
    }

    public Optional<RoleEntity> findById(String roleId) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = getRoleCollection().document(roleId).get().get();
        if (!snapshot.exists()) {
            return Optional.empty();
        }

        RoleEntity role = roleMapper.fromDocument(snapshot);
        if (role != null) role.setId(snapshot.getId());
        return Optional.ofNullable(role);
    }

    public String update(String roleId, RoleEntity updatedRole) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getRoleCollection().document(roleId);

        ApiFuture<String> future = firestore.runTransaction(transaction -> {
            Map<String, Object> map = roleMapper.toFirestoreMap(updatedRole);
            if (map == null) map = new java.util.HashMap<>();
            map.put("update_date", Instant.now());
            if (updatedRole.getUpdateBy() != null) map.put("update_by", updatedRole.getUpdateBy());

            transaction.set(docRef, map, SetOptions.merge());
            return "Update thành công";
        });

        return future.get();
    }

    public String softDelete(String roleId, String deletedBy) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getRoleCollection().document(roleId);

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
