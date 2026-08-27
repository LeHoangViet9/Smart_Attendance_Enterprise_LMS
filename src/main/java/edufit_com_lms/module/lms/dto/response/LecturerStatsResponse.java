package edufit_com_lms.module.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LecturerStatsResponse {
    private long totalCourses;
    private long pendingGradingSubmissions;
    private String attendanceRate;
    private List<CourseResponse> activeCourses;
}
