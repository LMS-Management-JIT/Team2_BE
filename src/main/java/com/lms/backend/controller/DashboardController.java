package com.lms.backend.controller;

import com.lms.backend.dto.response.DashboardResponse;
import com.lms.backend.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping
    public DashboardResponse getDashboard() throws Exception {
        return dashboardService.getDashboardData();
    }
}