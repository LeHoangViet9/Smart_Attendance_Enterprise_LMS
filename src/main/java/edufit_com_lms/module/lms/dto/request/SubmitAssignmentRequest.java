package edufit_com_lms.module.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAssignmentRequest {
    private Long studentId;

    @NotBlank(message = "fileUrl must not be blank")
    private String fileUrl;
}
