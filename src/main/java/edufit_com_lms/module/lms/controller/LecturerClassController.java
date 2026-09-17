package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.attendance.dto.response.EnrolledStudentResponse;
import edufit_com_lms.module.attendance.dto.response.StudentFaceDTO;
import edufit_com_lms.module.lms.dto.response.SchoolClassResponse;
import edufit_com_lms.module.lms.entity.ClassEnrollment;
import edufit_com_lms.module.lms.entity.SchoolClass;
import edufit_com_lms.module.lms.repository.ClassEnrollmentRepository;
import edufit_com_lms.module.lms.repository.SchoolClassRepository;
import edufit_com_lms.security.CustomUserDetail;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edufit_com_lms.module.attendance.repository.AttendanceRepository;
import edufit_com_lms.module.lms.repository.SubmissionRepository;
import edufit_com_lms.module.lms.repository.AssignmentRepository;
import edufit_com_lms.module.lms.entity.Assignment;
import edufit_com_lms.module.lms.entity.Submission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller quản lý Lớp học phần của Giảng viên.
 * BRD 6.2: Lecturer xem Class được phân công, xem danh sách Student, điểm danh.
 * BRD 8.3: Một Class thuộc một Course. Một Class có Lecturer phụ trách.
 */
@RestController
@RequestMapping("/api/v1/lecturer/classes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LECTURER')")
public class LecturerClassController {

    private final SchoolClassRepository schoolClassRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * GET /v1/lecturer/classes
     * Trả về danh sách Lớp học phần (Class) mà Giảng viên đang phụ trách.
     * BRD 8.3: Lecturer Assignment.
     */
    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolClassResponse>>> getMyClasses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        Long lecturerId = userDetails.getId();

        List<SchoolClass> classes = schoolClassRepository.findByLecturer_UserId(lecturerId);
        List<SchoolClassResponse> responses = classes.stream()
                .map(cls -> SchoolClassResponse.builder()
                        .id(cls.getId())
                        .className(cls.getClassName())
                        .majorName(cls.getMajor() != null ? cls.getMajor().getName() : "N/A")
                        .entryYear(cls.getEntryYear())
                        .lecturerId(cls.getLecturer() != null ? cls.getLecturer().getUserId() : null)
                        .lecturerName(cls.getLecturer() != null ? cls.getLecturer().getFullName() : null)
                        .courseId(cls.getCourse() != null ? cls.getCourse().getId() : null)
                        .courseName(cls.getCourse() != null ? cls.getCourse().getTitle() : null)
                        .studentCount(classEnrollmentRepository.findBySchoolClassId(cls.getId()).size())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched your classes", null, responses, HttpStatus.OK));
    }

    /**
     * GET /v1/lecturer/classes/{classId}/students
     * Trả về danh sách Sinh viên trong một Lớp học phần.
     * BRD 8.3: Student chỉ được truy cập Class mà mình được enrollment.
     */
    @Transactional(readOnly = true)
    @GetMapping("/{classId}/students")
    public ResponseEntity<ApiResponse<List<EnrolledStudentResponse>>> getEnrolledStudents(@PathVariable UUID classId) {
        List<ClassEnrollment> enrollments = classEnrollmentRepository.findBySchoolClassId(classId);

        List<EnrolledStudentResponse> responses = enrollments.stream().map(enrollment -> {
            var student = enrollment.getStudent();
            var profile = student.getStudentProfile();

            String className = "N/A";
            if (profile != null && profile.getSchoolClass() != null) {
                className = profile.getSchoolClass().getClassName();
            }

            long totalAttendance = attendanceRepository.countTotalAttendanceByClassAndStudent(classId, student.getUserId());
            long presentAttendance = attendanceRepository.countPresentAttendanceByClassAndStudent(classId, student.getUserId());
            double attendanceRate = totalAttendance == 0 ? 0.0 : ((double) presentAttendance / totalAttendance) * 100;
            
            // Tính điểm trung bình qua Java để tránh bug Hibernate 7 UUID/bigint type mismatch
            List<UUID> assignmentIds = assignmentRepository.findByClassId(classId)
                    .stream().map(Assignment::getId).collect(Collectors.toList());
            List<Submission> studentSubmissions = submissionRepository
                    .findByStudentIdAndAssignmentIdIn(student.getUserId(), assignmentIds);
            double avgScore = studentSubmissions.stream()
                    .filter(s -> s.getScore() != null)
                    .mapToDouble(Submission::getScore)
                    .average()
                    .orElse(0.0);

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
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched enrolled students", null, responses, HttpStatus.OK));
    }

    /**
     * GET /v1/lecturer/classes/{classId}/students/faces
     * Trả về Face Descriptor của từng Sinh viên trong Lớp học phần để Smart Attendance.
     * BRD OBJ-05: Face Recognition hỗ trợ điểm danh.
     */
    @Transactional(readOnly = true)
    @GetMapping("/{classId}/students/faces")
    public ResponseEntity<ApiResponse<List<StudentFaceDTO>>> getEnrolledStudentsFaces(@PathVariable UUID classId) {
        List<ClassEnrollment> enrollments = classEnrollmentRepository.findBySchoolClassId(classId);

        List<StudentFaceDTO> responses = enrollments.stream()
                .filter(enrollment -> enrollment.getStudent() != null)
                .map(enrollment -> {
                    var student = enrollment.getStudent();
                    List<Float> descriptor = null;
                    if (student.getFaceEmbedding() != null) {
                        try {
                            descriptor = objectMapper.readValue(student.getFaceEmbedding(), new TypeReference<List<Float>>() {});
                        } catch (Exception e) {
                            // ignore parsing error
                        }
                    }
                    return StudentFaceDTO.builder()
                            .id(student.getUserId())
                            .fullName(student.getFullName())
                            .faceDescriptor(descriptor)
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched student face descriptors", null, responses, HttpStatus.OK));
    }
}
