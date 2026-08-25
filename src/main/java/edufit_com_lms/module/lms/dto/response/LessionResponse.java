package edufit_com_lms.module.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessionResponse {
    private UUID id;
    private UUID courseId;
    private String title;
    private String content;
    private String videoUrl;
    private String documentUrl;
    private Integer orderIndex;
    private LocalDateTime createdAt;
}
