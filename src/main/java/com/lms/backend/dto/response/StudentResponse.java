package com.lms.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StudentResponse {

    private String id;

    private String email;

    private String fullName;

    private String phoneNumber;

    private LocalDateTime dayOfBirth;

    private String gender;

    private String address;

    private String identityNumber;

    private LocalDateTime issueDate;

    private String issuePlace;

    private String educationLevel;

    private String admissionResult;

    private String interviewContent;

    private String description;

    private String status;

    private List<StudentClassResponse> classes;
}