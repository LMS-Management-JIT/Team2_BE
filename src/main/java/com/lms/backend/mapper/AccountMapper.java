package com.lms.backend.mapper;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.lms.backend.dto.request.AccountRequest;
import com.lms.backend.dto.response.AccountResponse;
import com.lms.backend.entity.AccountEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class AccountMapper {

    public AccountEntity toEntity(AccountRequest request) {
        if (request == null) return null;

        return AccountEntity.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .roleId(request.getRoleId())
                .status(request.getStatus())
                .deleted(request.getDeleted() != null ? request.getDeleted() : false)
                .createdBy(request.getCreatedBy())
                .updateBy(request.getUpdateBy())
                .build();
    }

    public AccountResponse toResponse(AccountEntity entity) {
        if (entity == null) return null;

        AccountResponse response = new AccountResponse();
        BeanUtils.copyProperties(entity, response);
        return response;
    }

    public void updateEntityFromRequest(AccountRequest request, AccountEntity existing) {
        if (request == null || existing == null) return;

        existing.setEmail(request.getEmail());
        if (request.getPassword() != null) existing.setPassword(request.getPassword());
        existing.setRoleId(request.getRoleId());
        existing.setStatus(request.getStatus());
        if (request.getDeleted() != null) existing.setDeleted(request.getDeleted());
        existing.setUpdateBy(request.getRequestedBy() != null ? request.getRequestedBy() : request.getUpdateBy());
    }

    public Map<String, Object> toFirestoreMap(AccountEntity entity) {
        if (entity == null) return null;

        Map<String, Object> map = new HashMap<>();
        map.put("email", entity.getEmail());
        map.put("password", entity.getPassword());
        map.put("role_id", entity.getRoleId());
        map.put("status", entity.getStatus());
        map.put("deleted", entity.getDeleted());
        map.put("created_by", entity.getCreatedBy());
        map.put("update_by", entity.getUpdateBy());

        map.put("create_date", toTimestamp(entity.getCreateDate()));
        map.put("update_date", toTimestamp(entity.getUpdateDate()));

        return map;
    }

    public AccountEntity fromDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        AccountEntity entity = AccountEntity.builder()
                .email(doc.getString("email"))
                .password(doc.getString("password"))
                .roleId(doc.getString("role_id"))
                .status(doc.getString("status"))
                .deleted(doc.getBoolean("deleted") != null ? doc.getBoolean("deleted") : false)
                .createdBy(doc.getString("created_by"))
                .updateBy(doc.getString("update_by"))
                .createDate(safeExtractDate(doc, "create_date"))
                .updateDate(safeExtractDate(doc, "update_date"))
                .build();

        entity.setId(doc.getId());
        return entity;
    }

    private LocalDateTime safeExtractDate(DocumentSnapshot doc, String field) {
        try {
            Object raw = doc.get(field);
            if (raw == null) return null;

            if (raw instanceof Timestamp ts) {
                return toLocalDateTime(ts.toDate());
            }
            if (raw instanceof Date date) {
                return toLocalDateTime(date);
            }
            if (raw instanceof Map<?, ?> map) {
                // Trường hợp dữ liệu bị lưu sai dạng object thay vì Timestamp thật
                Object seconds = map.get("seconds") != null ? map.get("seconds") : map.get("_seconds");
                if (seconds instanceof Number secNum) {
                    return LocalDateTime.ofInstant(Instant.ofEpochSecond(secNum.longValue()), ZoneId.systemDefault());
                }
                System.err.println("[AccountMapper] Document " + doc.getId() + " có field '" + field
                        + "' bị lưu sai định dạng (Map không nhận diện được): " + map);
                return null;
            }

            System.err.println("[AccountMapper] Document " + doc.getId() + " có field '" + field
                    + "' kiểu dữ liệu không xác định: " + raw.getClass());
            return null;
        } catch (Exception e) {
            System.err.println("[AccountMapper] Lỗi đọc field '" + field + "' của document " + doc.getId()
                    + ": " + e.getMessage());
            return null;
        }
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