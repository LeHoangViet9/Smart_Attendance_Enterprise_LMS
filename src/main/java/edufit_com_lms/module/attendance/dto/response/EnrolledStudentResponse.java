package edufit_com_lms.module.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrolledStudentResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String className;
    private UUID enrollmentId;

    // Future stats (to be expanded)
    private Double averageScore;
    private Double attendanceRate;
}
