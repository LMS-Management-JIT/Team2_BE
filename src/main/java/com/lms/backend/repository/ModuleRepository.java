package com.lms.backend.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.lms.backend.entity.ModuleEntity;
import com.lms.backend.mapper.ModuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor // Tự động Inject Bean Firestore vào constructor (chỉ giữ lại 1 cái)
public class ModuleRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "modules";

    // 1. Hàm lấy danh sách học phần đang hoạt động
    public List<ModuleEntity> findAllActive() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("deleted", false)
                .get().get().getDocuments();

        List<ModuleEntity> modules = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            ModuleEntity entity = ModuleEntity.builder()
                    .id(doc.getId())
                    .moduleName(doc.getString("module_name"))
                    .moduleCode(doc.getString("module_code"))
                    .description(doc.getString("description"))
                    .deleted(doc.getBoolean("deleted") != null && doc.getBoolean("deleted"))
                    .createdBy(doc.getString("created_by"))
                    .updateBy(doc.getString("update_by"))
                    .createDate(ModuleMapper.toLocalDateTime(doc.getTimestamp("create_date"))) // Đã fix sang toInstant
                    .updateDate(ModuleMapper.toLocalDateTime(doc.getTimestamp("update_date"))) // Đã fix sang toInstant
                    .build();
            modules.add(entity);
        }
        return modules;
    }

    // 2. Hàm đếm số lượng học phần hoạt động (Mới merge từ develop về được dọn dẹp chuẩn chỉnh)
    public long countActive() throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION_NAME)
                .whereEqualTo("deleted", false)
                .get().get()
                .size();
    }

    // 3. Tìm kiếm học phần theo ID
    public ModuleEntity findById(String id) throws ExecutionException, InterruptedException {
        var docRef = firestore.collection(COLLECTION_NAME).document(id).get().get();
        if (!docRef.exists()) return null;

        return ModuleEntity.builder()
                .id(docRef.getId())
                .moduleName(docRef.getString("module_name"))
                .moduleCode(docRef.getString("module_code"))
                .description(docRef.getString("description"))
                .deleted(docRef.getBoolean("deleted") != null && docRef.getBoolean("deleted"))
                .createdBy(docRef.getString("created_by"))
                .updateBy(docRef.getString("update_by"))
                .createDate(ModuleMapper.toLocalDateTime(docRef.getTimestamp("create_date"))) // Đã fix sang toInstant
                .updateDate(ModuleMapper.toLocalDateTime(docRef.getTimestamp("update_date"))) // Đã fix sang toInstant
                .build();
    }

    // 4. Lưu hoặc Cập nhật học phần
    public void save(ModuleEntity entity) throws ExecutionException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("module_name", entity.getModuleName());
        data.put("module_code", entity.getModuleCode());
        data.put("description", entity.getDescription());
        data.put("deleted", entity.isDeleted());
        data.put("created_by", entity.getCreatedBy());
        data.put("update_by", entity.getUpdateBy());
        data.put("create_date", ModuleMapper.toTimestamp(entity.getCreateDate()));
        data.put("update_date", ModuleMapper.toTimestamp(entity.getUpdateDate()));

        firestore.collection(COLLECTION_NAME).document(entity.getId()).set(data).get();
    }
}