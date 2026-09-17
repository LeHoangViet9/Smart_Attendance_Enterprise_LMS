package edufit_com_lms.module.attendance.dto.request;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class AttendanceSubmitRequest {
    private UUID classId;
    private List<Long> presentStudentIds;
}
