package com.lms.backend.dto.response;

public record ModuleResponse(
        String id,
        String moduleName,
        String moduleCode,
        String description,
        String createDate,
        String updateDate,
        String createdBy,
        String updateBy
) {}