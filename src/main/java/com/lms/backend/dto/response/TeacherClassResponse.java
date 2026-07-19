package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherClassResponse {

    private String classId;
    private String className;
    private String program;
    private String startDate;
    private String endDate;
    private String status;
}