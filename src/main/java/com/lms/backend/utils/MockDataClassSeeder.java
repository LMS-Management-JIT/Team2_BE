package com.lms.backend.utils;

import com.lms.backend.dto.request.ClassRequest;
import com.lms.backend.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@Profile("dev") // Chỉ chạy khi kích hoạt profile 'dev' trong ứng dụng cá nhân
public class MockDataClassSeeder implements CommandLineRunner {

    @Autowired
    private ClassService classService;

    @Override
    public void run(String... args) throws Exception {
        if (classService.getAllClasses().isEmpty()) {
            System.out.println(" Database trống. Đang đẩy dữ liệu mẫu lên Firestore...");

            List<ClassRequest> mockData = Arrays.asList(
                    createMock("Lớp Backend Java 01", "Spring Boot & Firebase", "Backend", "course_java_01"),
                    createMock("Lớp Frontend React 02", "ReactJS & Tailwind", "Frontend", "course_react_02"),
                    createMock("Lớp Tiếng Nhật N3", "Giao tiếp IT", "Ngoại ngữ", "course_n3_01")
            );

            for (ClassRequest req : mockData) {
                String id = classService.createClass(req);
                System.out.println(" Đã tạo lớp mẫu thành công: " + id);
            }

            System.out.println(" HOÀN TẤT ĐẨY DỮ LIỆU MẪU!");
        } else {
            System.out.println(" Firestore đã có sẵn dữ liệu. Bỏ qua bước tạo Mock Data.");
        }
    }

    private ClassRequest createMock(String name, String desc, String prog, String courseId) {
        ClassRequest req = new ClassRequest();
        req.setClassName(name);
        req.setDescription(desc);
        req.setPrograme(prog);
        req.setStatus("ACTIVE");
        req.setEndDateClass(LocalDateTime.now().plusMonths(6));
        req.setCourseId(courseId);
        req.setRequestedBy("System Seeder");
        return req;
    }
}