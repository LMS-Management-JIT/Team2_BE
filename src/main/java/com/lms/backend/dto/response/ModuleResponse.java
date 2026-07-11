package com.lms.backend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ModuleResponse {
    private String id;
    private String moduleCode;
    private String moduleName;
    private String description;
    private boolean deleted;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private String createdBy;
    private String updateBy;
}