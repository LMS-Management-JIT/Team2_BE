package com.lms.backend.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClassDetailResponse extends ClassResponse {
    private Map<String, Object> course;
    private List<Map<String, Object>> students;
    private List<Map<String, Object>> teachers;
}