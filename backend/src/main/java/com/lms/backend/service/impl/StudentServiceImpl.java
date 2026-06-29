package com.lms.backend.service.impl;

import com.google.cloud.firestore.Firestore;
import com.lms.backend.service.IStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl
        implements IStudentService {

    private final Firestore firestore;

    @Override
    public long countStudents() throws Exception {

        return firestore
                .collection("accounts")
                .whereEqualTo("roleId", "/roles/student")
                .get()
                .get()
                .size();
    }
}