package com.lms.backend.dto.response;
import com.lms.backend.dto.response.StudentClassResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentListResponse {

    private String id;

    private String fullName;

    private String email;

    private String phoneNumber;

    private String gender;

    private String status;

    private List<StudentClassResponse> classes;
}