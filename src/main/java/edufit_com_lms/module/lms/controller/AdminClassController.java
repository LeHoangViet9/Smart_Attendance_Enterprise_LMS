package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.lms.dto.response.SchoolClassResponse;
import edufit_com_lms.module.lms.service.AdminClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/classes")
@RequiredArgsConstructor
public class AdminClassController {
    private final AdminClassService adminClassService;

    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolClassResponse>>> getAllClasses() {
        return new ResponseEntity<>(new ApiResponse<>(
                true, "Lấy danh sách lớp thành công", null, adminClassService.getAllClasses(), HttpStatus.OK),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    @PutMapping("/{classId}/lecturer/{lecturerId}")
    public ResponseEntity<ApiResponse<Void>> assignHomeroomLecturer(
            @PathVariable UUID classId,
            @PathVariable Long lecturerId) {
        adminClassService.assignHomeroomLecturer(classId, lecturerId);
        return new ResponseEntity<>(new ApiResponse<>(
                true, "Gán giáo viên chủ nhiệm thành công", null, null, HttpStatus.OK), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    @PostMapping("/auto-assign")
    public ResponseEntity<ApiResponse<Void>> autoAssignStudents() {
        adminClassService.autoAssignStudents();
        return new ResponseEntity<>(new ApiResponse<>(
                true, "Đã tự động phân lớp cho các học sinh chưa có lớp (Tối đa 30/lớp) thành công", null, null,
                HttpStatus.OK), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    @PostMapping("/auto-assign-lecturers")
    public ResponseEntity<ApiResponse<Void>> autoAssignLecturers() {
        adminClassService.autoAssignLecturers();
        return new ResponseEntity<>(new ApiResponse<>(
                true, "Đã tự động gán các giảng viên chủ nhiệm vào các lớp chưa có giảng viên thành công", null, null,
                HttpStatus.OK), HttpStatus.OK);
    }
}
