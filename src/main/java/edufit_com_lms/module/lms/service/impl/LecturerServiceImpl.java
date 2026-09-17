package edufit_com_lms.module.lms.service.impl;

import edufit_com_lms.module.attendance.repository.AttendanceRepository;
import edufit_com_lms.module.lms.dto.response.SchoolClassResponse;
import edufit_com_lms.module.lms.dto.response.LecturerStatsResponse;
import edufit_com_lms.module.lms.entity.SchoolClass;
import edufit_com_lms.module.lms.repository.ClassEnrollmentRepository;
import edufit_com_lms.module.lms.repository.SchoolClassRepository;
import edufit_com_lms.module.lms.repository.SubmissionRepository;
import edufit_com_lms.module.lms.service.LecturerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LecturerServiceImpl implements LecturerService {

    private final SchoolClassRepository schoolClassRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final SubmissionRepository submissionRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional(readOnly = true)
    @Override
    public LecturerStatsResponse getLecturerStats(Long lecturerId) {
        // BRD 8.3: Classes (Lớp học phần) mà Giảng viên phụ trách
        List<SchoolClass> classes = schoolClassRepository.findByLecturer_UserId(lecturerId);
        long totalClasses = classes.size();

        List<SchoolClassResponse> activeClasses = classes.stream()
                .map(cls -> SchoolClassResponse.builder()
                        .id(cls.getId())
                        .className(cls.getClassName())
                        .majorName(cls.getMajor() != null ? cls.getMajor().getName() : null)
                        .entryYear(cls.getEntryYear())
                        .lecturerId(cls.getLecturer() != null ? cls.getLecturer().getUserId() : null)
                        .lecturerName(cls.getLecturer() != null ? cls.getLecturer().getFullName() : null)
                        .courseId(cls.getCourse() != null ? cls.getCourse().getId() : null)
                        .courseName(cls.getCourse() != null ? cls.getCourse().getTitle() : null)
                        .studentCount(classEnrollmentRepository.findBySchoolClassId(cls.getId()).size())
                        .build())
                .collect(Collectors.toList());

        // BRD OBJ-03: Pending grading submissions
        long pendingSubmissions = submissionRepository.countPendingGradingByLecturer(lecturerId);

        // BRD OBJ-05: Attendance rate
        long totalAttendance = attendanceRepository.countTotalAttendanceByLecturer(lecturerId);
        long presentAttendance = attendanceRepository.countPresentAttendanceByLecturer(lecturerId);

        String attendanceRate = "0%";
        if (totalAttendance > 0) {
            double rate = ((double) presentAttendance / totalAttendance) * 100;
            attendanceRate = String.format("%.0f%%", rate);
        }

        return LecturerStatsResponse.builder()
                .totalClasses(totalClasses)
                .pendingGradingSubmissions(pendingSubmissions)
                .attendanceRate(attendanceRate)
                .activeClasses(activeClasses)
                .build();
    }
}
