package com.lms.backend.service.impl;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.lms.backend.repository.ModuleRepository;
import com.lms.backend.service.IModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements IModuleService {

    private final ModuleRepository moduleRepository; // không inject Firestore

    @Override
    public long countModules() throws Exception {
        return moduleRepository.countActive();
    }
}