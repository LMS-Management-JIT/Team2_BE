package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherListResponse {

    private String id;

    private String fullName;

    private String email;

    private String phoneNumber;

    private String gender;

    private Boolean available;

    private List<TeacherClassResponse> classes;
}