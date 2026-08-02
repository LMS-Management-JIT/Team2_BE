package com.lms.backend.service.impl;

import com.lms.backend.dto.request.CourseRequest;
import com.lms.backend.dto.response.CourseResponse;
import com.lms.backend.dto.response.PagedResponse;
import com.lms.backend.entity.CourseEntity;
import com.lms.backend.mapper.CourseMapper;
import com.lms.backend.repository.CourseRepository;
import com.lms.backend.service.ICourseService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements ICourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else if (authentication != null && authentication.getPrincipal() instanceof String principalName && !"anonymousUser".equals(principalName)) {
            return principalName;
        }
        return "system";
    }

    @Override
    @SneakyThrows
    public List<Map<String, Object>> findAllCourses() {
        return courseRepository.findAllCourses();
    }

    @Override
    @SneakyThrows
    public PagedResponse<CourseResponse> findAllCoursesPage(int page, int size, String name) {
        PagedResponse<CourseEntity> entityPage = courseRepository.findAllActive(page - 1, size, name);
        List<CourseResponse> responseList = entityPage.getContent().stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(responseList, entityPage.getTotalElements(),
                entityPage.getPage() + 1, entityPage.getSize(), entityPage.getTotalPages());
    }

    @Override
    @SneakyThrows
    public Map<String, Object> getCourseById(String courseId) {
        return courseRepository.getCourseById(courseId);
    }

    @Override
    @SneakyThrows
    public String createCourse(CourseRequest request) {
        String currentUser = request.getRequestedBy() != null ? request.getRequestedBy() : getCurrentUserEmail();

        CourseEntity entity = courseMapper.toEntity(request);
        entity.setCreateAt(com.google.cloud.Timestamp.now());
        entity.setUpdatedAt(com.google.cloud.Timestamp.now());
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);

        return courseRepository.save(entity);
    }

    @Override
    @SneakyThrows
    public CourseResponse updateCourse(String courseId, CourseRequest request) {
        CourseEntity existingEntity = courseRepository.findById(courseId);
        if (existingEntity == null || existingEntity.isDeleted()) {
            throw new IllegalArgumentException("Khong tim thay khoa hoc de cap nhat!");
        }

        String currentUser = request.getRequestedBy() != null ? request.getRequestedBy() : getCurrentUserEmail();

        courseMapper.updateEntityFromRequest(request, existingEntity);
        existingEntity.setUpdatedAt(com.google.cloud.Timestamp.now());
        existingEntity.setUpdatedBy(currentUser);

        courseRepository.update(courseId, existingEntity);
        return courseMapper.toResponse(existingEntity);
    }

    @Override
    @SneakyThrows
    public String deleteCourse(String courseId, String deletedBy) {
        CourseEntity existingEntity = courseRepository.findById(courseId);
        if (existingEntity == null) {
            throw new IllegalArgumentException("Khong tim thay khoa hoc de xoa!");
        }

        String user = deletedBy != null ? deletedBy : getCurrentUserEmail();
        return courseRepository.softDelete(courseId, user);
    }
}
