package edufit_com_lms.module.lms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseRequest {
    private String title;
    private String description;
    private String thumbnailUrl;
    private Boolean isPublished;
}
