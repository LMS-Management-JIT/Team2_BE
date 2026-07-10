package com.lms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ModuleRequest {

    @NotBlank(message = "Mã module không được để trống")
    private String moduleCode;

    @NotBlank(message = "Tên module không được để trống")
    private String moduleName;

    private String description;

    private Boolean deleted;

    private String requestedBy;
}