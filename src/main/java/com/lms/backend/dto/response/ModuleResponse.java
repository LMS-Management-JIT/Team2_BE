package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleResponse {
    private String id;
    private String moduleName;
    private String moduleCode;
    private String description;
    private boolean deleted;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private String createdBy;
    private String updateBy;
}