package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileEntity {

    @DocumentId
    private String id;

    @PropertyName("account_id")
    private String accountId;

    @PropertyName("address")
    private String address;

    @PropertyName("create_date")
    private LocalDateTime createDate;

    @PropertyName("create_by")
    private String createdBy;

    @PropertyName("day_of_birth")
    private LocalDateTime dayOfBirth;

    private Boolean deleted;

    @PropertyName("full_name")
    private String fullName;

    private String gender;

    @PropertyName("phone_number")
    private String phoneNumber;

    @PropertyName("update_date")
    private LocalDateTime updateDate;

    @PropertyName("update_by")
    private String updatedBy;
}