package com.lms.backend.dto.response;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeacherResponse {

    private String id;

    private String profileId;

    private String specialization;

    private String qualification;

    private String certifications;

    private Boolean available;

    private Boolean deleted;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;

    private String createdBy;

    private String updatedBy;

    private List<String> classNames;

}