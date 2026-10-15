package edufit_com_lms.module.attendance.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.attendance.dto.request.AttendanceSubmitRequest;
import edufit_com_lms.module.attendance.dto.response.AttendanceRecordResponse;
import edufit_com_lms.module.attendance.entity.AttendanceRecord;
import edufit_com_lms.module.attendance.repository.AttendanceRepository;
import edufit_com_lms.module.attendance.service.AttendanceService;
import edufit_com_lms.security.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    public ResponseEntity<ApiResponse<Void>> submitAttendance(@Valid @RequestBody AttendanceSubmitRequest request) {
        attendanceService.submitAttendance(request);
        return new ResponseEntity<>(new ApiResponse<>(
                true, "Diem danh thanh cong", null, null, HttpStatus.OK), HttpStatus.OK);
    }


    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    @GetMapping("/class/{classId}/history")
    public ResponseEntity<ApiResponse<Page<AttendanceRecordResponse>>> getAttendanceHistory(
            @PathVariable UUID classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<AttendanceRecord> records;
        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            records = attendanceRepository.findBySchoolClassIdAndCheckInTimeBetween(classId, start, end, pageable);
        } else {
            records = attendanceRepository.findBySchoolClassId(classId, pageable);
        }
        
        Page<AttendanceRecordResponse> response = records.map(record -> AttendanceRecordResponse.builder()
                .id(record.getId())
                .studentId(record.getStudent() != null ? record.getStudent().getUserId() : null)
                .studentName(record.getStudent() != null ? record.getStudent().getFullName() : null)
                .classId(record.getSchoolClass() != null ? record.getSchoolClass().getId() : null)
                .className(record.getSchoolClass() != null ? record.getSchoolClass().getClassName() : null)
                .checkInTime(record.getCheckInTime())
                .status(record.getStatus())
                .build());
                
        return new ResponseEntity<>(new ApiResponse<>(
                true, "Lay lich su diem danh thanh cong", null, response, HttpStatus.OK), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<Page<AttendanceRecordResponse>>> getMyAttendanceHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        Long studentId = userDetails.getId();
        
        Page<AttendanceRecord> records;
        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            records = attendanceRepository.findByStudentUserIdAndCheckInTimeBetween(studentId, start, end, pageable);
        } else {
            records = attendanceRepository.findByStudentUserId(studentId, pageable);
        }
        
        Page<AttendanceRecordResponse> response = records.map(record -> AttendanceRecordResponse.builder()
                .id(record.getId())
                .studentId(record.getStudent() != null ? record.getStudent().getUserId() : null)
                .studentName(record.getStudent() != null ? record.getStudent().getFullName() : null)
                .classId(record.getSchoolClass() != null ? record.getSchoolClass().getId() : null)
                .className(record.getSchoolClass() != null ? record.getSchoolClass().getClassName() : null)
                .checkInTime(record.getCheckInTime())
                .status(record.getStatus())
                .build());
                
        return new ResponseEntity<>(new ApiResponse<>(
                true, "Lay lich su diem danh cua ban thanh cong", null, response, HttpStatus.OK), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('LECTURER')")
    @PostMapping("/class/{classId}/verify-faces")
    public ResponseEntity<ApiResponse<List<Long>>> verifyFaces(
            @PathVariable UUID classId,
            @RequestParam("file") MultipartFile file) {
        List<Long> presentIds = attendanceService.verifyFaces(classId, file);
        return ResponseEntity.ok(new ApiResponse<>(true, "Verified faces", null, presentIds, HttpStatus.OK));
    }
}
