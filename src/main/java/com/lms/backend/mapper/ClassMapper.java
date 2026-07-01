package com.lms.backend.mapper;

import com.lms.backend.dto.request.ClassRequest;
import com.lms.backend.dto.response.ClassResponse;
import com.lms.backend.entity.ClassEntity;
import org.springframework.stereotype.Component;

@Component
public class ClassMapper {

    // 1. Nhận Request từ Frontend -> Chuyển thành Entity để lưu DB
    public ClassEntity toEntity(ClassRequest request) {
        if (request == null) return null;

        return ClassEntity.builder()
                .className(request.getClassName())
                .description(request.getDescription())
                .programe(request.getPrograme())
                .status(request.getStatus())
                .courseId(request.getCourseId())
                .startDateClass(request.getStartDateClass())
                .endDateClass(request.getEndDateClass())
                .build();
    }

    // 2. Lấy Entity từ DB -> Chuyển thành Response trả về Frontend
    public ClassResponse toResponse(ClassEntity entity) {
        if (entity == null) return null;

        ClassResponse response = new ClassResponse();
        response.setId(entity.getId());
        response.setClassName(entity.getClassName());
        response.setDescription(entity.getDescription());
        response.setPrograme(entity.getPrograme());
        response.setStatus(entity.getStatus());
        response.setCreatedBy(entity.getCreatedBy());
        response.setUpdateBy(entity.getUpdateBy());
        response.setDeleted(entity.isDeleted());
        response.setCourseId(entity.getCourseId());

        // Gán trực tiếp LocalDateTime
        response.setStartDateClass(entity.getStartDateClass());
        response.setEndDateClass(entity.getEndDateClass());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());

        return response;
    }

    // 3. Cập nhật Entity từ Request
    public void updateEntityFromRequest(ClassRequest request, ClassEntity existingEntity) {
        if (request == null || existingEntity == null) return;

        existingEntity.setClassName(request.getClassName());
        existingEntity.setDescription(request.getDescription());
        existingEntity.setPrograme(request.getPrograme());
        existingEntity.setStatus(request.getStatus());
        existingEntity.setCourseId(request.getCourseId());
        existingEntity.setStartDateClass(request.getStartDateClass());
        existingEntity.setEndDateClass(request.getEndDateClass());
    }
}