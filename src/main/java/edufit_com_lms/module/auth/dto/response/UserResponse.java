package edufit_com_lms.module.auth.dto.response;

import java.time.LocalDateTime;

import edufit_com_lms.module.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long userId;
    private String email;
    private String fullName;
    private String phone;
    private String code;
    private String address;
    private String avatarUrl;
    private Role role;
    private Boolean isActive;

    // Auth fields (for login response)
    private String accessToken;
    private String refreshToken;

    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Profile Fields
    private String parentPhone;
    private String className;
    private Integer enrollmentYear;

    private String degree;
    private String major;
    private String department;
}
