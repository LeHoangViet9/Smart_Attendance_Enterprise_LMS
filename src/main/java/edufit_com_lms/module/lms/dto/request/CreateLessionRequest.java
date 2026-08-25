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
public class CreateLessionRequest {
    @NotBlank(message = "Title must not be blank")
    private String title;

    private String content;

    private String videoUrl;

    private String documentUrl;

    private Integer orderIndex;
}
