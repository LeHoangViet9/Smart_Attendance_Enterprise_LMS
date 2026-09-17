package edufit_com_lms.module.lms.service.impl;

import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.lms.dto.request.CreateCourseRequest;
import edufit_com_lms.module.lms.dto.request.CreateLessionRequest;
import edufit_com_lms.module.lms.dto.request.UpdateCourseRequest;
import edufit_com_lms.module.lms.dto.request.UpdateLessionRequest;
import edufit_com_lms.module.lms.dto.response.CourseResponse;
import edufit_com_lms.module.lms.dto.response.LessionResponse;
import edufit_com_lms.module.lms.entity.Courses;
import edufit_com_lms.module.lms.entity.Lesson;
import edufit_com_lms.module.lms.entity.Major;
import edufit_com_lms.module.notification.event.NotificationEvent;
import edufit_com_lms.module.lms.repository.CourseRepository;
import edufit_com_lms.module.lms.repository.LessionRepository;
import edufit_com_lms.module.lms.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final LessionRepository lessionRepository;
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
    public CourseResponse getCourseById(UUID id) {
        Courses course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Course not found with ID: " + id));

        List<LessionResponse> lessions = lessionRepository.findByCourseIdOrderByOrderIndexAsc(id).stream()
                .map(this::mapToLessionResponse)
                .collect(Collectors.toList());

        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .isPublished(course.getIsPublished())
                .totalLessons(lessions.size())
                .createdAt(course.getCreatedAt())
                .lessions(lessions)
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
                .title("Khóa học mới: " + saved.getTitle())
                .message("Giảng viên vừa tạo khóa học mới. Vui lòng kiểm duyệt.")
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
        List<Lesson> lessions = lessionRepository.findByCourseIdOrderByOrderIndexAsc(id);
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
    @Transactional(readOnly = true)
    public Page<LessionResponse> getPaginatedLessionsByCourseId(UUID courseId, Pageable pageable) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFound("Course not found with ID: " + courseId);
        }
        Page<Lesson> pageResult = lessionRepository.findByCourseIdOrderByOrderIndexAsc(courseId, pageable);
        List<LessionResponse> content = pageResult.getContent().stream()
                .map(this::mapToLessionResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, pageResult.getTotalElements());
    }

    @Override
    public LessionResponse addLession(UUID courseId, CreateLessionRequest request) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFound("Course not found with ID: " + courseId);
        }

        int nextOrder = request.getOrderIndex() != null ? request.getOrderIndex() : 1;
        if (request.getOrderIndex() == null) {
            List<Lesson> existing = lessionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
            nextOrder = existing.size() + 1;
        }

        Lesson lession = Lesson.builder()
                .courseId(courseId)
                .title(request.getTitle())
                .content(request.getContent())
                .videoUrl(request.getVideoUrl())
                .documentUrl(request.getDocumentUrl())
                .orderIndex(nextOrder)
                .isPublished(request.getIsPublished() != null ? request.getIsPublished() : true)
                .build();

        Lesson saved = lessionRepository.save(lession);
        log.info("Added lession: {} to course {}", saved.getTitle(), courseId);

        eventPublisher.publishEvent(NotificationEvent.builder()
                .title("Bài học mới: " + saved.getTitle())
                .message("Giảng viên vừa thêm bài học mới. Vui lòng kiểm duyệt.")
                .type("SYSTEM_LOG")
                .relatedCourseId(courseId)
                .relatedLessionId(saved.getId())
                .build());

        return mapToLessionResponse(saved);
    }

    @Override
    public LessionResponse updateLession(UUID lessionId, UpdateLessionRequest request) {
        Lesson lession = lessionRepository.findById(lessionId)
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
        if (request.getIsPublished() != null) {
            lession.setIsPublished(request.getIsPublished());
        }

        Lesson updated = lessionRepository.save(lession);
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
        List<Lesson> lessions = lessionRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());

        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .isPublished(course.getIsPublished())
                .totalLessons(lessions.size())
                .createdAt(course.getCreatedAt())
                .build();
    }

    private LessionResponse mapToLessionResponse(Lesson lession) {
        return LessionResponse.builder()
                .id(lession.getId())
                .courseId(lession.getCourseId())
                .title(lession.getTitle())
                .content(lession.getContent())
                .videoUrl(lession.getVideoUrl())
                .documentUrl(lession.getDocumentUrl())
                .orderIndex(lession.getOrderIndex())
                .isPublished(lession.getIsPublished())
                .createdAt(lession.getCreatedAt())
                .build();
    }
}
