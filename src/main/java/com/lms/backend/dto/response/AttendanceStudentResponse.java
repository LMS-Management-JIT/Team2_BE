package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceStudentResponse {
    private String studentId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String status; // mặc định "Chưa điểm danh" nếu chưa có record
    private String note;
}