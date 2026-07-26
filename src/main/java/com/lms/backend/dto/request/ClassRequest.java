package com.lms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ClassRequest {

    @NotBlank(message = "Tên lớp không được để trống")
    private String className;

    private String description;

    private String note;

    @NotBlank(message = "Chương trình học không được để trống")
    private String programe;

    private String status;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDateClass;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDateClass;

    @NotBlank(message = "ID Khóa học không được để trống")
    private String courseId;
    private List<Integer> scheduleDays;
    private String startTime;
    private String endTime;
    private String requestedBy;
}