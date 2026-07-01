package com.lms.backend.service.impl;

import com.lms.backend.dto.response.DashboardResponse;
import com.lms.backend.service.IDashboardService;
import com.lms.backend.service.IModuleService;
import com.lms.backend.service.IClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final IModuleService moduleService;
    private final IClassService classService;

    @Override
    public DashboardResponse getDashboardData() throws Exception {
        return DashboardResponse.builder()
                .totalModules(moduleService.countModules())
                .totalClasses(classService.countClasses())
                .build();
    }
}