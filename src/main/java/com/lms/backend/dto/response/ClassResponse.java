package com.lms.backend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class ClassResponse {
    private String id;
    private String className;
    private String description;
    private String programe;
    private String status;

    private List<Integer> scheduleDays;
    private String startTime;
    private String endTime;

    private LocalDateTime startDateClass;
    private LocalDateTime endDateClass;
    private LocalDateTime createDate;
    private String createdBy;
    private LocalDateTime updateDate;
    private String updateBy;

    private boolean deleted;
    private String courseId;
    private String note;
}