package edufit_com_lms.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message = "Máº­t kháº©u cÅ© khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String oldPassword;
    @NotBlank(message = "Máº­t kháº©u má»›i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Size( min = 8, message = "Máº­t kháº©u má»›i pháº£i dÃ i Ã­t nháº¥t 6 kÃ½ tá»±")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",message = "Máº­t kháº©u khÃ´ng há»£p lá»‡")
    private String newPassword;
}
