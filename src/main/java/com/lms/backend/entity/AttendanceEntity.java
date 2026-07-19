package com.lms.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceEntity {
    private String studentId;
    private String status; // "Có mặt" | "Vắng" | "Vắng phép" | "Đi trễ"
    private String note;
    private String updatedBy;
    private String updateDate; // ISO string, ghi đơn giản
}