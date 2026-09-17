package edufit_com_lms.module.attendance.service;

import edufit_com_lms.module.attendance.dto.request.AttendanceSubmitRequest;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {
    void submitAttendance(AttendanceSubmitRequest request);
    List<Long> verifyFaces(UUID classId, MultipartFile frame);
}
