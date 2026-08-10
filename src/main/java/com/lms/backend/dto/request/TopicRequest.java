package com.lms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TopicRequest {
    @NotBlank(message = "Tên chủ đề không được để trống")
    private String topicName;
    private String createdBy;
}