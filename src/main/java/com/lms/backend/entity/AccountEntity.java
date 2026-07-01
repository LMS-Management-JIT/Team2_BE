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
    @Exclude private LocalDateTime updateDate;

    @Exclude
    public LocalDateTime getCreateDate() { return this.createDate; }
    @Exclude
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }

    @Exclude
    public LocalDateTime getUpdateDate() { return this.updateDate; }
    @Exclude
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }


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