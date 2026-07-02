package com.lms.backend.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.entity.RoleEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class RoleMapper {

    public Map<String, Object> toFirestoreMap(RoleEntity entity) {
        if (entity == null) return null;

        Map<String, Object> map = new HashMap<>();
        map.put("name", entity.getName());
        map.put("deleted", entity.getDeleted());
        map.put("created_by", entity.getCreatedBy());
        map.put("update_by", entity.getUpdateBy());

        // Convert LocalDateTime to java.util.Date for Firestore
        map.put("create_date", toTimestamp(entity.getCreateDate()));
        map.put("update_date", toTimestamp(entity.getUpdateDate()));

        return map;
    }

    public RoleEntity fromDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        RoleEntity entity = RoleEntity.builder()
                .name(doc.getString("name"))
                .deleted(doc.getBoolean("deleted") != null ? doc.getBoolean("deleted") : false)
                .createdBy(doc.getString("created_by"))
                .updateBy(doc.getString("update_by"))
                .createDate(toLocalDateTime(doc.getDate("create_date")))
                .updateDate(toLocalDateTime(doc.getDate("update_date")))
                .build();

        entity.setId(doc.getId());
        return entity;
    }

    private Date toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
