package edufit_com_lms.module.lms.service.impl;

import edufit_com_lms.module.lms.dto.response.CourseResponse;
import edufit_com_lms.module.lms.dto.response.LecturerStatsResponse;
import edufit_com_lms.module.lms.repository.CourseRepository;
import edufit_com_lms.module.lms.repository.SubmissionRepository;
import edufit_com_lms.module.lms.service.CourseService;
import edufit_com_lms.module.lms.service.LecturerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LecturerServiceImpl implements LecturerService {

    private final CourseRepository courseRepository;
    private final SubmissionRepository submissionRepository;
    private final CourseService courseService;

    @Override
    public LecturerStatsResponse getLecturerStats(Long lecturerId) {
        long totalCourses = courseRepository.findByLecturerId(lecturerId).size();
        long pendingSubmissions = submissionRepository.countPendingGradingByLecturer(lecturerId);

        List<CourseResponse> activeCourses = courseService.getCoursesByLecturer(lecturerId);

        return LecturerStatsResponse.builder()
                .totalCourses(totalCourses)
                .pendingGradingSubmissions(pendingSubmissions)
                .attendanceRate("94%") // Default placeholder since attendance module is not fully merged yet
                .activeCourses(activeCourses)
                .build();
    }
}
