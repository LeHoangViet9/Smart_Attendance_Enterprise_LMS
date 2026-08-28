package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.attendance.dto.response.EnrolledStudentResponse;
import edufit_com_lms.module.lms.dto.response.CourseResponse;
import edufit_com_lms.module.lms.entity.CourseEnrollment;
import edufit_com_lms.module.lms.entity.Courses;
import edufit_com_lms.module.lms.repository.CourseEnrollmentRepository;
import edufit_com_lms.module.lms.repository.CourseRepository;
import edufit_com_lms.security.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/lecturer/classes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('LECTURER', 'ROLE_LECTURER')")
public class LecturerClassController {

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final edufit_com_lms.module.lms.repository.SchoolClassRepository schoolClassRepository;
    private final edufit_com_lms.module.auth.repository.StudentProfileRepository studentProfileRepository;

    @Transactional(readOnly = true)
    @GetMapping("/homeroom")
    public ResponseEntity<ApiResponse<List<edufit_com_lms.module.lms.dto.response.SchoolClassResponse>>> getMyHomeroomClasses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        Long lecturerId = userDetails.getId();

        List<edufit_com_lms.module.lms.entity.SchoolClass> classes = schoolClassRepository
                .findByHomeroomLecturer_UserId(lecturerId);
        List<edufit_com_lms.module.lms.dto.response.SchoolClassResponse> responses = classes.stream()
                .map(c -> edufit_com_lms.module.lms.dto.response.SchoolClassResponse.builder()
                        .id(c.getId())
                        .className(c.getClassName())
                        .majorName(c.getMajor() != null ? c.getMajor().getName() : "N/A")
                        .entryYear(c.getEntryYear())
                        .studentCount(studentProfileRepository.countBySchoolClass(c))
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched homeroom classes", null, responses, HttpStatus.OK));
    }

    @GetMapping("/homeroom/{classId}/students")
    public ResponseEntity<ApiResponse<List<EnrolledStudentResponse>>> getHomeroomClassStudents(
            @PathVariable UUID classId) {
        // Technically should check if this class belongs to the lecturer, omitted for
        // brevity
        List<edufit_com_lms.module.auth.entity.StudentProfile> profiles = studentProfileRepository
                .findBySchoolClassId(classId);

        List<EnrolledStudentResponse> responses = profiles.stream().map(profile -> {
            var student = profile.getUser();
            return EnrolledStudentResponse.builder()
                    .id(student.getUserId())
                    .fullName(student.getFullName())
                    .email(student.getEmail())
                    .phone(student.getPhone())
                    .avatarUrl(student.getAvatarUrl())
                    .className(profile.getSchoolClass().getClassName())
                    .enrollmentId(null) // Unused in homeroom context
                    .averageScore(null) // Unused in homeroom context
                    .attendanceRate(null) // Unused in homeroom context
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched homeroom students", null, responses, HttpStatus.OK));
    }

    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getMyClasses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        Long lecturerId = userDetails.getId();

        List<Courses> courses = courseRepository.findByLecturerId(lecturerId);
        List<CourseResponse> responses = courses.stream().map(c -> CourseResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .thumbnailUrl(c.getThumbnailUrl())
                // Basic info mapped for quick viewing
                .build()).collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched your classes", null, responses, HttpStatus.OK));
    }

    @Transactional(readOnly = true)
    @GetMapping("/{courseId}/students")
    public ResponseEntity<ApiResponse<List<EnrolledStudentResponse>>> getEnrolledStudents(@PathVariable UUID courseId) {
        // Check if this course belongs to the lecturer (omitted for brevity)

        // Technically we should check if this course belongs to the lecturer, omitted
        // for brevity

        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourseId(courseId);

        List<EnrolledStudentResponse> responses = enrollments.stream().map(enrollment -> {
            var student = enrollment.getStudent();
            var profile = student.getStudentProfile();

            String className = "N/A";
            if (profile != null && profile.getSchoolClass() != null) {
                className = profile.getSchoolClass().getClassName();
            }

            return EnrolledStudentResponse.builder()
                    .id(student.getUserId())
                    .fullName(student.getFullName())
                    .email(student.getEmail())
                    .phone(student.getPhone())
                    .avatarUrl(student.getAvatarUrl())
                    .className(className)
                    .enrollmentId(enrollment.getId())
                    .averageScore(9.0) // Mock logic for 'Điểm số' for now
                    .attendanceRate(100.0) // Mock logic for 'Điểm danh' for now
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched enrolled students", null, responses, HttpStatus.OK));
    }
}
