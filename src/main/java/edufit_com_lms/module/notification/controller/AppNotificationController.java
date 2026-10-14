package edufit_com_lms.module.notification.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.notification.dto.request.ReportRequest;
import edufit_com_lms.module.notification.dto.response.AppNotificationResponse;
import edufit_com_lms.module.notification.service.AppNotificationService;
import edufit_com_lms.security.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AppNotificationController {

    private final AppNotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AppNotificationResponse>>> getAllNotifications(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getAllNotifications(getCurrentUserId(), pageable)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount(getCurrentUserId())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Marked all as read", null));
    }

    @PostMapping("/report")
    public ResponseEntity<ApiResponse<Void>> submitReport(@Valid @RequestBody ReportRequest request) {
        notificationService.createNotification(
                "Violation report",
                request.getReason(),
                "REPORT",
                request.getRelatedCourseId(),
                request.getRelatedLessonId(),
                getCurrentUserId(),
                null);
        return ResponseEntity.ok(ApiResponse.success("Report submitted successfully", null));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail userDetail) {
            return userDetail.getId();
        }
        return null;
    }
}
