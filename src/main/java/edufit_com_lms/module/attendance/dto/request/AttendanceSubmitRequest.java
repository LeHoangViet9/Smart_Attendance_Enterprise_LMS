package edufit_com_lms.module.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AttendanceSubmitRequest {
    @NotNull
    private UUID classId;

    @NotNull
    private List<Long> presentStudentIds;

    private List<Long> lateStudentIds;
}
