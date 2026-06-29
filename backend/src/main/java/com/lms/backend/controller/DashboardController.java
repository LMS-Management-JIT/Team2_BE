package com.lms.backend.controller;

import com.lms.backend.dto.response.*;
import com.lms.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IClassService classService;
    private final IModuleService moduleService;
    private final ITeacherService teacherService;
    private final IStudentService studentService;
    private final IHistoryService historyService;

    @GetMapping("/total-classes")
    public ClassesResponse getTotalClasses() throws Exception {

        return new ClassesResponse(
                classService.countClasses()
        );
    }

    @GetMapping("/total-modules")
    public ModulesResponse getTotalModules() throws Exception {

        return new ModulesResponse(
                moduleService.countModules()
        );
    }

    @GetMapping("/total-teachers")
    public TeachersResponse getTotalTeachers() throws Exception {

        return new TeachersResponse(
                teacherService.countTeachers()
        );
    }

    @GetMapping("/total-students")
    public StudentsResponse getTotalStudents()
            throws Exception {

        return new StudentsResponse(
                studentService.countStudents()
        );
    }

    @GetMapping("/history")
    public List<HistoryResponse> getHistory() throws Exception {

        return historyService.getRecentHistory();
    }
}