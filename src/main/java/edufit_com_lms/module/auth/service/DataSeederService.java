package edufit_com_lms.module.auth.service;

import edufit_com_lms.module.auth.entity.LecturerProfile;
import edufit_com_lms.module.auth.entity.Role;
import edufit_com_lms.module.auth.entity.StudentProfile;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.auth.repository.LecturerProfileRepository;
import edufit_com_lms.module.auth.repository.StudentProfileRepository;
import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.lms.entity.CourseEnrollment;
import edufit_com_lms.module.lms.entity.Courses;
import edufit_com_lms.module.lms.entity.Major;
import edufit_com_lms.module.lms.entity.SchoolClass;
import edufit_com_lms.module.lms.repository.CourseEnrollmentRepository;
import edufit_com_lms.module.lms.repository.MajorRepository;
import edufit_com_lms.module.lms.repository.SchoolClassRepository;
import edufit_com_lms.module.lms.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSeederService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final LecturerProfileRepository lecturerProfileRepository;
    private final MajorRepository majorRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void seedData() {
        log.info("Starting Data Seeding (Large Volume Mock Data)...");

        // 1. Create Majors
        Major aiMajor = majorRepository.findByCode("AI").orElseGet(() -> majorRepository
                .save(Major.builder().code("AI").name("Artificial Intelligence").description("AI Major").build()));
        Major javaMajor = majorRepository.findByCode("SE").orElseGet(() -> majorRepository
                .save(Major.builder().code("SE").name("Software Engineering").description("SE Major").build()));

        // 2. Create Classes
        SchoolClass[] classes = {
                schoolClassRepository.findByClassName("21AI")
                        .orElseGet(() -> schoolClassRepository
                                .save(SchoolClass.builder().className("21AI").major(aiMajor).entryYear(2021).build())),
                schoolClassRepository.findByClassName("21SE")
                        .orElseGet(() -> schoolClassRepository.save(
                                SchoolClass.builder().className("21SE").major(javaMajor).entryYear(2021).build())),
                schoolClassRepository.findByClassName("22AI")
                        .orElseGet(() -> schoolClassRepository
                                .save(SchoolClass.builder().className("22AI").major(aiMajor).entryYear(2022).build())),
                schoolClassRepository.findByClassName("22SE")
                        .orElseGet(() -> schoolClassRepository
                                .save(SchoolClass.builder().className("22SE").major(javaMajor).entryYear(2022).build()))
        };

        Random rand = new Random();
        String defaultPassword = passwordEncoder.encode("123456");

        // 3. Generate 100 students if non-existent
        long studentCount = userRepository.findAll().stream().filter(u -> u.getRole() == Role.STUDENT).count();
        if (studentCount < 100) {
            log.info("Generating Mock Students...");
            List<User> newUsers = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                String code = String.format("SV%03d", i + 1); // SV002 to SV101
                if (!userRepository.existsByCode(code)) {
                    User student = User.builder()
                            .email("student" + i + "@edu.vn")
                            .password(defaultPassword)
                            .fullName("Student Name " + i)
                            .phone("09" + (10000000 + rand.nextInt(90000000)))
                            .code(code)
                            .role(Role.STUDENT)
                            .address("Dormitory " + (char) ('A' + rand.nextInt(5)))
                            .isActive(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    newUsers.add(student);
                }
            }
            if (!newUsers.isEmpty()) {
                userRepository.saveAll(newUsers);
            }
        }

        // 3.5 Generate 4 Lecturers if non-existent
        long lecturerCount = userRepository.findAll().stream().filter(u -> u.getRole() == Role.LECTURER).count();
        if (lecturerCount < 4) {
            log.info("Generating Mock Lecturers...");
            List<User> newLecturers = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                String code = String.format("GV%03d", i);
                if (!userRepository.existsByCode(code)) {
                    User lecturer = User.builder()
                            .email("lecturer" + i + "@edu.vn")
                            .password(defaultPassword)
                            .fullName("Giảng viên " + i)
                            .phone("09" + (90000000 + rand.nextInt(9000000)))
                            .code(code)
                            .role(Role.LECTURER)
                            .isActive(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    newLecturers.add(lecturer);
                }
            }
            if (!newLecturers.isEmpty()) {
                userRepository.saveAll(newLecturers);
            }
        }

        // 4. Update Student Profiles to meet minimum threshold of 20 per class
        List<User> users = userRepository.findAll();
        for (User user : users) {
            // Student Profiles
            if (user.getRole() == Role.STUDENT) {
                if (user.getStudentProfile() == null) {
                    StudentProfile sp = StudentProfile.builder()
                            .user(user)
                            .parentPhone("098" + (1000000 + rand.nextInt(9000000)))
                            // Distribute students evenly among classes (roughly >= 20 each)
                            .schoolClass(classes[rand.nextInt(classes.length)])
                            .enrollmentYear(2021 + rand.nextInt(2))
                            .build();
                    studentProfileRepository.save(sp);
                } else if (user.getStudentProfile().getSchoolClass() == null) {
                    StudentProfile sp = user.getStudentProfile();
                    sp.setSchoolClass(classes[rand.nextInt(classes.length)]);
                    studentProfileRepository.save(sp);
                }
            }

            // Lecturer Profiles
            if (user.getRole() == Role.LECTURER) {
                if (user.getLecturerProfile() == null) {
                    LecturerProfile lp = LecturerProfile.builder()
                            .user(user)
                            .degree("Ph.D")
                            .major(rand.nextBoolean() ? "AI" : "Software Engineering")
                            .department("IT Faculty")
                            .build();
                    lecturerProfileRepository.save(lp);
                }
            }
        }

        // 5. Generate Courses if none exist
        if (courseRepository.count() == 0) {
            log.info("Generating Mock Courses...");
            String[] courseNames = { "Data Structures", "Algorithms", "AI Basics", "Web Development",
                    "Database Systems" };
            for (int i = 0; i < courseNames.length; i++) {
                Courses course = Courses.builder()
                        .title(courseNames[i])
                        .description("Fundamental course for " + courseNames[i])
                        .major(i % 2 == 0 ? aiMajor : javaMajor)
                        .isPublished(true)
                        .build();
                courseRepository.save(course);
            }
        }

        // 6. Enroll Students in Courses
        List<Courses> courses = courseRepository.findAll();
        List<User> students = userRepository.findAll().stream().filter(u -> u.getRole() == Role.STUDENT).toList();

        if (!courses.isEmpty() && !students.isEmpty()) {
            for (User student : students) {
                int enrollmentsToCreate = Math.min(2, courses.size()); // Each student gets 2 courses
                for (int i = 0; i < enrollmentsToCreate; i++) {
                    Courses c = courses.get(rand.nextInt(courses.size()));
                    if (!enrollmentRepository.existsByCourseIdAndStudentUserId(c.getId(), student.getUserId())) {
                        enrollmentRepository.save(CourseEnrollment.builder()
                                .course(c)
                                .student(student)
                                .build());
                    }
                }
            }
        }

        log.info("Data Seeding Completed!");
    }
}
