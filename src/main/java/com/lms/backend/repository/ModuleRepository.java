package com.lms.backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.lms.backend.entity.ModuleEntity;
import com.lms.backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class ModuleRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "modules";

    private CollectionReference getModuleCollection() {
        return firestore.collection("modules");
    }

    public long countActive() throws Exception {
        return getModuleCollection()
                .whereEqualTo("deleted", false)
                .get().get()
                .size();
    }
    public List<ModuleEntity> findAllActive() {
        try {
            CollectionReference moduleRef = firestore.collection(COLLECTION_NAME);
            ApiFuture<QuerySnapshot> future = moduleRef.whereEqualTo("deleted", false).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            List<ModuleEntity> result = new ArrayList<>();
            for (QueryDocumentSnapshot doc : documents) {
                result.add(toEntityWithTimestamps(doc));
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Khong the lay danh sach module");
        } catch (ExecutionException e) {
            throw new BadRequestException("Khong the lay danh sach module");
        }
    }

    public Optional<ModuleEntity> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot snapshot = docRef.get().get();

            if (!snapshot.exists()) {
                return Optional.empty();
            }
            return Optional.ofNullable(toEntityWithTimestamps(snapshot));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Khong the tim module");
        } catch (ExecutionException e) {
            throw new BadRequestException("Khong the tim module");
        }
    }

    public ModuleEntity save(ModuleEntity entity, String userId) {
        try {
            CollectionReference moduleRef = firestore.collection(COLLECTION_NAME);
            DocumentReference newDocRef = moduleRef.document();

            Map<String, Object> data = new HashMap<>();
            data.put("module_name", entity.getModuleName());
            data.put("module_code", entity.getModuleCode());
            data.put("description", entity.getDescription());
            data.put("deleted", false);
            data.put("created_by", userId);
            data.put("update_by", userId);
            data.put("create_date", Timestamp.now());
            data.put("update_date", Timestamp.now());

            newDocRef.set(data).get();

            DocumentSnapshot savedDoc = newDocRef.get().get();
            return toEntityWithTimestamps(savedDoc);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Khong the tao module");
        } catch (ExecutionException e) {
            throw new BadRequestException("Khong the tao module");
        }
    }

    public ModuleEntity update(String id, ModuleEntity entity, String userId) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);

            Map<String, Object> data = new HashMap<>();
            data.put("module_name", entity.getModuleName());
            data.put("module_code", entity.getModuleCode());
            data.put("description", entity.getDescription());
            data.put("update_by", userId);
            data.put("update_date", Timestamp.now());

            docRef.update(data).get();

            DocumentSnapshot updatedDoc = docRef.get().get();
            return toEntityWithTimestamps(updatedDoc);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Khong the cap nhat module");
        } catch (ExecutionException e) {
            throw new BadRequestException("Khong the cap nhat module");
        }
    }

    public void softDelete(String id, String userId) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);

            Map<String, Object> data = new HashMap<>();
            data.put("deleted", true);
            data.put("update_by", userId);
            data.put("update_date", Timestamp.now());

            docRef.update(data).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Khong the xoa module");
        } catch (ExecutionException e) {
            throw new BadRequestException("Khong the xoa module");
        }
    }

    private ModuleEntity toEntityWithTimestamps(DocumentSnapshot doc) {
        ModuleEntity entity = doc.toObject(ModuleEntity.class);
        if (entity == null) {
            return null;
        }
        // THÊM MỚI — đọc thủ công vì Lombok không copy @PropertyName sang getter/setter
        entity.setModuleName(doc.getString("module_name"));
        entity.setModuleCode(doc.getString("module_code"));
        entity.setCreatedBy(doc.getString("created_by"));
        entity.setUpdateBy(doc.getString("update_by"));
        Timestamp createTs = doc.getTimestamp("create_date");
        Timestamp updateTs = doc.getTimestamp("update_date");

        if (createTs != null) {
            entity.setCreateDate(LocalDateTime.ofInstant(createTs.toDate().toInstant(), ZoneId.systemDefault()));
        }
        if (updateTs != null) {
            entity.setUpdateDate(LocalDateTime.ofInstant(updateTs.toDate().toInstant(), ZoneId.systemDefault()));
        }
        return entity;
    }
}