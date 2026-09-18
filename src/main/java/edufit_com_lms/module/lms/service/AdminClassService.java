package edufit_com_lms.module.lms.service;

import edufit_com_lms.module.lms.dto.response.SchoolClassResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminClassService {
    Page<SchoolClassResponse> getAllClasses(Pageable pageable);
    void assignHomeroomLecturer(UUID classId, Long lecturerId);
    void autoAssignStudents();
    void autoAssignLecturers();

}
