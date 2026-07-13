package com.lms.backend.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.ModuleRequest;
import com.lms.backend.dto.response.ModuleResponse;
import com.lms.backend.entity.ModuleEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class ModuleMapper {

    // 1. Convert Request -> Entity
    public ModuleEntity toEntity(ModuleRequest request) {
        if (request == null) return null;
        return ModuleEntity.builder()
                .moduleCode(request.getModuleCode())
                .moduleName(request.getModuleName())
                .description(request.getDescription())
                .deleted(request.getDeleted() != null ? request.getDeleted() : false)
                .build();
    }

    // 2. Convert Entity -> Response
    public ModuleResponse toResponse(ModuleEntity entity) {
        if (entity == null) return null;
        ModuleResponse response = new ModuleResponse();
        BeanUtils.copyProperties(entity, response);
        return response;
    }

    public void updateEntityFromRequest(ModuleRequest request, ModuleEntity existing) {
        if (request == null || existing == null) return;
        existing.setModuleCode(request.getModuleCode());
        existing.setModuleName(request.getModuleName());
        existing.setDescription(request.getDescription());
        if (request.getDeleted() != null) existing.setDeleted(request.getDeleted());
    }

    // =====================================================================
    // CÁC HÀM XỬ LÝ FIRESTORE MANUAL (NÉ LỖI REFLECTION JAVA 21)
    // =====================================================================

    // 3. Convert Entity -> Map để lưu xuống Firebase
    public Map<String, Object> toFirestoreMap(ModuleEntity entity) {
        if (entity == null) return null;

        Map<String, Object> map = new HashMap<>();
        map.put("module_code", entity.getModuleCode());
        map.put("module_name", entity.getModuleName());
        map.put("description", entity.getDescription());
        map.put("deleted", entity.isDeleted());
        map.put("created_by", entity.getCreatedBy());
        map.put("update_by", entity.getUpdateBy());

        // Đổi LocalDateTime thành Date cho Firestore
        map.put("create_date", toTimestamp(entity.getCreateDate()));
        map.put("update_date", toTimestamp(entity.getUpdateDate()));

        return map;
    }

    // 4. Convert Document từ Firebase -> Entity
    public ModuleEntity fromDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        ModuleEntity entity = ModuleEntity.builder()
                .id(doc.getId())
                .moduleCode(doc.getString("module_code"))
                .moduleName(doc.getString("module_name"))
                .description(doc.getString("description"))
                .deleted(doc.getBoolean("deleted") != null ? doc.getBoolean("deleted") : false)
                .createdBy(doc.getString("created_by"))
                .updateBy(doc.getString("update_by"))
                .createDate(toLocalDateTime(doc.getDate("create_date")))
                .updateDate(toLocalDateTime(doc.getDate("update_date")))
                .build();

        return entity;
    }

    // Helper: Chuyển đổi thời gian
    private Date toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}