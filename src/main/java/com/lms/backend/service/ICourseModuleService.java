package com.lms.backend.service;

import com.lms.backend.dto.request.CourseModuleRequest;
import com.lms.backend.dto.response.CourseModuleResponse;
import com.lms.backend.dto.response.ModuleResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ICourseModuleService {
    String create(CourseModuleRequest request);
    CourseModuleResponse getById(String id);
    List<CourseModuleResponse> getAll();
    String update(String id, CourseModuleRequest request);
    String delete(String id, String deletedBy);

    List<ModuleResponse> getModulesDetailByCourseId(String courseId);

    // ========================================================================
    // Thêm/xóa/sắp xếp module trong course (giống pattern ModuleTopic)
    // ========================================================================
    CourseModuleResponse addModuleToCourse(String courseId, String moduleId, String createdBy)
            throws ExecutionException, InterruptedException;

    void removeModuleFromCourse(String courseId, String moduleId, String updatedBy);

    void reorderModules(String courseId, List<String> moduleIds, String updatedBy)
            throws ExecutionException, InterruptedException;

    // ========================================================================
    // Lấy danh sách mapping entities theo courseId (giống getByModuleId)
    // ========================================================================
    List<CourseModuleResponse> getByCourseId(String courseId);
}