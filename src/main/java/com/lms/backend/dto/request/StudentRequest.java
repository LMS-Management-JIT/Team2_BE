package com.lms.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentRequest {
    // ===== Account =====
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    // ===== Profile =====
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^(0[3|5|7|8|9])[0-9]{8}$",
            message = "Số điện thoại không hợp lệ"
    )
    private String phoneNumber;

    private LocalDateTime dayOfBirth;

    private String gender;

    private String address;

    // ===== Student =====

    @Pattern(
            regexp = "^\\d{12}$",
            message = "CCCD phải gồm đúng 12 chữ số"
    )
    private String identityNumber;

    private LocalDateTime issueDate;

    private String issuePlace;

    private String educationLevel;

    private String interviewContent;

    private String admissionResult;

    private String description;

    private String requestedBy;
}