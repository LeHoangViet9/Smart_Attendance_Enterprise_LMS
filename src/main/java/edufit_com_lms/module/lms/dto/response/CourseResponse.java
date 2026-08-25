package edufit_com_lms.module.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private UUID id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private Long lecturerId;
    private String lecturerName;
    private Boolean isPublished;
    private Integer totalLessons;
    private LocalDateTime createdAt;
    private List<LessionResponse> lessions;
}
