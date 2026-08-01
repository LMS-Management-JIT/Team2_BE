package com.lms.backend.service.impl;

import com.lms.backend.dto.request.CourseModuleRequest;
import com.lms.backend.dto.response.CourseModuleResponse;
import com.lms.backend.dto.response.ModuleResponse;
import com.lms.backend.entity.CourseModuleEntity;
import com.lms.backend.entity.ModuleEntity;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.mapper.CourseModuleMapper;
import com.lms.backend.mapper.ModuleMapper;
import com.lms.backend.repository.CourseModuleRepository;
import com.lms.backend.repository.ModuleRepository;
import com.lms.backend.service.ICourseModuleService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

// ==========================================
// THÊM IMPORT CHO CHỨC NĂNG NỐI BẢNG MODULE
// ==========================================

@Service
@RequiredArgsConstructor
public class CourseModuleServiceImpl implements ICourseModuleService {

    private final CourseModuleRepository repository;
    private final CourseModuleMapper mapper;

    // ==========================================
    // KHAI BÁO THÊM MODULE REPOSITORY & MAPPER
    // ==========================================
    private final ModuleRepository moduleRepository;
    private final ModuleMapper moduleMapper;

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
    public String create(CourseModuleRequest request) {
        String currentUser = request.getCreatedBy() != null ? request.getCreatedBy() : getCurrentUserEmail();
        CourseModuleEntity entity = mapper.toEntity(request);

        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);

