package com.lms.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccountResponse {

    private String id;
    private String email;
    private String roleId;
    private String status;
    private Boolean deleted;
    private String createdBy;
    private String updateBy;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
