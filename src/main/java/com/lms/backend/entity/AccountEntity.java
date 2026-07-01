package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {

    @DocumentId
    private String id;

    private String email;
    private String password;

    /** Reference to Role.id */
    @PropertyName("role_id")
    private String roleId;

    private String status;
    private Boolean deleted;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("update_by")
    private String updateBy;

    @Exclude
    private LocalDateTime createDate;

    @Exclude
    private LocalDateTime updateDate;



}