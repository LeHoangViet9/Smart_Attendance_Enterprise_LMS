package edufit_com_lms.module.lms.service.impl;

import edufit_com_lms.common.exception.AppException;
import edufit_com_lms.module.attendance.repository.AttendanceRepository;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.lms.dto.request.UpdateGradebookRequest;
import edufit_com_lms.module.lms.dto.response.GradebookItemResponse;
import edufit_com_lms.module.lms.entity.Assignment;
import edufit_com_lms.module.lms.entity.ClassEnrollment;
import edufit_com_lms.module.lms.entity.Gradebook;
import edufit_com_lms.module.lms.entity.SchoolClass;
import edufit_com_lms.module.lms.entity.Submission;
import edufit_com_lms.module.lms.repository.AssignmentRepository;
import edufit_com_lms.module.lms.repository.ClassEnrollmentRepository;
import edufit_com_lms.module.lms.repository.GradebookRepository;
import edufit_com_lms.module.lms.repository.SchoolClassRepository;
import edufit_com_lms.module.lms.repository.SubmissionRepository;
import edufit_com_lms.module.lms.service.GradebookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class GradebookServiceImpl implements GradebookService {

    private final SchoolClassRepository schoolClassRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final AttendanceRepository attendanceRepository;
    private final GradebookRepository gradebookRepository;

    private GradebookItemResponse mapToResponse(Gradebook g) {
        User student = g.getEnrollment().getStudent();
        SchoolClass schoolClass = g.getEnrollment().getSchoolClass();
        return GradebookItemResponse.builder()
                .id(g.getId())
                .studentId(student.getUserId())
                .studentName(student.getFullName())
                .studentEmail(student.getEmail())
                .className(schoolClass.getClassName())
                .courseName(schoolClass.getCourse() != null ? schoolClass.getCourse().getTitle() : "")
                .attendanceScore(g.getAttendanceScore())
                .assignmentScore(g.getAssignmentScore())
                .midtermScore(g.getMidtermScore())
                .finalScore(g.getFinalScore())
                .averageScore(g.getAverageScore())
                .teacherComment(g.getTeacherComment())
                .classId(schoolClass.getId())
                .build();
    }

    @Override
    public List<GradebookItemResponse> getGradebookForClass(UUID classId, Long lecturerId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Lớp học không tồn tại"));

        if (!schoolClass.getLecturer().getUserId().equals(lecturerId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền xem sổ điểm của lớp này");
        }

        List<Gradebook> gradebooks = gradebookRepository.findByClassId(classId);
        
        // If empty, auto-sync once to initialize
        if (gradebooks.isEmpty()) {
            return syncGradebookForClass(classId, lecturerId);
        }

        return gradebooks.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<GradebookItemResponse> syncGradebookForClass(UUID classId, Long lecturerId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Lớp học không tồn tại"));

        if (!schoolClass.getLecturer().getUserId().equals(lecturerId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền xem sổ điểm của lớp này");
        }

        long totalAttendanceSessions = attendanceRepository.countDistinctCheckInTimeBySchoolClassId(classId);

        List<Assignment> assignments = assignmentRepository.findByClassId(classId);
        assignments.sort(Comparator.comparing(Assignment::getDueDate));

        List<Assignment> exams = assignments.stream().filter(Assignment::getIsExam).collect(Collectors.toList());
        Assignment midtermExam = null;
        Assignment finalExam = null;
        if (exams.size() >= 2) {
            midtermExam = exams.get(0);
            finalExam = exams.get(exams.size() - 1);
        } else if (exams.size() == 1) {
            finalExam = exams.get(0);
        }

        List<ClassEnrollment> enrollments = classEnrollmentRepository.findBySchoolClassId(classId);
        
        List<UUID> assignmentIds = assignments.stream().map(Assignment::getId).collect(Collectors.toList());
        List<Submission> allSubmissions = assignmentIds.isEmpty() ? new ArrayList<>() : submissionRepository.findByAssignmentIdIn(assignmentIds);
        Map<Long, List<Submission>> submissionsByStudent = allSubmissions.stream()
                .collect(Collectors.groupingBy(Submission::getStudentId));

        // Fix N+1: Fetch all existing gradebooks for this class
        List<Gradebook> existingGradebooks = gradebookRepository.findByClassId(classId);
        Map<UUID, Gradebook> gradebookMap = existingGradebooks.stream()
                .collect(Collectors.toMap(g -> g.getEnrollment().getId(), g -> g));

        // Fix N+1: Fetch all attendance counts grouped by student
        List<Object[]> presentCountsData = attendanceRepository.countPresentAttendanceByClassIdGroupedByStudent(classId);
        Map<Long, Long> presentCountMap = presentCountsData.stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        List<Gradebook> gradebooksToSave = new ArrayList<>();

        for (ClassEnrollment enrollment : enrollments) {
            User student = enrollment.getStudent();
            if (student == null) continue;

            Gradebook gradebook = gradebookMap.getOrDefault(enrollment.getId(), 
                    Gradebook.builder().enrollment(enrollment).build());

            long presentCount = presentCountMap.getOrDefault(student.getUserId(), 0L);
            Double attendanceScore = totalAttendanceSessions == 0 ? 0.0 : (double) presentCount / totalAttendanceSessions * 10.0;

            List<Submission> studentSubs = submissionsByStudent.getOrDefault(student.getUserId(), new ArrayList<>());
            
            Double sumNormalAssignments = 0.0;
            int countNormalAssignments = 0;
            Double midtermScore = 0.0;
            Double finalScore = 0.0;

            for (Assignment a : assignments) {
                Submission sub = studentSubs.stream().filter(s -> s.getAssignmentId().equals(a.getId())).findFirst().orElse(null);
                Double score = 0.0;
                if (sub != null && sub.getScore() != null) {
                    double max = a.getMaxScore() != null && a.getMaxScore() > 0 ? a.getMaxScore() : 10.0;
                    score = (sub.getScore() / max) * 10.0;
                }

                if (midtermExam != null && a.getId().equals(midtermExam.getId())) {
                    midtermScore = score;
                } else if (finalExam != null && a.getId().equals(finalExam.getId())) {
                    finalScore = score;
                } else if (!a.getIsExam()) {
                    sumNormalAssignments += score;
                    countNormalAssignments++;
                }
            }

            Double avgAssignmentScore = countNormalAssignments == 0 ? 0.0 : sumNormalAssignments / countNormalAssignments;

            Double totalScore = (attendanceScore * 1 + avgAssignmentScore * 1 + midtermScore * 3 + finalScore * 5) / 10.0;

            gradebook.setAttendanceScore((float) (Math.round(attendanceScore * 10.0) / 10.0));
            gradebook.setAssignmentScore((float) (Math.round(avgAssignmentScore * 10.0) / 10.0));
            gradebook.setMidtermScore((float) (Math.round(midtermScore * 10.0) / 10.0));
            gradebook.setFinalScore((float) (Math.round(finalScore * 10.0) / 10.0));
            gradebook.setAverageScore((float) (Math.round(totalScore * 10.0) / 10.0));

            gradebooksToSave.add(gradebook);
        }

        gradebookRepository.saveAll(gradebooksToSave);
        return gradebooksToSave.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void updateGradebook(UUID gradebookId, UpdateGradebookRequest request, Long lecturerId) {
        Gradebook gradebook = gradebookRepository.findById(gradebookId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Bản ghi sổ điểm không tồn tại"));

        if (!gradebook.getEnrollment().getSchoolClass().getLecturer().getUserId().equals(lecturerId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền sửa sổ điểm này");
        }

        if (request.getAttendanceScore() != null) gradebook.setAttendanceScore(request.getAttendanceScore());
        if (request.getAssignmentScore() != null) gradebook.setAssignmentScore(request.getAssignmentScore());
        if (request.getMidtermScore() != null) gradebook.setMidtermScore(request.getMidtermScore());
        if (request.getFinalScore() != null) gradebook.setFinalScore(request.getFinalScore());
        if (request.getTeacherComment() != null) gradebook.setTeacherComment(request.getTeacherComment());

        Float att = gradebook.getAttendanceScore() != null ? gradebook.getAttendanceScore() : 0f;
        Float asg = gradebook.getAssignmentScore() != null ? gradebook.getAssignmentScore() : 0f;
        Float mid = gradebook.getMidtermScore() != null ? gradebook.getMidtermScore() : 0f;
        Float fin = gradebook.getFinalScore() != null ? gradebook.getFinalScore() : 0f;
        
        Float avg = (att * 1 + asg * 1 + mid * 3 + fin * 5) / 10.0f;
        gradebook.setAverageScore(Math.round(avg * 10.0f) / 10.0f);

        gradebookRepository.save(gradebook);
    }

    @Override
    public List<GradebookItemResponse> getMyGrades(Long studentId) {
        List<Gradebook> gradebooks = gradebookRepository.findByStudentId(studentId);
        return gradebooks.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
}
