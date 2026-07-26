package com.lms.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassSessionEntity {
    private String id;
    private String classId;
    private String title;
    private String sessionDate;
    private String startTime;
    private String endTime;
    private String status;
    private Boolean deleted;
}