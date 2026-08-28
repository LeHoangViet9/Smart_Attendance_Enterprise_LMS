package edufit_com_lms.module.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ReportRequest {
    @NotBlank(message = "Reason is required")
    private String reason;
    private UUID relatedCourseId;
    private UUID relatedLessionId;
}
