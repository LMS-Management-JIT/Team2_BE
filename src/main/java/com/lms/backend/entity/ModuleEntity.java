package com.lms.backend.entity;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleEntity {

    @DocumentId
    private String id;

    @PropertyName("module_name")
    private String moduleName;

    @PropertyName("module_code")
    private String moduleCode;

    @PropertyName("description")
    private String description;

    @PropertyName("deleted")
    private boolean deleted;

    @Exclude
    private LocalDateTime createDate;

    @Exclude
    private LocalDateTime updateDate;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("update_by")
    private String updateBy;
}