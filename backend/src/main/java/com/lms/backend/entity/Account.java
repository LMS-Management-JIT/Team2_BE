package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @DocumentId
    private String id;

    private String email;
    private String password;

    /** Reference to Role.id */
    private String roleId;

    private String status;
    private Boolean deleted;

    private Instant createDate;
    private String createdBy;
    private Instant updateDate;
    private String updateBy;
}