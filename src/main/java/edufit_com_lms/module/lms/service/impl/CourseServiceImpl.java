package edufit_com_lms.module.lms.service.impl;

import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.lms.dto.request.CreateCourseRequest;
import edufit_com_lms.module.lms.dto.request.CreateLessionRequest;
import edufit_com_lms.module.lms.dto.request.UpdateCourseRequest;
import edufit_com_lms.module.lms.dto.request.UpdateLessionRequest;
import edufit_com_lms.module.lms.dto.response.CourseResponse;
import edufit_com_lms.module.lms.dto.response.LessionResponse;
import edufit_com_lms.module.lms.entity.Courses;
import edufit_com_lms.module.lms.entity.Lession;
import edufit_com_lms.module.lms.repository.CourseRepository;
import edufit_com_lms.module.lms.repository.LessionRepository;
import edufit_com_lms.module.lms.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final LessionRepository lessionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToCourseSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByLecturer(Long lecturerId) {
        return courseRepository.findByLecturerId(lecturerId).stream()
                .map(this::mapToCourseSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(UUID id) {
        Courses course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Course not found with ID: " + id));

        List<LessionResponse> lessions = lessionRepository.findByCourseIdOrderByOrderIndexAsc(id).stream()
                .map(this::mapToLessionResponse)
                .collect(Collectors.toList());

        String lecturerName = null;
        if (course.getLecturerId() != null) {
            lecturerName = userRepository.findById(course.getLecturerId())
                    .map(User::getFullName)
                    .orElse("Lecturer");
        }

        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .lecturerId(course.getLecturerId())
                .lecturerName(lecturerName)
                .isPublished(course.getIsPublished())
                .totalLessons(lessions.size())
                .createdAt(course.getCreatedAt())
                .lessions(lessions)
                .build();
    }

    @Override
    public CourseResponse createCourse(CreateCourseRequest request, Long lecturerId) {
        Courses course = Courses.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .lecturerId(lecturerId)
                .isPublished(request.getIsPublished() != null ? request.getIsPublished() : true)
                .build();

        Courses saved = courseRepository.save(course);
        log.info("Created course: {} by lecturer {}", saved.getTitle(), lecturerId);
        return mapToCourseSummaryResponse(saved);
    }

    @Override
    public CourseResponse updateCourse(UUID id, UpdateCourseRequest request) {
        Courses course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Course not found with ID: " + id));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            course.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getThumbnailUrl() != null) {
            course.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getIsPublished() != null) {
            course.setIsPublished(request.getIsPublished());
        }

        Courses updated = courseRepository.save(course);
        log.info("Updated course ID: {}", id);
        return mapToCourseSummaryResponse(updated);
    }

    @Override
    public void deleteCourse(UUID id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFound("Course not found with ID: " + id);
        }
        List<Lession> lessions = lessionRepository.findByCourseIdOrderByOrderIndexAsc(id);
        lessionRepository.deleteAll(lessions);
        courseRepository.deleteById(id);
        log.info("Deleted course ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessionResponse> getLessionsByCourseId(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFound("Course not found with ID: " + courseId);
        }
        return lessionRepository.findByCourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(this::mapToLessionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LessionResponse addLession(UUID courseId, CreateLessionRequest request) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFound("Course not found with ID: " + courseId);
        }

        int nextOrder = request.getOrderIndex() != null ? request.getOrderIndex() : 1;
        if (request.getOrderIndex() == null) {
            List<Lession> existing = lessionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
            nextOrder = existing.size() + 1;
        }

        Lession lession = Lession.builder()
                .courseId(courseId)
                .title(request.getTitle())
                .content(request.getContent())
                .videoUrl(request.getVideoUrl())
                .documentUrl(request.getDocumentUrl())
                .orderIndex(nextOrder)
                .build();

        Lession saved = lessionRepository.save(lession);
        log.info("Added lession: {} to course {}", saved.getTitle(), courseId);
        return mapToLessionResponse(saved);
    }

    @Override
    public LessionResponse updateLession(UUID lessionId, UpdateLessionRequest request) {
        Lession lession = lessionRepository.findById(lessionId)
                .orElseThrow(() -> new ResourceNotFound("Lession not found with ID: " + lessionId));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            lession.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            lession.setContent(request.getContent());
        }
        if (request.getVideoUrl() != null) {
            lession.setVideoUrl(request.getVideoUrl());
        }
        if (request.getDocumentUrl() != null) {
            lession.setDocumentUrl(request.getDocumentUrl());
        }
        if (request.getOrderIndex() != null) {
            lession.setOrderIndex(request.getOrderIndex());
        }

        Lession updated = lessionRepository.save(lession);
        log.info("Updated lession ID: {}", lessionId);
        return mapToLessionResponse(updated);
    }

    @Override
    public void deleteLession(UUID lessionId) {
        if (!lessionRepository.existsById(lessionId)) {
            throw new ResourceNotFound("Lession not found with ID: " + lessionId);
        }
        lessionRepository.deleteById(lessionId);
        log.info("Deleted lession ID: {}", lessionId);
    }

    private CourseResponse mapToCourseSummaryResponse(Courses course) {
        String lecturerName = null;
        if (course.getLecturerId() != null) {
            lecturerName = userRepository.findById(course.getLecturerId())
                    .map(User::getFullName)
                    .orElse("Lecturer");
        }

        List<Lession> lessions = lessionRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());

        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .lecturerId(course.getLecturerId())
                .lecturerName(lecturerName)
                .isPublished(course.getIsPublished())
                .totalLessons(lessions.size())
                .createdAt(course.getCreatedAt())
                .build();
    }

    private LessionResponse mapToLessionResponse(Lession lession) {
        return LessionResponse.builder()
                .id(lession.getId())
                .courseId(lession.getCourseId())
                .title(lession.getTitle())
                .content(lession.getContent())
                .videoUrl(lession.getVideoUrl())
                .documentUrl(lession.getDocumentUrl())
                .orderIndex(lession.getOrderIndex())
                .createdAt(lession.getCreatedAt())
                .build();
    }
}
