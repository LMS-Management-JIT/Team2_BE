package com.lms.backend.dto.response;

public record ModuleResponseDto(
        String id,
        String moduleName,
        String moduleCode,
        String description,
        String createDate,
        String updateDate,
        String createdBy,
        String updateBy
) {}