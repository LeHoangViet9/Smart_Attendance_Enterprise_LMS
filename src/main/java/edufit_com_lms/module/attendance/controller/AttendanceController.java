package edufit_com_lms.module.attendance.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.attendance.dto.request.AttendanceSubmitRequest;
import edufit_com_lms.module.attendance.entity.AttendanceRecord;
import edufit_com_lms.module.attendance.repository.AttendanceRepository;
import edufit_com_lms.module.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.concurrent.CompletableFuture;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final AttendanceRepository attendanceRepository;

    @PreAuthorize("hasRole('LECTURER')")
    @PostMapping("/submit-attendance")
    public ResponseEntity<ApiResponse<Void>> submitAttendance(@RequestBody AttendanceSubmitRequest request) {
        attendanceService.submitAttendance(request);
        return new ResponseEntity<>(new ApiResponse<>(
                true, "Điểm danh thành công", null, null, HttpStatus.OK), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    @GetMapping("/class/{classId}/history")
    public ResponseEntity<ApiResponse<List<AttendanceRecord>>> getAttendanceHistory(@PathVariable UUID classId) {
        List<AttendanceRecord> records = attendanceRepository.findBySchoolClassId(classId);
        return new ResponseEntity<>(new ApiResponse<>(
                true, "Lấy lịch sử điểm danh thành công", null, records, HttpStatus.OK), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('LECTURER')")
    @PostMapping("/class/{classId}/verify-faces")
    public CompletableFuture<ResponseEntity<ApiResponse<List<Long>>>> verifyFaces(
            @PathVariable UUID classId,
            @RequestParam("file") MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Long> presentIds = attendanceService.verifyFaces(classId, file);
                return ResponseEntity.ok(new ApiResponse<>(true, "Verified faces", null, presentIds, HttpStatus.OK));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse<>(false, e.getMessage(), null, null, HttpStatus.INTERNAL_SERVER_ERROR));
            }
        });
    }
}
