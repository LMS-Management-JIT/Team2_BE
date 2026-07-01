package com.lms.backend.service.impl;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.lms.backend.service.IModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements IModuleService {

    private final Firestore firestore;

    @Override
    public long countModules() throws Exception {

        QuerySnapshot snapshot = firestore
                .collection("modules")
                .whereEqualTo("deleted", false)
                .get()
                .get();

        return snapshot.size();
    }
}