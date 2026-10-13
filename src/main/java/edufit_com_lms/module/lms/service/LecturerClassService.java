package edufit_com_lms.module.lms.service;

import edufit_com_lms.module.attendance.dto.response.EnrolledStudentResponse;
import edufit_com_lms.module.attendance.dto.response.StudentFaceDTO;
import edufit_com_lms.module.lms.dto.response.SchoolClassResponse;

import java.util.List;
import java.util.UUID;

public interface LecturerClassService {
    List<SchoolClassResponse> getMyClasses(Long lecturerId);

    SchoolClassResponse getClassDetails(Long lecturerId, UUID classId);

    List<EnrolledStudentResponse> getEnrolledStudents(Long lecturerId, UUID classId);

    List<StudentFaceDTO> getEnrolledStudentFaces(Long lecturerId, UUID classId);
}
