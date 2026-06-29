package com.lms.backend.service.impl;

import com.google.cloud.firestore.Firestore;
import com.lms.backend.service.IClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements IClassService {

    private final Firestore firestore;

    @Override
    public long countClasses() throws Exception {

        return firestore
                .collection("classes")
                .get()
                .get()
                .size();
    }
}