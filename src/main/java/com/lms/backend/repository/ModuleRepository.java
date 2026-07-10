package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.lms.backend.entity.ModuleEntity;
import com.lms.backend.mapper.ModuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class ModuleRepository {

    private final Firestore firestore;
    private final ModuleMapper moduleMapper; // Dùng mapper thay vì Object trực tiếp

    public CollectionReference getModuleCollection() {
        return firestore.collection("modules");
    }

    // Đếm số lượng cho Dashboard
    public long countActive() throws Exception {
        return getModuleCollection()
                .whereEqualTo("deleted", false)
                .get().get()
                .size();
    }

    // CREATE
    public String save(ModuleEntity entity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getModuleCollection().document();
        entity.setId(docRef.getId());

        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {
            Map<String, Object> dataToSave = moduleMapper.toFirestoreMap(entity);
            transaction.set(docRef, dataToSave);
            return docRef.getId();
        });
        return transactionResult.get();
    }

    // READ ALL ACTIVE with basic pagination and optional filtering
    public com.lms.backend.dto.response.PagedResponse<ModuleEntity> findAllActive(int page, int size, String name, String status) throws ExecutionException, InterruptedException {
        List<ModuleEntity> all = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = getModuleCollection().whereEqualTo("deleted", false).get();

        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            ModuleEntity entity = moduleMapper.fromDocument(doc);
            if (entity != null) all.add(entity);
        }

        // apply optional server-side filtering (simple contains, case-insensitive)
        List<ModuleEntity> filtered = new ArrayList<>();
        for (ModuleEntity e : all) {
            boolean matches = true;
            if (name != null && !name.isBlank()) {
                String en = e.getModuleName() == null ? "" : e.getModuleName();
                if (!en.toLowerCase().contains(name.toLowerCase())) matches = false;
            }
            // status field not present on ModuleEntity; ignore status filter for now
            if (matches) filtered.add(e);
        }

        long total = filtered.size();
        int safeSize = Math.max(1, size);
        int safePage = Math.max(0, page);
        int fromIndex = safePage * safeSize;
        int toIndex = Math.min(fromIndex + safeSize, filtered.size());
        List<ModuleEntity> pageItems = new ArrayList<>();
        if (fromIndex < filtered.size()) {
            pageItems = filtered.subList(fromIndex, toIndex);
        }

        int totalPages = (int) ((total + safeSize - 1) / safeSize);

        return new com.lms.backend.dto.response.PagedResponse<>(pageItems, total, safePage, safeSize, totalPages);
    }
    public List<ModuleEntity> findAllActiveByIds(List<String> ids) throws ExecutionException, InterruptedException {
        List<ModuleEntity> result = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = getModuleCollection()
                .whereIn(FieldPath.documentId(), ids)
                .whereEqualTo("deleted", false)
                .get();

        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            ModuleEntity entity = moduleMapper.fromDocument(doc);
            if (entity != null) result.add(entity);
        }
        return result;
    }



    // READ DETAIL BY ID
    public ModuleEntity findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = getModuleCollection().document(id).get().get();
        if (doc.exists()) {
            return moduleMapper.fromDocument(doc);
        }
        return null;
    }

    // UPDATE
    public String update(String moduleId, ModuleEntity updatedEntity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getModuleCollection().document(moduleId);

        ApiFuture<String> transactionResult = firestore.runTransaction(transaction -> {
            Map<String, Object> dataToUpdate = moduleMapper.toFirestoreMap(updatedEntity);
            transaction.set(docRef, dataToUpdate, SetOptions.merge());
            return "Cập nhật thành công Module: " + moduleId;
        });
        return transactionResult.get();
    }

    // SOFT DELETE
    public String softDelete(String moduleId, String deletedBy) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getModuleCollection().document(moduleId);

        ApiFuture<String> future = firestore.runTransaction(transaction -> {
            transaction.update(docRef,
                    "deleted", true,
                    "update_date", Instant.now(),
                    "update_by", deletedBy
            );
            return "Đã xóa mềm Module thành công!";
        });
        return future.get();
    }
    public ModuleEntity findSmartByIdOrCode(String identifier) throws ExecutionException, InterruptedException {
        if (identifier == null || identifier.isBlank()) return null;

        // 1. ƯU TIÊN 1: Thử tìm chính xác theo Document ID của Firestore (VD: FSvPf9BiCHBe4xjSQN5W)
        DocumentSnapshot doc = getModuleCollection().document(identifier).get().get();
        if (doc.exists()) {
            return moduleMapper.fromDocument(doc);
        }

        // 2. ƯU TIÊN 2: Nếu không thấy, tự động chuyển sang dò trong cột `module_code` (VD: 03281432-c940-...)
        QuerySnapshot queryByCode = getModuleCollection()
                .whereEqualTo("module_code", identifier)
                .whereEqualTo("deleted", false)
                .limit(1)
                .get()
                .get();

        if (!queryByCode.isEmpty()) {
            return moduleMapper.fromDocument(queryByCode.getDocuments().get(0));
        }

        // 3. Nếu vẫn không thấy, trả về null để code bên ngoài tự bỏ qua
        return null;
    }
}