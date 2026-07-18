package com.lms.backend.dto.response;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeacherResponse {

    private String id;

    private String fullName;

    private String email;

    private String phoneNumber;

    private LocalDateTime dayOfBirth;

    private String specialization;

    private String qualification;

    private String certifications;

    private Boolean available;

    private Boolean deleted;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;

    private String createdBy;

    private String updatedBy;

    private String gender;

    private String address;

    private List<TeacherClassResponse> classes;

}