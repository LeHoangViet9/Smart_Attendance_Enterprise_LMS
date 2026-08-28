package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.auth.entity.Role;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.lms.entity.Courses;
import edufit_com_lms.module.lms.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/courses/auto-assign")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
public class LecturerAssignmentController {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> autoAssignLecturersToCourses() {
        // Find all lecturers
        List<User> lecturers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.LECTURER && u.getLecturerProfile() != null)
                .collect(Collectors.toList());

        // Find all courses without lecturers
        List<Courses> unassignedCourses = courseRepository.findAll().stream()
                .filter(c -> c.getLecturerId() == null && c.getMajor() != null)
                .collect(Collectors.toList());

        int assignedCount = 0;

        for (User lecturer : lecturers) {
            String lecturerMajor = lecturer.getLecturerProfile().getMajor();
            // In a real system, we'd map "AI" to aiMajor, but lecturerProfile.major is a
            // String right now
            // We map "AI" to Major code "AI", "Software Engineering" to "SE" based on
            // DataSeeder logic
            String expectedMajorCode = "AI".equals(lecturerMajor) ? "AI" : "SE";

            // Count how many courses this lecturer currently has
            long currentCourseCount = courseRepository.findByLecturerId(lecturer.getUserId()).size();
            long availableSlots = 3 - currentCourseCount;

            if (availableSlots <= 0) {
                continue; // This lecturer is already at max capacity
            }

            // Find matching courses for this lecturer
            List<Courses> matchingCourses = unassignedCourses.stream()
                    .filter(c -> c.getMajor().getCode().equals(expectedMajorCode))
                    .limit(availableSlots)
                    .collect(Collectors.toList());

            for (Courses c : matchingCourses) {
                c.setLecturerId(lecturer.getUserId());
                courseRepository.save(c);
                unassignedCourses.remove(c); // Remove from pool
                assignedCount++;
            }
        }

        return ResponseEntity.ok(new ApiResponse<>(true,
                "Auto-assignment computed", null,
                "Successfully assigned " + assignedCount + " classes to lecturers based on their major.",
                HttpStatus.OK));
    }
}
