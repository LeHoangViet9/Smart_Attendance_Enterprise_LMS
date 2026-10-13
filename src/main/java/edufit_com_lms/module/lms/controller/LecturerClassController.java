package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.attendance.dto.response.EnrolledStudentResponse;
import edufit_com_lms.module.attendance.dto.response.StudentFaceDTO;
import edufit_com_lms.module.lms.dto.response.SchoolClassResponse;
import edufit_com_lms.module.lms.service.LecturerClassService;
import edufit_com_lms.security.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lecturer/classes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LECTURER')")
public class LecturerClassController {

    private final LecturerClassService lecturerClassService;

    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolClassResponse>>> getMyClasses() {
        List<SchoolClassResponse> responses = lecturerClassService.getMyClasses(currentUserId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched your classes", null, responses, HttpStatus.OK));
    }

    @Transactional(readOnly = true)
    @GetMapping("/{classId}")
    public ResponseEntity<ApiResponse<SchoolClassResponse>> getClassDetails(@PathVariable UUID classId) {
        SchoolClassResponse response = lecturerClassService.getClassDetails(currentUserId(), classId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched class details", null, response, HttpStatus.OK));
    }

    @Transactional(readOnly = true)
    @GetMapping("/{classId}/students")
    public ResponseEntity<ApiResponse<List<EnrolledStudentResponse>>> getEnrolledStudents(@PathVariable UUID classId) {
        List<EnrolledStudentResponse> responses = lecturerClassService.getEnrolledStudents(currentUserId(), classId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched enrolled students", null, responses, HttpStatus.OK));
    }

    @Transactional(readOnly = true)
    @GetMapping("/{classId}/students/faces")
    public ResponseEntity<ApiResponse<List<StudentFaceDTO>>> getEnrolledStudentsFaces(@PathVariable UUID classId) {
        List<StudentFaceDTO> responses = lecturerClassService.getEnrolledStudentFaces(currentUserId(), classId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched student face descriptors", null, responses, HttpStatus.OK));
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        return userDetails.getId();
    }
}