        return repository.save(entity);
    }

    @Override
    @SneakyThrows
    public CourseModuleResponse getById(String id) {
        CourseModuleEntity entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy CourseModule hoặc đã bị xóa!"));
        return mapper.toResponse(entity);
    }

    @Override
    @SneakyThrows
    public List<CourseModuleResponse> getAll() {
        return repository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getDeleted()))
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public String update(String id, CourseModuleRequest request) {
        CourseModuleEntity entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy CourseModule để cập nhật!"));

        String currentUser = request.getUpdatedBy() != null ? request.getUpdatedBy() : getCurrentUserEmail();

        mapper.updateEntityFromRequest(request, entity);
        entity.setUpdateDate(LocalDateTime.now());
        entity.setUpdatedBy(currentUser);

        return repository.update(id, entity);
    }

    @Override
    @SneakyThrows
    public String delete(String id, String deletedBy) {
        repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy CourseModule để xóa!"));

        String user = deletedBy != null ? deletedBy : getCurrentUserEmail();
        return repository.softDelete(id, user);
    }

    // =========================================================================
    // HÀM MỚI ĐƯỢC THÊM VÀO: LẤY CHI TIẾT MODULE TỪ COURSE_ID (CÓ SMART LOOKUP)
    // =========================================================================
    @SneakyThrows
    @Override
    public List<ModuleResponse> getModulesDetailByCourseId(String courseId) {
        // 1. Lấy danh sách các liên kết từ bảng course_modules
        List<CourseModuleEntity> mappings = repository.findByCourseId(courseId);

        List<ModuleResponse> responses = new ArrayList<>();

        // 2. Vòng lặp lấy chi tiết Module
        for (CourseModuleEntity mapping : mappings) {
            String targetId = mapping.getModuleId();

            if (targetId != null && !targetId.isBlank()) {

                // Gọi hàm Smart Lookup bên ModuleRepository
                ModuleEntity module = moduleRepository.findSmartByIdOrCode(targetId);

                if (module != null && !module.isDeleted()) {
                    responses.add(moduleMapper.toResponse(module));
                } else {
                    // In cảnh báo ra console nếu database có rác
                    System.err.println("Cảnh báo: Dữ liệu rác! Không thể tìm thấy Module nào (ID/Code) khớp với: " + targetId);
                }
            }
        }
        return responses;
    }

    // ========================================================================
    // Thêm module vào course (giống pattern addTopicToModule bên ModuleTopic)
    // ========================================================================
    @Override
    @SneakyThrows
    public CourseModuleResponse addModuleToCourse(String courseId, String moduleId, String createdBy)
            throws ExecutionException, InterruptedException {

        // Kiểm tra module tồn tại
        ModuleEntity module = moduleRepository.findSmartByIdOrCode(moduleId);
        if (module == null || module.isDeleted()) {
            throw new ResourceNotFoundException("Không tìm thấy module với ID: " + moduleId);
        }

        // Kiểm tra trùng lặp
        if (repository.findByCourseIdAndModuleId(courseId, moduleId).isPresent()) {
            throw new IllegalArgumentException("Module đã tồn tại trong khóa học này!");
        }

        // Auto-assign order
        long count = repository.countByCourseId(courseId);
        int nextOrder = (int) count + 1;

        String currentUser = createdBy != null ? createdBy : getCurrentUserEmail();

        CourseModuleEntity entity = CourseModuleEntity.builder()
                .courseId(courseId)
                .moduleId(moduleId)
                .moduleOrder(nextOrder)
                .deleted(false)
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        String savedId = repository.save(entity);
        CourseModuleEntity saved = repository.findById(savedId)
                .orElseThrow(() -> new NoSuchElementException("Lỗi khi lấy lại dữ liệu!"));
        return toResponseWithModuleName(saved);
    }

    // ========================================================================
    // Xóa module khỏi course (giống pattern removeTopicFromModule bên ModuleTopic)
    // ========================================================================
    @Override
    @SneakyThrows
    public void removeModuleFromCourse(String courseId, String moduleId, String updatedBy) {
        CourseModuleEntity mapping = repository.findByCourseIdAndModuleId(courseId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy module trong khóa học! courseId: " + courseId + ", moduleId: " + moduleId));

        String user = updatedBy != null ? updatedBy : getCurrentUserEmail();
        repository.softDelete(mapping.getId(), user);

        // Reorder remaining modules in the course
        List<CourseModuleEntity> remaining = repository.findByCourseId(courseId);
        for (int i = 0; i < remaining.size(); i++) {
            CourseModuleEntity rel = remaining.get(i);
            if (!rel.getId().equals(mapping.getId())) {
                repository.updateModuleOrder(rel.getId(), i + 1);
            }
        }
    }

    // ========================================================================
    // Lấy danh sách mapping entities theo courseId (giống getByModuleId bên ModuleTopic)
    // ========================================================================
    @Override
    @SneakyThrows
    public List<CourseModuleResponse> getByCourseId(String courseId) {
        List<CourseModuleResponse> responses = new ArrayList<>();
        for (CourseModuleEntity mapping : repository.findByCourseId(courseId)) {
            responses.add(toResponseWithModuleName(mapping));
        }
        return responses;
    }

    /**
     * Map CourseModuleEntity -> CourseModuleResponse, đồng thời lấy thêm tên
     * module (moduleName) từ bảng modules để trả về trong response.
     */
    @SneakyThrows
    private CourseModuleResponse toResponseWithModuleName(CourseModuleEntity mapping) {
        CourseModuleResponse response = mapper.toResponse(mapping);

        String moduleId = mapping.getModuleId();
        if (moduleId != null && !moduleId.isBlank()) {
            ModuleEntity module = moduleRepository.findSmartByIdOrCode(moduleId);
            if (module != null && !module.isDeleted()) {
                response.setModuleName(module.getModuleName());
            } else {
                System.err.println("Cảnh báo: Không tìm thấy Module khớp với moduleId: " + moduleId);
            }
        }
        return response;
    }

    // ========================================================================
    // Sắp xếp thứ tự module trong course
    // ========================================================================
    @Override
    @SneakyThrows
    public void reorderModules(String courseId, List<String> moduleIds, String updatedBy) {
        if (moduleIds == null || moduleIds.isEmpty()) return;

        for (int i = 0; i < moduleIds.size(); i++) {
            String moduleId = moduleIds.get(i);
            CourseModuleEntity mapping = repository.findByCourseIdAndModuleId(courseId, moduleId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy module trong khóa học: " + moduleId));

            repository.updateModuleOrder(mapping.getId(), i + 1);
        }
    }
}