package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;
import java.util.Date;

@Data
public class ClassEntity {

    @DocumentId
    private String id; // Chuyển int thành String cho Firestore

    private String className;
    private String description;
    private String programe;
    private String status;

    private Date endDateClass;
    private Date createDate;
    private String createdBy;
    private Date updateDate;
    private String updateBy;

    private boolean deleted;

    private String courseId;
}