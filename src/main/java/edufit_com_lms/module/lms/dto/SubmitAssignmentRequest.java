package edufit_com_lms.module.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAssignmentRequest {
    @NotNull(message = "studentId must not be null")
    private UUID studentId;

    @NotBlank(message = "fileUrl must not be blank")
    private String fileUrl;
}
