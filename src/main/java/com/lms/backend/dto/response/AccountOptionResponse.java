package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountOptionResponse {
    private String accountId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String roleName;
}