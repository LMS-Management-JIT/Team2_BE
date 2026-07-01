package com.lms.backend.service.impl;

import com.lms.backend.dto.response.DashboardResponse;
import com.lms.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final IModuleService moduleService;
    private final IClassService classService;
    private final IStudentService studentService;
    private final ITeacherService teacherService;

    @Override
    public DashboardResponse getDashboardData() throws Exception {
        return DashboardResponse.builder()
                .totalModules(moduleService.countModules())
                .totalClasses(classService.countClasses())
                .totalStudents(studentService.countStudents())
                .totalTeachers(teacherService.countTeachers())
                .build();
    }
}