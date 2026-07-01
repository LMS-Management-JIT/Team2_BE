package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassEntity {

    @DocumentId
    private String id;

    @PropertyName("class_name")
    private String className;

    private String description;
    private String programe;
    private String status;
    private boolean deleted;

    @PropertyName("course_id")
    private String courseId;

    @PropertyName("created_by")
    private String createdBy;

    @PropertyName("update_by")
    private String updateBy;

    @Exclude private LocalDateTime startDateClass;
    @Exclude private LocalDateTime endDateClass;
    @Exclude private LocalDateTime createDate;
    @Exclude private LocalDateTime updateDate;

    @Exclude
    public LocalDateTime getStartDateClass() { return this.startDateClass; }
    @Exclude
    public void setStartDateClass(LocalDateTime startDateClass) { this.startDateClass = startDateClass; }

    @Exclude
    public LocalDateTime getEndDateClass() { return this.endDateClass; }
    @Exclude
    public void setEndDateClass(LocalDateTime endDateClass) { this.endDateClass = endDateClass; }

    @Exclude
    public LocalDateTime getCreateDate() { return this.createDate; }
    @Exclude
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }

    @Exclude
    public LocalDateTime getUpdateDate() { return this.updateDate; }
    @Exclude
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }

    @PropertyName("start_date_class")
    public Date getStartDateClassForFirestore() {
        return (startDateClass == null) ? null : Date.from(startDateClass.atZone(ZoneId.systemDefault()).toInstant());
    }
    @PropertyName("start_date_class")
    public void setStartDateClassFromFirestore(Date date) {
        this.startDateClass = (date == null) ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    @PropertyName("end_date_class")
    public Date getEndDateClassForFirestore() {
        return (endDateClass == null) ? null : Date.from(endDateClass.atZone(ZoneId.systemDefault()).toInstant());
    }
    @PropertyName("end_date_class")
    public void setEndDateClassFromFirestore(Date date) {
        this.endDateClass = (date == null) ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    @PropertyName("create_date")
    public Date getCreateDateForFirestore() {
        return (createDate == null) ? null : Date.from(createDate.atZone(ZoneId.systemDefault()).toInstant());
    }
    @PropertyName("create_date")
    public void setCreateDateFromFirestore(Date date) {
        this.createDate = (date == null) ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    @PropertyName("update_date")
    public Date getUpdateDateForFirestore() {
        return (updateDate == null) ? null : Date.from(updateDate.atZone(ZoneId.systemDefault()).toInstant());
    }
    @PropertyName("update_date")
    public void setUpdateDateFromFirestore(Date date) {
        this.updateDate = (date == null) ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}