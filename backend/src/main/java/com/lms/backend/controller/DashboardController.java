package com.lms.backend.controller;

import com.lms.backend.dto.response.ClassesResponse;
import com.lms.backend.service.IClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IClassService classService;

    @GetMapping("/total-classes")
    public ClassesResponse getTotalClasses() throws Exception {

        return new ClassesResponse(
                classService.countClasses()
        );
    }
}