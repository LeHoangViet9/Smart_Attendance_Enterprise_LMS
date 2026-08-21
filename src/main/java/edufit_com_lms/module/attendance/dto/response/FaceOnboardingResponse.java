package edufit_com_lms.module.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FaceOnboardingResponse {
    private String message;
    private String avatarUrl;
    private boolean isVectorGenerated;
}
