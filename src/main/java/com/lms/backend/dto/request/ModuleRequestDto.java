package com.lms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ModuleRequestDto(
        @NotBlank(message = "Tên module  không được để trống")
        String moduleName,

        @NotBlank(message = "Mã module  không được để trống")
        String moduleCode,

        String description
) {}