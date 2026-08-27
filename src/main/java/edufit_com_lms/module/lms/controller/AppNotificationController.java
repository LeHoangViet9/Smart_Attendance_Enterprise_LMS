package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.lms.dto.request.ReportRequest;
import edufit_com_lms.module.lms.entity.AppNotification;
import edufit_com_lms.module.lms.service.AppNotificationService;
import edufit_com_lms.security.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class AppNotificationController {

    private final AppNotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppNotification>>> getAllNotifications() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getAllNotifications()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đọc", null));
    }

    @PostMapping("/report")
    public ResponseEntity<ApiResponse<Void>> submitReport(@Valid @RequestBody ReportRequest request) {
        Long studentId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail) {
            studentId = ((CustomUserDetail) authentication.getPrincipal()).getId();
        }

        notificationService.createNotification(
                "Báo cáo có nội dung vi phạm",
                request.getReason(),
                "REPORT",
                request.getRelatedCourseId(),
                request.getRelatedLessionId(),
                studentId);
        return ResponseEntity.ok(ApiResponse.success("Gửi báo cáo thành công", null));
    }
}
