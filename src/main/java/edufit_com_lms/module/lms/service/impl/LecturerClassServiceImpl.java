package edufit_com_lms.module.lms.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edufit_com_lms.common.exception.ConflictException;
import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.attendance.dto.response.EnrolledStudentResponse;
import edufit_com_lms.module.attendance.dto.response.StudentFaceDTO;
import edufit_com_lms.module.attendance.repository.AttendanceRepository;
import edufit_com_lms.module.lms.dto.response.SchoolClassResponse;
import edufit_com_lms.module.lms.entity.Assignment;
import edufit_com_lms.module.lms.entity.ClassEnrollment;
import edufit_com_lms.module.lms.entity.SchoolClass;
import edufit_com_lms.module.lms.repository.AssignmentRepository;
import edufit_com_lms.module.lms.repository.ClassEnrollmentRepository;
import edufit_com_lms.module.lms.repository.SchoolClassRepository;
import edufit_com_lms.module.lms.repository.SubmissionRepository;
import edufit_com_lms.module.lms.service.LecturerClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LecturerClassServiceImpl implements LecturerClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SchoolClassResponse> getMyClasses(Long lecturerId) {
        List<SchoolClass> classes = schoolClassRepository.findByLecturer_UserId(lecturerId);
        List<UUID> classIds = classes.stream().map(SchoolClass::getId).toList();
        Map<UUID, Long> studentCounts = getStudentCounts(classIds);

        return classes.stream()
                .map(cls -> toClassResponse(cls, studentCounts.getOrDefault(cls.getId(), 0L)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolClassResponse getClassDetails(Long lecturerId, UUID classId) {
        SchoolClass cls = getOwnedClass(lecturerId, classId);
        long studentCount = getStudentCounts(List.of(classId)).getOrDefault(classId, 0L);
        return toClassResponse(cls, studentCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrolledStudentResponse> getEnrolledStudents(Long lecturerId, UUID classId) {
        getOwnedClass(lecturerId, classId);

        List<ClassEnrollment> enrollments = classEnrollmentRepository.findBySchoolClassId(classId);
        Map<Long, AttendanceStats> attendanceStats = attendanceRepository.countAttendanceByClassIdGroupedByStudent(classId)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> new AttendanceStats(((Number) row[1]).longValue(), ((Number) row[2]).longValue())
                ));

        List<UUID> assignmentIds = assignmentRepository.findByClasses_Id(classId).stream()
                .map(Assignment::getId)
                .toList();
        Map<Long, Double> averageScores = assignmentIds.isEmpty()
                ? Collections.emptyMap()
                : submissionRepository.averageScoreByStudentForAssignments(assignmentIds).stream()
                        .collect(Collectors.toMap(
                                row -> ((Number) row[0]).longValue(),
                                row -> ((Number) row[1]).doubleValue()
                        ));

        return enrollments.stream()
                .filter(enrollment -> enrollment.getStudent() != null)
                .map(enrollment -> {
                    var student = enrollment.getStudent();
                    var profile = student.getStudentProfile();
                    String className = profile != null && profile.getSchoolClass() != null
                            ? profile.getSchoolClass().getClassName()
                            : "N/A";
                    AttendanceStats stats = attendanceStats.getOrDefault(student.getUserId(), new AttendanceStats(0, 0));
                    double attendanceRate = stats.total() == 0 ? 0.0 : ((double) stats.present() / stats.total()) * 100;
                    double avgScore = averageScores.getOrDefault(student.getUserId(), 0.0);

                    return EnrolledStudentResponse.builder()
                            .id(student.getUserId())
                            .fullName(student.getFullName())
                            .email(student.getEmail())
                            .phone(student.getPhone())
                            .avatarUrl(student.getAvatarUrl())
                            .className(className)
                            .enrollmentId(enrollment.getId())
                            .averageScore(Math.round(avgScore * 10.0) / 10.0)
                            .attendanceRate(Math.round(attendanceRate * 10.0) / 10.0)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentFaceDTO> getEnrolledStudentFaces(Long lecturerId, UUID classId) {
        getOwnedClass(lecturerId, classId);
        return classEnrollmentRepository.findBySchoolClassId(classId).stream()
                .filter(enrollment -> enrollment.getStudent() != null)
                .map(enrollment -> {
                    var student = enrollment.getStudent();
                    List<Float> descriptor = null;
                    if (student.getFaceEmbedding() != null) {
                        try {
                            descriptor = objectMapper.readValue(student.getFaceEmbedding(), new TypeReference<List<Float>>() {});
                        } catch (Exception ignored) {
                        }
                    }
                    return StudentFaceDTO.builder()
                            .id(student.getUserId())
                            .fullName(student.getFullName())
                            .faceDescriptor(descriptor)
                            .build();
                })
                .toList();
    }

    private SchoolClass getOwnedClass(Long lecturerId, UUID classId) {
        SchoolClass cls = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFound("Khong tim thay lop hoc phan"));
        if (cls.getLecturer() == null || !cls.getLecturer().getUserId().equals(lecturerId)) {
            throw new ConflictException("Ban khong phai giang vien cua lop nay");
        }
        return cls;
    }

    private Map<UUID, Long> getStudentCounts(List<UUID> classIds) {
        if (classIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return classEnrollmentRepository.countBySchoolClassIds(classIds).stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> ((Number) row[1]).longValue()));
    }

    private SchoolClassResponse toClassResponse(SchoolClass cls, long studentCount) {
        return SchoolClassResponse.builder()
                .id(cls.getId())
                .className(cls.getClassName())
                .majorName(cls.getMajor() != null ? cls.getMajor().getName() : "N/A")
                .entryYear(cls.getEntryYear())
                .lecturerId(cls.getLecturer() != null ? cls.getLecturer().getUserId() : null)
                .lecturerName(cls.getLecturer() != null ? cls.getLecturer().getFullName() : null)
                .courseId(cls.getCourse() != null ? cls.getCourse().getId() : null)
                .courseName(cls.getCourse() != null ? cls.getCourse().getTitle() : null)
                .studentCount(Math.toIntExact(studentCount))
                .build();
    }

    private record AttendanceStats(long total, long present) {
    }
}
