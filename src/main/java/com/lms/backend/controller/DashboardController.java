package com.lms.backend.controller;

import com.lms.backend.dto.response.*;
import com.lms.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final IModuleService moduleService;

    @GetMapping("/total-modules")
    public DashboardResponse getTotalModules() throws Exception {
        return new DashboardResponse(moduleService.countModules());
    }
}