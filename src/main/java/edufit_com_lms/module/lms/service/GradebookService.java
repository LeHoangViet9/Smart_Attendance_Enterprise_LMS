package edufit_com_lms.module.lms.service;

import edufit_com_lms.common.exception.BadRequestException;
import edufit_com_lms.module.lms.dto.request.UpdateGradeRequest;
import edufit_com_lms.module.lms.dto.response.GradebookResponse;
import edufit_com_lms.module.lms.entity.ClassEnrollment;
import edufit_com_lms.module.lms.entity.Gradebook;
import edufit_com_lms.module.lms.repository.ClassEnrollmentRepository;
import edufit_com_lms.module.lms.repository.GradebookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradebookService {

    private final GradebookRepository gradebookRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;

    @Transactional
    public List<GradebookResponse> getGradebooksByClass(UUID classId) {
        List<ClassEnrollment> enrollments = classEnrollmentRepository.findBySchoolClassId(classId);
        List<Gradebook> gradebooks = new ArrayList<>();
        
        for (ClassEnrollment enrollment : enrollments) {
            Optional<Gradebook> optionalGb = gradebookRepository.findByEnrollmentId(enrollment.getId());
            if (optionalGb.isEmpty()) {
                Gradebook gb = Gradebook.builder()
                        .enrollment(enrollment)
                        .build();
                gradebooks.add(gradebookRepository.save(gb));
            } else {
                gradebooks.add(optionalGb.get());
            }
        }
        
        return gradebooks.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<GradebookResponse> getGradebooksByStudent(Long studentId) {
        List<Gradebook> gradebooks = gradebookRepository.findByStudentId(studentId);
        return gradebooks.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public GradebookResponse updateGrade(UUID gradebookId, UpdateGradeRequest request) {
        Gradebook gradebook = gradebookRepository.findById(gradebookId)
                .orElseThrow(() -> new BadRequestException("Gradebook not found"));

        gradebook.setAttendanceScore(request.getAttendanceScore());
        gradebook.setAssignmentScore(request.getAssignmentScore());
        gradebook.setMidtermScore(request.getMidtermScore());
        gradebook.setFinalScore(request.getFinalScore());
        gradebook.setTeacherComment(request.getTeacherComment());

        calculateAndSetAverageScore(gradebook);

        return toResponse(gradebookRepository.save(gradebook));
    }

    private void calculateAndSetAverageScore(Gradebook gradebook) {
        float attendance = gradebook.getAttendanceScore() != null ? gradebook.getAttendanceScore() : 0;
        float assignment = gradebook.getAssignmentScore() != null ? gradebook.getAssignmentScore() : 0;
        float midterm = gradebook.getMidtermScore() != null ? gradebook.getMidtermScore() : 0;
        float finalScore = gradebook.getFinalScore() != null ? gradebook.getFinalScore() : 0;

        float average = (attendance * 1 + assignment * 1 + midterm * 2 + finalScore * 3) / 7.0f;
        average = (float) (Math.round(average * 100.0) / 100.0);
        gradebook.setAverageScore(average);
    }

    @Transactional(readOnly = true)
    public byte[] exportGradebookToExcel(UUID classId) {
        List<Gradebook> gradebooks = gradebookRepository.findByClassId(classId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bảng Điểm");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Tên Học Sinh", "Email", "Chuyên Cần (x1)", "Bài Tập (x1)", "Giữa Kỳ (x2)", "Cuối Kỳ (x3)", "Điểm Trung Bình", "Nhận Xét"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Gradebook gb : gradebooks) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(gb.getEnrollment().getStudent().getFullName());
                row.createCell(1).setCellValue(gb.getEnrollment().getStudent().getEmail());
                row.createCell(2).setCellValue(gb.getAttendanceScore() != null ? gb.getAttendanceScore() : 0);
                row.createCell(3).setCellValue(gb.getAssignmentScore() != null ? gb.getAssignmentScore() : 0);
                row.createCell(4).setCellValue(gb.getMidtermScore() != null ? gb.getMidtermScore() : 0);
                row.createCell(5).setCellValue(gb.getFinalScore() != null ? gb.getFinalScore() : 0);
                row.createCell(6).setCellValue(gb.getAverageScore() != null ? gb.getAverageScore() : 0);
                row.createCell(7).setCellValue(gb.getTeacherComment() != null ? gb.getTeacherComment() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Lỗi xuất file Excel", e);
            throw new BadRequestException("Lỗi khi xuất file Excel: " + e.getMessage());
        }
    }

    private GradebookResponse toResponse(Gradebook entity) {
        return GradebookResponse.builder()
                .id(entity.getId())
                .enrollmentId(entity.getEnrollment().getId())
                .classId(entity.getEnrollment().getSchoolClass().getId())
                .studentId(entity.getEnrollment().getStudent().getUserId())
                .studentName(entity.getEnrollment().getStudent().getFullName())
                .studentEmail(entity.getEnrollment().getStudent().getEmail())
                .attendanceScore(entity.getAttendanceScore())
                .assignmentScore(entity.getAssignmentScore())
                .midtermScore(entity.getMidtermScore())
                .finalScore(entity.getFinalScore())
                .averageScore(entity.getAverageScore())
                .teacherComment(entity.getTeacherComment())
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null)
                .build();
    }
}
