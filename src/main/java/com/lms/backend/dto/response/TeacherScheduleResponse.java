package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherScheduleResponse {
    private String id;
    private String classId;
    private String className;
    private String title;
    private String sessionDate;
    private String startTime;
    private String endTime;
    private String location;
}