package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.*;

import java.time.LocalDateTime;

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