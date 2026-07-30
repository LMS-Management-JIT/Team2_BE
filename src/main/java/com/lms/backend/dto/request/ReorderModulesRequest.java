package com.lms.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderModulesRequest {
    private String courseId;
    private List<String> moduleIds;
    private String updatedBy;
}
