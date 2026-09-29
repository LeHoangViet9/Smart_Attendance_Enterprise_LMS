package edufit_com_lms.module.attendance.dto.response;

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
public class AttendanceRecordResponse {
    private UUID id;
    private Long studentId;
    private String studentName;
    private UUID classId;
    private String className;
    private LocalDateTime checkInTime;
    private String status;
}
