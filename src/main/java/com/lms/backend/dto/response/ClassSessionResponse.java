package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassSessionResponse {
    private String id;
    private String title;
    private String sessionDate;
    private String startTime;
    private String endTime;
    private String status;
}