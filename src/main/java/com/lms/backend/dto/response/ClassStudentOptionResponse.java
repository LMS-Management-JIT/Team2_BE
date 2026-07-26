package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassStudentOptionResponse {
    private String studentId;
    private String fullName;
    private String phoneNumber;
    private String email;
}