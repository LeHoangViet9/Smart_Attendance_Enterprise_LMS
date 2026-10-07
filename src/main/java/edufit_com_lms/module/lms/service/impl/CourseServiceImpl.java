package edufit_com_lms.module.lms.service.impl;

import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.lms.dto.request.CreateCourseRequest;
import edufit_com_lms.module.lms.dto.request.CreateLessonRequest;
import edufit_com_lms.module.lms.dto.request.UpdateCourseRequest;
import edufit_com_lms.module.lms.dto.request.UpdateLessonRequest;
import edufit_com_lms.module.lms.dto.response.CourseResponse;
import edufit_com_lms.module.lms.dto.response.LessonResponse;
import edufit_com_lms.module.lms.entity.Courses;
import edufit_com_lms.module.lms.entity.Lesson;
import edufit_com_lms.module.lms.entity.Major;
import edufit_com_lms.module.lms.repository.CourseRepository;
import edufit_com_lms.module.lms.repository.LessonRepository;
import edufit_com_lms.module.lms.service.CourseService;
import edufit_com_lms.module.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToCourseSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getPaginatedCourses(String keyword, Pageable pageable) {
        Page<Courses> pageResult;
        if (keyword != null && !keyword.isBlank()) {
            pageResult = courseRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else {
            pageResult = courseRepository.findAll(pageable);
        }

        List<CourseResponse> content = pageResult.getContent().stream()
                .map(this::mapToCourseSummaryResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, pageResult.getTotalElements());
    }


    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getPaginatedCoursesByMajorId(UUID majorId, String keyword, Pageable pageable) {
        Page<Courses> pageResult;
        if (keyword != null && !keyword.isBlank()) {
            pageResult = courseRepository.findByMajorIdAndTitleContainingIgnoreCase(majorId, keyword, pageable);
        } else {
            pageResult = courseRepository.findByMajorId(majorId, pageable);
        }

        List<CourseResponse> content = pageResult.getContent().stream()
                .map(this::mapToCourseSummaryResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, pageResult.getTotalElements());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getPaginatedCoursesByIds(List<UUID> ids, String keyword, Pageable pageable) {
        Page<Courses> pageResult;
        if (keyword != null && !keyword.isBlank()) {
            pageResult = courseRepository.findByIdInAndTitleContainingIgnoreCase(ids, keyword, pageable);
        } else {
            pageResult = courseRepository.findByIdIn(ids, pageable);
        }

        List<CourseResponse> content = pageResult.getContent().stream()
                .map(this::mapToCourseSummaryResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, pageResult.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(UUID id) {
        Courses course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Course not found with ID: " + id));

        List<LessonResponse> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(id).stream()
                .map(this::mapToLessonResponse)
                .collect(Collectors.toList());

        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .isPublished(course.getIsPublished())
                .totalLessons(lessons.size())
                .createdAt(course.getCreatedAt())
                .lessons(lessons)
                .build();
    }

    @Override
    public CourseResponse createCourse(CreateCourseRequest request, Long lecturerId) {
        Major assignedMajor = null;
        if (lecturerId != null) {
            edufit_com_lms.module.auth.entity.LecturerProfile profile = 
                userRepository.findById(lecturerId)
                    .map(edufit_com_lms.module.auth.entity.User::getLecturerProfile)
                    .orElse(null);
            if (profile != null) {
                assignedMajor = profile.getMajor();
            }
        }

        Courses course = Courses.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .isPublished(request.getIsPublished() != null ? request.getIsPublished() : true)
                .major(assignedMajor)
                .build();

        Courses saved = courseRepository.save(course);
        log.info("Created course: {}", saved.getTitle());

        eventPublisher.publishEvent(NotificationEvent.builder()
                .title("KhÃƒÂ³a hÃ¡Â»Âc mÃ¡Â»â€ºi: " + saved.getTitle())
                .message("GiÃ¡ÂºÂ£ng viÃƒÂªn vÃ¡Â»Â«a tÃ¡ÂºÂ¡o khÃƒÂ³a hÃ¡Â»Âc mÃ¡Â»â€ºi. Vui lÃƒÂ²ng kiÃ¡Â»Æ’m duyÃ¡Â»â€¡t.")
                .type("SYSTEM_LOG")
                .relatedCourseId(saved.getId())
                .build());

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
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(id);
        lessonRepository.deleteAll(lessons);
        courseRepository.deleteById(id);
        log.info("Deleted course ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsByCourseId(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFound("Course not found with ID: " + courseId);
        }
        return lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(this::mapToLessonResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LessonResponse> getPaginatedLessonsByCourseId(UUID courseId, Pageable pageable) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFound("Course not found with ID: " + courseId);
        }
        Page<Lesson> pageResult = lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId, pageable);
        List<LessonResponse> content = pageResult.getContent().stream()
                .map(this::mapToLessonResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, pageResult.getTotalElements());
    }

    @Override
    public LessonResponse addLesson(UUID courseId, CreateLessonRequest request) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFound("Course not found with ID: " + courseId);
        }

        int nextOrder = request.getOrderIndex() != null ? request.getOrderIndex() : 1;
        if (request.getOrderIndex() == null) {
            List<Lesson> existing = lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
            nextOrder = existing.size() + 1;
        }

        Lesson lesson = Lesson.builder()
                .courseId(courseId)
                .title(request.getTitle())
                .content(request.getContent())
                .videoUrl(request.getVideoUrl())
                .documentUrl(request.getDocumentUrl())
                .orderIndex(nextOrder)
                .isPublished(request.getIsPublished() != null ? request.getIsPublished() : true)
                .build();

        Lesson saved = lessonRepository.save(lesson);
        log.info("Added Lesson: {} to course {}", saved.getTitle(), courseId);

        eventPublisher.publishEvent(NotificationEvent.builder()
                .title("BÃƒÂ i hÃ¡Â»Âc mÃ¡Â»â€ºi: " + saved.getTitle())
                .message("GiÃ¡ÂºÂ£ng viÃƒÂªn vÃ¡Â»Â«a thÃƒÂªm bÃƒÂ i hÃ¡Â»Âc mÃ¡Â»â€ºi. Vui lÃƒÂ²ng kiÃ¡Â»Æ’m duyÃ¡Â»â€¡t.")
                .type("SYSTEM_LOG")
                .relatedCourseId(courseId)
                .relatedLessonId(saved.getId())
                .build());

        return mapToLessonResponse(saved);
    }

    @Override
    public LessonResponse updateLesson(UUID LessonId, UpdateLessonRequest request) {
        Lesson lesson = lessonRepository.findById(LessonId)
                .orElseThrow(() -> new ResourceNotFound("Lesson not found with ID: " + LessonId));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            lesson.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            lesson.setContent(request.getContent());
        }
        if (request.getVideoUrl() != null) {
            lesson.setVideoUrl(request.getVideoUrl());
        }
        if (request.getDocumentUrl() != null) {
            lesson.setDocumentUrl(request.getDocumentUrl());
        }
        if (request.getOrderIndex() != null) {
            lesson.setOrderIndex(request.getOrderIndex());
        }
        if (request.getIsPublished() != null) {
            lesson.setIsPublished(request.getIsPublished());
        }

        Lesson updated = lessonRepository.save(lesson);
        log.info("Updated Lesson ID: {}", LessonId);
        return mapToLessonResponse(updated);
    }

    @Override
    public void deleteLesson(UUID LessonId) {
        if (!lessonRepository.existsById(LessonId)) {
            throw new ResourceNotFound("Lesson not found with ID: " + LessonId);
        }
        lessonRepository.deleteById(LessonId);
        log.info("Deleted Lesson ID: {}", LessonId);
    }

    private CourseResponse mapToCourseSummaryResponse(Courses course) {
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());

        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .isPublished(course.getIsPublished())
                .totalLessons(lessons.size())
                .createdAt(course.getCreatedAt())
                .build();
    }

    private LessonResponse mapToLessonResponse(Lesson lesson) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .courseId(lesson.getCourseId())
                .title(lesson.getTitle())
                .content(lesson.getContent())
                .videoUrl(lesson.getVideoUrl())
                .documentUrl(lesson.getDocumentUrl())
                .orderIndex(lesson.getOrderIndex())
                .isPublished(lesson.getIsPublished())
                .createdAt(lesson.getCreatedAt())
                .build();
    }
}
