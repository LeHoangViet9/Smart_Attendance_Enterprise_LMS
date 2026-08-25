package edufit_com_lms.module.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsResponse {
    private long totalUsers;
    private long totalQuizzes;
    private long totalCourses;
    private long totalSubmissions;
}
