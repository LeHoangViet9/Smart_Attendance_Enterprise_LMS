package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.attendance.dto.response.EnrolledStudentResponse;
import edufit_com_lms.module.auth.repository.StudentProfileRepository;
import edufit_com_lms.module.lms.dto.response.SchoolClassResponse;
import edufit_com_lms.module.lms.service.impl.AdminClassServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/classes")
@RequiredArgsConstructor
public class AdminClassController {
    private final AdminClassServiceImpl adminClassService;
    private final StudentProfileRepository studentProfileRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SchoolClassResponse>>> getAllClasses(
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return new ResponseEntity<>(new ApiResponse<>(
                true, "Láº¥y danh sÃ¡ch lá»›p thÃ nh cÃ´ng", null, adminClassService.getAllClasses(pageable), HttpStatus.OK),
                HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{classId}/lecturer/{lecturerId}")
    public ResponseEntity<ApiResponse<Void>> assignHomeroomLecturer(
            @PathVariable UUID classId,
            @PathVariable Long lecturerId) {
        adminClassService.assignHomeroomLecturer(classId, lecturerId);
        return new ResponseEntity<>(new ApiResponse<>(
                true, "GÃ¡n giÃ¡o viÃªn chá»§ nhiá»‡m thÃ nh cÃ´ng", null, null, HttpStatus.OK), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auto-assign")
    public ResponseEntity<ApiResponse<Void>> autoAssignStudents() {
        adminClassService.autoAssignStudents();
        return new ResponseEntity<>(new ApiResponse<>(
                true, "ÄÃ£ tá»± Ä‘á»™ng phÃ¢n lá»›p cho cÃ¡c há»c sinh chÆ°a cÃ³ lá»›p (Tá»‘i Ä‘a 30/lá»›p) thÃ nh cÃ´ng", null, null,
                HttpStatus.OK), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auto-assign-lecturers")
    public ResponseEntity<ApiResponse<Void>> autoAssignLecturers() {
        adminClassService.autoAssignLecturers();
        return new ResponseEntity<>(new ApiResponse<>(
                true, "ÄÃ£ tá»± Ä‘á»™ng gÃ¡n cÃ¡c giáº£ng viÃªn chá»§ nhiá»‡m vÃ o cÃ¡c lá»›p chÆ°a cÃ³ giáº£ng viÃªn thÃ nh cÃ´ng", null, null,
                HttpStatus.OK), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{classId}/students")
    public ResponseEntity<ApiResponse<List<EnrolledStudentResponse>>> getClassStudents(@PathVariable UUID classId) {
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
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched class students", null, responses, HttpStatus.OK));
    }
}
