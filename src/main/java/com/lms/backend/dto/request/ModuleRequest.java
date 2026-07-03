package com.lms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleRequest {

    @NotBlank(message = "Tên module không được để trống")
    @Size(max = 150, message = "Tên module tối đa 150 ký tự")
    private String moduleName;

    @NotBlank(message = "Mã module không được để trống")
    @Size(max = 50, message = "Mã module tối đa 50 ký tự")
    private String moduleCode;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;
}