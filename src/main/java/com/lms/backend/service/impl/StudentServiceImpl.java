package com.lms.backend.service.impl;

import com.lms.backend.repository.StudentRepository;
import com.lms.backend.service.IStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements IStudentService {

    private final StudentRepository studentRepository;

    @Override
    public long countStudents() throws Exception {
        return studentRepository.countActive();
    }
}