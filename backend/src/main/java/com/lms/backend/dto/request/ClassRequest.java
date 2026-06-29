package com.lms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClassRequest {

    @NotBlank(message = "Tên lớp không được để trống")
    private String className;

    private String description;

    @NotBlank(message = "Chương trình học không được để trống")
    private String programe;

    private String status;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDateClass;

    @NotBlank(message = "ID Khóa học không được để trống")
    private String courseId;

    private String requestedBy;
}