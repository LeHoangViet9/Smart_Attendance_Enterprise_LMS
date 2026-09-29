package edufit_com_lms.module.attendance.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.attendance.dto.request.AttendanceSubmitRequest;
import edufit_com_lms.module.attendance.dto.response.AttendanceRecordResponse;
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
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import edufit_com_lms.security.CustomUserDetail;

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
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getAttendanceHistory(
            @PathVariable UUID classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<AttendanceRecord> records;
        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            records = attendanceRepository.findBySchoolClassIdAndCheckInTimeBetween(classId, start, end);
        } else {
            records = attendanceRepository.findBySchoolClassId(classId);
        }
        
        List<AttendanceRecordResponse> response = records.stream().map(record -> AttendanceRecordResponse.builder()
                .id(record.getId())
                .studentId(record.getStudent() != null ? record.getStudent().getUserId() : null)
                .studentName(record.getStudent() != null ? record.getStudent().getFullName() : null)
                .classId(record.getSchoolClass() != null ? record.getSchoolClass().getId() : null)
                .className(record.getSchoolClass() != null ? record.getSchoolClass().getClassName() : null)
                .checkInTime(record.getCheckInTime())
                .status(record.getStatus())
                .build()).collect(Collectors.toList());
                
        return new ResponseEntity<>(new ApiResponse<>(
                true, "Lấy lịch sử điểm danh thành công", null, response, HttpStatus.OK), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getMyAttendanceHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        Long studentId = userDetails.getId();
        
        List<AttendanceRecord> records;
        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            records = attendanceRepository.findByStudentUserIdAndCheckInTimeBetween(studentId, start, end);
        } else {
            records = attendanceRepository.findByStudentUserId(studentId);
        }
        
        List<AttendanceRecordResponse> response = records.stream().map(record -> AttendanceRecordResponse.builder()
                .id(record.getId())
                .studentId(record.getStudent() != null ? record.getStudent().getUserId() : null)
                .studentName(record.getStudent() != null ? record.getStudent().getFullName() : null)
                .classId(record.getSchoolClass() != null ? record.getSchoolClass().getId() : null)
                .className(record.getSchoolClass() != null ? record.getSchoolClass().getClassName() : null)
                .checkInTime(record.getCheckInTime())
                .status(record.getStatus())
                .build()).collect(Collectors.toList());
                
        return new ResponseEntity<>(new ApiResponse<>(
                true, "Lấy lịch sử điểm danh của bạn thành công", null, response, HttpStatus.OK), HttpStatus.OK);
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
