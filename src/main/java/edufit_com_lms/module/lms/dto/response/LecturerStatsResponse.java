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
    private long totalClasses;
    private long pendingGradingSubmissions;
    private String attendanceRate;
    /** BRD 8.3: Danh sÃ¡ch Lá»›p há»c pháº§n (Class) cá»§a Giáº£ng viÃªn */
    private List<SchoolClassResponse> activeClasses;
}
