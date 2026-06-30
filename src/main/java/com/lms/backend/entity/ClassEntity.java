package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.Data;
import java.util.Date;

@Data
public class ClassEntity {

    @DocumentId
    private String id; // Chuyển int thành String cho Firestore

    @PropertyName("class_name")
    private String className;

    @PropertyName("description")
    private String description;

    @PropertyName("programe")
    private String programe;

    @PropertyName("status")
    private String status;

    @PropertyName("end_date_class")
    private Date endDateClass;

    @PropertyName("create_date")
    private Date createDate;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("update_date")
    private Date updateDate;

    @PropertyName("update_by")
    private String updateBy;

    @PropertyName("deleted")
    private boolean deleted;

    @PropertyName("course_id")
    private String courseId;
}