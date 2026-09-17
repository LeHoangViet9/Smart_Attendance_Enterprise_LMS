package edufit_com_lms.module.attendance.service.impl;

import edufit_com_lms.module.attendance.dto.request.AttendanceSubmitRequest;
import edufit_com_lms.module.attendance.entity.AttendanceRecord;
import edufit_com_lms.module.attendance.repository.AttendanceRepository;
import edufit_com_lms.module.attendance.service.AttendanceService;
import edufit_com_lms.module.lms.entity.ClassEnrollment;
import edufit_com_lms.module.lms.entity.SchoolClass;
import edufit_com_lms.module.lms.repository.ClassEnrollmentRepository;
import edufit_com_lms.module.lms.repository.SchoolClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void submitAttendance(AttendanceSubmitRequest request) {
        SchoolClass schoolClass = schoolClassRepository.findById(request.getClassId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần"));

        // Lấy danh sách sinh viên từ ClassEnrollment (BRD 8.3: Student enrolled in Class)
        List<ClassEnrollment> enrollments = classEnrollmentRepository.findBySchoolClassId(request.getClassId());

        LocalDateTime now = LocalDateTime.now();

        List<AttendanceRecord> records = enrollments.stream()
                .filter(enrollment -> enrollment.getStudent() != null)
                .map(enrollment -> {
                    boolean isPresent = request.getPresentStudentIds() != null &&
                            request.getPresentStudentIds().contains(enrollment.getStudent().getUserId());

                    return AttendanceRecord.builder()
                            .student(enrollment.getStudent())
                            .schoolClass(schoolClass)
                            .checkInTime(now)
                            .status(isPresent ? "PRESENT" : "ABSENT")
                            .build();
                }).toList();

        attendanceRepository.saveAll(records);
    }

    @Override
    public List<Long> verifyFaces(UUID classId, MultipartFile frame) {
        if (frame == null || frame.isEmpty()) {
            throw new RuntimeException("Frame is empty");
        }

        // Get enrolled students
        List<ClassEnrollment> enrollments = classEnrollmentRepository.findBySchoolClassId(classId);
        List<Map<String, Object>> registeredDescriptors = new ArrayList<>();

        for (ClassEnrollment enrollment : enrollments) {
            if (enrollment.getStudent() != null && enrollment.getStudent().getFaceEmbedding() != null) {
                try {
                    List<Float> descriptor = objectMapper.readValue(
                        enrollment.getStudent().getFaceEmbedding(), 
                        new TypeReference<List<Float>>() {}
                    );
                    Map<String, Object> item = new HashMap<>();
                    item.put("studentId", enrollment.getStudent().getUserId());
                    item.put("descriptor", descriptor);
                    registeredDescriptors.add(item);
                } catch (Exception e) {
                    // Ignore parsing error for single student
                }
            }
        }

        if (registeredDescriptors.isEmpty()) {
            return new ArrayList<>(); // No registered faces
        }

        try {
            String descriptorsJson = objectMapper.writeValueAsString(registeredDescriptors);

            RestTemplate restTemplate = new RestTemplate();
            String aiServiceUrl = "http://localhost:8000/api/v1/ai/verify-face";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(frame.getBytes()) {
                @Override
                public String getFilename() {
                    return "frame.jpg";
                }
            });
            body.add("registered_descriptors", descriptorsJson);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<JsonNode> aiResponse = restTemplate.postForEntity(aiServiceUrl, requestEntity, JsonNode.class);

            if (!aiResponse.getStatusCode().is2xxSuccessful() || aiResponse.getBody() == null) {
                throw new RuntimeException("AI Service connection error");
            }

            JsonNode responseBody = aiResponse.getBody();
            if (responseBody.has("present_student_ids")) {
                return objectMapper.convertValue(
                    responseBody.get("present_student_ids"), 
                    new TypeReference<List<Long>>() {}
                );
            }
            return new ArrayList<>();

        } catch (Exception e) {
            throw new RuntimeException("Error communicating with AI Service: " + e.getMessage());
        }
    }
}
