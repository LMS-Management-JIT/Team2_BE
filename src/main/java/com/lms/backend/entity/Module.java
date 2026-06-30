package com.lms.backend.entity;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;


public class Module {
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

    @PropertyName("create_date")
    private LocalDateTime createDate;

    @PropertyName("update_date")
    private LocalDateTime updateDate;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("update_by")
    private String updateBy;
}
