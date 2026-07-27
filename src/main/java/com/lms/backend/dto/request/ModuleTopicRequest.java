package com.lms.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleTopicRequest {

    private String id;

    @NotBlank(message = "Module ID is required")
    private String moduleId;

    @NotBlank(message = "Topic ID is required")
    private String topicId;


    private Integer topicOrder;
}