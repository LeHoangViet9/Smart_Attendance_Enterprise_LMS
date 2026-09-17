package edufit_com_lms.module.lms.service;

import edufit_com_lms.module.lms.dto.request.CreateCourseRequest;
import edufit_com_lms.module.lms.dto.request.CreateLessionRequest;
import edufit_com_lms.module.lms.dto.request.UpdateCourseRequest;
import edufit_com_lms.module.lms.dto.request.UpdateLessionRequest;
import edufit_com_lms.module.lms.dto.response.CourseResponse;
import edufit_com_lms.module.lms.dto.response.LessionResponse;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseService {
    // Course Operations
    List<CourseResponse> getAllCourses();

    Page<CourseResponse> getPaginatedCourses(String keyword, Pageable pageable);


    Page<CourseResponse> getPaginatedCoursesByMajorId(UUID majorId, String keyword, Pageable pageable);

    CourseResponse getCourseById(UUID id);

    CourseResponse createCourse(CreateCourseRequest request, Long lecturerId);

    CourseResponse updateCourse(UUID id, UpdateCourseRequest request);

    void deleteCourse(UUID id);

    // Lession Operations
    List<LessionResponse> getLessionsByCourseId(UUID courseId);

    Page<LessionResponse> getPaginatedLessionsByCourseId(UUID courseId, Pageable pageable);

    LessionResponse addLession(UUID courseId, CreateLessionRequest request);

    LessionResponse updateLession(UUID lessionId, UpdateLessionRequest request);

    void deleteLession(UUID lessionId);
}
