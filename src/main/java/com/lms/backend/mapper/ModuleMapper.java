package com.lms.backend.mapper;

import com.lms.backend.dto.request.ModuleRequest;
import com.lms.backend.dto.response.ModuleResponse;
import com.lms.backend.entity.ModuleEntity;
import org.springframework.beans.BeanUtils;

public class ModuleMapper {

    private ModuleMapper() {
        // utility class
    }

    public static ModuleEntity toEntity(ModuleRequest request) {
        if (request == null) {
            return null;
        }
        ModuleEntity entity = new ModuleEntity();
        BeanUtils.copyProperties(request, entity);
        return entity;
    }

    public static ModuleResponse toResponse(ModuleEntity entity) {
        if (entity == null) {
            return null;
        }
        ModuleResponse response = new ModuleResponse();
        BeanUtils.copyProperties(entity, response);
        return response;
    }

    // Không copy id, deleted, createDate, createdBy khi update
    public static void updateEntityFromRequest(ModuleRequest request, ModuleEntity entity) {
        if (request == null || entity == null) {
            return;
        }
        BeanUtils.copyProperties(request, entity, "id", "deleted", "createDate", "createdBy");
    }
}