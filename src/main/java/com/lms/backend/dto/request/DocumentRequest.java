package com.lms.backend.dto.request;

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
public class DocumentRequest {

    @NotBlank(message = "Module ID cannot be blank")
    private String moduleId;

    @NotBlank(message = "Document name cannot be blank")
    private String documentName;

    @NotNull(message = "Document type cannot be null")
    private String type;

    @NotBlank(message = "URL cannot be blank")
    private String url;

}