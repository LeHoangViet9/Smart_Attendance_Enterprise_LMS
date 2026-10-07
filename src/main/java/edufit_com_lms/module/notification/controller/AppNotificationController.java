package edufit_com_lms.module.notification.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.notification.dto.request.ReportRequest;
import edufit_com_lms.module.notification.service.AppNotificationService;
import edufit_com_lms.security.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AppNotificationController {

    private final AppNotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<edufit_com_lms.module.notification.dto.response.AppNotificationResponse>>> getAllNotifications(
            @org.springframework.data.web.PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable) {
        Long userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail) {
            userId = ((CustomUserDetail) authentication.getPrincipal()).getId();
        }
        return ResponseEntity.ok(ApiResponse.success(notificationService.getAllNotifications(userId, pageable)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        Long userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail) {
            userId = ((CustomUserDetail) authentication.getPrincipal()).getId();
        }
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount(userId)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id) {
        Long userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail) {
            userId = ((CustomUserDetail) authentication.getPrincipal()).getId();
        }
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Ã„ÂÃƒÂ£ Ã„â€˜ÃƒÂ¡nh dÃ¡ÂºÂ¥u Ã„â€˜Ã¡Â»Âc", null));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        Long userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail) {
            userId = ((CustomUserDetail) authentication.getPrincipal()).getId();
        }
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("Ã„ÂÃƒÂ£ Ã„â€˜ÃƒÂ¡nh dÃ¡ÂºÂ¥u Ã„â€˜Ã¡Â»Âc tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£", null));
    }

    @PostMapping("/report")
    public ResponseEntity<ApiResponse<Void>> submitReport(@Valid @RequestBody ReportRequest request) {
        Long studentId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail) {
            studentId = ((CustomUserDetail) authentication.getPrincipal()).getId();
        }

        notificationService.createNotification(
                "BÃƒÂ¡o cÃƒÂ¡o cÃƒÂ³ nÃ¡Â»â„¢i dung vi phÃ¡ÂºÂ¡m",
                request.getReason(),
                "REPORT",
                request.getRelatedCourseId(),
                request.getRelatedLessonId(),
                studentId,
                null);
        return ResponseEntity.ok(ApiResponse.success("GÃ¡Â»Â­i bÃƒÂ¡o cÃƒÂ¡o thÃƒÂ nh cÃƒÂ´ng", null));
    }
}
