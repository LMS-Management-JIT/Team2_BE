package com.lms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ModuleRequestDto(
        @NotBlank(message = "Tên học phần không được để trống")
        @Size(max = 50, message = "Tên học phần không quá 50 ký tự")
        String moduleName,

        @NotBlank(message = "Mã học phần không được để trống")
        @Size(max = 255, message = "Mã học phần không quá 255 ký tự")
        String moduleCode,

        String description
) {}