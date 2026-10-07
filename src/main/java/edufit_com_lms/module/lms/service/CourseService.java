package edufit_com_lms.module.lms.service;

import edufit_com_lms.module.lms.dto.request.CreateCourseRequest;
import edufit_com_lms.module.lms.dto.request.CreateLessonRequest;
import edufit_com_lms.module.lms.dto.request.UpdateCourseRequest;
import edufit_com_lms.module.lms.dto.request.UpdateLessonRequest;
import edufit_com_lms.module.lms.dto.response.CourseResponse;
import edufit_com_lms.module.lms.dto.response.LessonResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CourseService {
    // Course Operations
    List<CourseResponse> getAllCourses();

    Page<CourseResponse> getPaginatedCourses(String keyword, Pageable pageable);


    Page<CourseResponse> getPaginatedCoursesByMajorId(UUID majorId, String keyword, Pageable pageable);
    
    Page<CourseResponse> getPaginatedCoursesByIds(List<UUID> ids, String keyword, Pageable pageable);

    CourseResponse getCourseById(UUID id);

    CourseResponse createCourse(CreateCourseRequest request, Long lecturerId);

    CourseResponse updateCourse(UUID id, UpdateCourseRequest request);

    void deleteCourse(UUID id);

    // Lesson Operations
    List<LessonResponse> getLessonsByCourseId(UUID courseId);

    Page<LessonResponse> getPaginatedLessonsByCourseId(UUID courseId, Pageable pageable);

    LessonResponse addLesson(UUID courseId, CreateLessonRequest request);

    LessonResponse updateLesson(UUID LessonId, UpdateLessonRequest request);

    void deleteLesson(UUID LessonId);
}
