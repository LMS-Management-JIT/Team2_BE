package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {
 
    @DocumentId
    private String id;

    private String name;
    private Boolean deleted;

    @PropertyName("create_date")
    private Instant createDate;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("update_date")
    private Instant updateDate;

    @PropertyName("update_by")
    private String updateBy;
}
 