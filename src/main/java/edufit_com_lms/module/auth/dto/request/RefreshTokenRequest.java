package edufit_com_lms.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for refreshing JWT tokens.
 */
@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token must not be blank")
    private String refreshToken;
}
