package edufit_com_lms.module.lms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLessionRequest {
    private String title;
    private String content;
    private String videoUrl;
    private String documentUrl;
    private Integer orderIndex;
    private Boolean isPublished;
}
