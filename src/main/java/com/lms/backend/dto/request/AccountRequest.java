package com.lms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountRequest {

    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @NotBlank(message = "Role ID không được để trống")
    private String roleId;

    private String status;
    private Boolean deleted;
    private String createdBy;
    private String updateBy;
    private String requestedBy;
}
