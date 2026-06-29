package com.lms.backend.dto.response;

import lombok.Data;

import java.time.Instant;

@Data
public class AccountResponse {

    private String id;
    private String email;
    private String roleId;
    private String status;
    private Boolean deleted;
    private Instant createDate;
    private String createdBy;
    private Instant updateDate;
    private String updateBy;
}

