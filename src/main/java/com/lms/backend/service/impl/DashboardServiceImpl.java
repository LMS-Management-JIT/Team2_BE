package com.lms.backend.service.impl;

import com.lms.backend.dto.response.DashboardResponse;
import com.lms.backend.dto.response.HistoryResponse;
import com.lms.backend.exception.DashboardException;
//import com.lms.backend.repository.HistoryRepository;
import com.lms.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final IModuleService moduleService;
    private final IClassService classService;
    private final IStudentService studentService;
    private final ITeacherService teacherService;
    private final IHistoryService historyService;

    @Override
    public DashboardResponse getDashboardData() {
        try {
            return DashboardResponse.builder()
                    .totalModules(moduleService.countModules())
                    .totalClasses(classService.countClasses())
                    .totalStudents(studentService.countStudents())
                    .totalTeachers(teacherService.countTeachers())
                    .recentHistories(historyService.getRecentHistories(10))
                    .build();
        } catch (Exception e) {
            throw new DashboardException("Lỗi khi lấy dữ liệu dashboard: " + e.getMessage());
        }
    }
}