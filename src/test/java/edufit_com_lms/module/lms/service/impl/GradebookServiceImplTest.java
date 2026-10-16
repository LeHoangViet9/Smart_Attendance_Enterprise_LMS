package edufit_com_lms.module.lms.service.impl;

import edufit_com_lms.common.exception.AppException;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.attendance.repository.AttendanceRepository;
import edufit_com_lms.module.lms.entity.*;
import edufit_com_lms.module.lms.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradebookServiceImplTest {
    @Mock SchoolClassRepository schoolClassRepository;
    @Mock ClassEnrollmentRepository classEnrollmentRepository;
    @Mock AssignmentRepository assignmentRepository;
    @Mock SubmissionRepository submissionRepository;
    @Mock AttendanceRepository attendanceRepository;
    @Mock GradebookRepository gradebookRepository;
    @InjectMocks GradebookServiceImpl service;
    final UUID classId = UUID.randomUUID();
    SchoolClass schoolClass;

    @BeforeEach void setup() {
        schoolClass = SchoolClass.builder().id(classId).className("Test")
                .lecturer(User.builder().userId(20L).build()).build();
        when(schoolClassRepository.findById(classId)).thenReturn(Optional.of(schoolClass));
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }
    void login(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "unused",
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
    void existingGrades() {
        User student = User.builder().userId(30L).fullName("Student").build();
        ClassEnrollment enrollment = ClassEnrollment.builder().schoolClass(schoolClass).student(student).build();
        when(gradebookRepository.findByClassId(classId)).thenReturn(
                List.of(Gradebook.builder().id(UUID.randomUUID()).enrollment(enrollment).build()));
    }
    @Test void adminCanReadAnotherLecturersClass() {
        login("ADMIN"); existingGrades();
        assertEquals(1, service.getGradebookForClass(classId, 1L).size());
    }
    @Test void assignedLecturerCanRead() {
        login("LECTURER"); existingGrades();
        assertEquals(1, service.getGradebookForClass(classId, 20L).size());
    }
    @Test void otherLecturerCannotRead() {
        login("LECTURER");
        assertEquals(403, assertThrows(AppException.class,
                () -> service.getGradebookForClass(classId, 21L)).getStatus().value());
        verifyNoInteractions(gradebookRepository);
    }
    @Test void adminCanInitializeUnassignedClass() {
        login("ADMIN"); schoolClass.setLecturer(null);
        when(gradebookRepository.findByClassId(classId)).thenReturn(List.of());
        when(assignmentRepository.findByClasses_Id(classId)).thenReturn(new ArrayList<>());
        when(classEnrollmentRepository.findBySchoolClassId(classId)).thenReturn(List.of());
        assertTrue(service.getGradebookForClass(classId, 1L).isEmpty());
        verify(gradebookRepository).saveAll(List.of());
    }
}
