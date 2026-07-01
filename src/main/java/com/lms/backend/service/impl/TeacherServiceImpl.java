package com.lms.backend.service.impl;

import com.lms.backend.repository.TeacherRepository;
import com.lms.backend.service.ITeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements ITeacherService {

    private final TeacherRepository teacherRepository;

    @Override
    public long countTeachers() throws Exception {
        return teacherRepository.countActive();
    }
}