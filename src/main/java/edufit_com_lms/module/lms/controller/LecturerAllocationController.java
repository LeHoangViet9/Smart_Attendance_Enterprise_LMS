package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.auth.entity.Role;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.lms.entity.SchoolClass;
import edufit_com_lms.module.lms.repository.SchoolClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin: Auto-assign Lecturers to Classes (not Courses).
 * BRD 8.4: Admin có thể gán Lecturer thủ công hoặc sử dụng Auto-Assign.
 */
@RestController
@RequestMapping("/api/v1/admin/courses/auto-assign")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class LecturerAllocationController {

    private final SchoolClassRepository schoolClassRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> autoAssignLecturersToClasses() {
        // Tìm tất cả giảng viên đang active
        List<User> lecturers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.LECTURER && u.getLecturerProfile() != null)
                .collect(Collectors.toList());

        // Tìm các Class chưa có giảng viên phụ trách
        List<SchoolClass> unassignedClasses = schoolClassRepository.findAll().stream()
                .filter(c -> c.getLecturer() == null && c.getMajor() != null)
                .collect(Collectors.toList());

        int assignedCount = 0;

        for (User lecturer : lecturers) {
            String lecturerMajor = lecturer.getLecturerProfile().getMajor() != null ? lecturer.getLecturerProfile().getMajor().getCode() : "";
            String expectedMajorCode = "AI".equals(lecturerMajor) ? "AI" : "SE";

            // BRD 8.4: Không vượt quá workload cho phép (tối đa 3 lớp / giảng viên)
            long currentClassCount = schoolClassRepository.findByLecturer_UserId(lecturer.getUserId()).size();
            long availableSlots = 3 - currentClassCount;

            if (availableSlots <= 0) continue;

            List<SchoolClass> matchingClasses = unassignedClasses.stream()
                    .filter(c -> c.getMajor().getCode().equals(expectedMajorCode))
                    .limit(availableSlots)
                    .collect(Collectors.toList());

            for (SchoolClass c : matchingClasses) {
                c.setLecturer(lecturer);
                schoolClassRepository.save(c);
                unassignedClasses.remove(c);
                assignedCount++;
            }
        }

        return ResponseEntity.ok(new ApiResponse<>(true,
                "Auto-assignment computed", null,
                "Successfully assigned " + assignedCount + " classes to lecturers based on their major.",
                HttpStatus.OK));
    }
}

