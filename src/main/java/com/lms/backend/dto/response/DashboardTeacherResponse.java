package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTeacherResponse {
    private String id;
    private String profileId;
    private String fullName;
    private String phoneNumber;
    private String gender;
    private String address;
    private String specialization;
    private String qualification;
    private String certifications;
    private Boolean available;
}
