package com.lms.backend.service;

import com.lms.backend.dto.request.CourseModuleRequest;
import com.lms.backend.dto.response.CourseModuleResponse;
import com.lms.backend.dto.response.ModuleResponse;
import lombok.SneakyThrows;

import java.util.List;

public interface ICourseModuleService {
    String create(CourseModuleRequest request);
    CourseModuleResponse getById(String id);
    List<CourseModuleResponse> getAll();
    String update(String id, CourseModuleRequest request);
    String delete(String id, String deletedBy);

    // =========================================================================
    // HÀM MỚI ĐƯỢC THÊM VÀO: LẤY CHI TIẾT MODULE TỪ COURSE_ID (CÓ SMART LOOKUP)
    // =========================================================================
    List<ModuleResponse> getModulesDetailByCourseId(String courseId);
}