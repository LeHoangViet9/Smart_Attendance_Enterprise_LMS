package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.lms.dto.response.SchoolClassResponse;
import edufit_com_lms.module.lms.entity.ClassEnrollment;
import edufit_com_lms.module.lms.repository.ClassEnrollmentRepository;
import edufit_com_lms.security.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/student/classes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentClassController {

    private final ClassEnrollmentRepository classEnrollmentRepository;

    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolClassResponse>>> getMyClasses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        Long studentId = userDetails.getId();

        List<ClassEnrollment> enrollments = classEnrollmentRepository.findByStudentUserId(studentId);

        List<SchoolClassResponse> responses = enrollments.stream()
                .filter(e -> e.getSchoolClass() != null)
                .map(e -> {
                    var cls = e.getSchoolClass();
                    return SchoolClassResponse.builder()
                            .id(cls.getId())
                            .className(cls.getClassName())
                            .majorName(cls.getMajor() != null ? cls.getMajor().getName() : "N/A")
                            .entryYear(cls.getEntryYear())
                            .lecturerId(cls.getLecturer() != null ? cls.getLecturer().getUserId() : null)
                            .lecturerName(cls.getLecturer() != null ? cls.getLecturer().getFullName() : null)
                            .courseId(cls.getCourse() != null ? cls.getCourse().getId() : null)
                            .courseName(cls.getCourse() != null ? cls.getCourse().getTitle() : null)
                            .studentCount(0) // Could fetch count but might not be necessary for student view
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched your enrolled classes", null, responses, HttpStatus.OK));
    }
}
