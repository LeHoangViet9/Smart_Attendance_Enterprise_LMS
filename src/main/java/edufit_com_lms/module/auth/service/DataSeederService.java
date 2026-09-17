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
import edufit_com_lms.module.lms.entity.Lesson;
import edufit_com_lms.module.lms.repository.LessionRepository;
import edufit_com_lms.module.lms.entity.ClassEnrollment;
import edufit_com_lms.module.lms.repository.ClassEnrollmentRepository;
import edufit_com_lms.module.lms.entity.Assignment;
import edufit_com_lms.module.lms.repository.AssignmentRepository;
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
    private final LessionRepository lessionRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final AssignmentRepository assignmentRepository;

    @Transactional
    public void seedData() {
        log.info("Starting Data Seeding (Large Volume Mock Data)...");

        // 1. Create Majors
        Major aiMajor = majorRepository.findByCode("AI").orElseGet(() -> majorRepository
                .save(Major.builder().code("AI").name("Artificial Intelligence").description("AI Major").build()));
        Major javaMajor = majorRepository.findByCode("SE").orElseGet(() -> majorRepository
                .save(Major.builder().code("SE").name("Software Engineering").description("SE Major").build()));
        Major mktMajor = majorRepository.findByCode("MKT").orElseGet(() -> majorRepository
                .save(Major.builder().code("MKT").name("Digital Marketing").description("Marketing Major").build()));
        Major gdMajor = majorRepository.findByCode("GD").orElseGet(() -> majorRepository
                .save(Major.builder().code("GD").name("Graphic Design").description("Design Major").build()));

        // 2. Create Classes
        SchoolClass[] classes = {
                schoolClassRepository.findByClassName("21AI").orElseGet(() -> schoolClassRepository
                        .save(SchoolClass.builder().className("21AI").major(aiMajor).entryYear(2021).build())),
                schoolClassRepository.findByClassName("21SE").orElseGet(() -> schoolClassRepository
                        .save(SchoolClass.builder().className("21SE").major(javaMajor).entryYear(2021).build())),
                schoolClassRepository.findByClassName("22AI").orElseGet(() -> schoolClassRepository
                        .save(SchoolClass.builder().className("22AI").major(aiMajor).entryYear(2022).build())),
                schoolClassRepository.findByClassName("22SE").orElseGet(() -> schoolClassRepository
                        .save(SchoolClass.builder().className("22SE").major(javaMajor).entryYear(2022).build())),
                schoolClassRepository.findByClassName("21MKT").orElseGet(() -> schoolClassRepository
                        .save(SchoolClass.builder().className("21MKT").major(mktMajor).entryYear(2021).build())),
                schoolClassRepository.findByClassName("22MKT").orElseGet(() -> schoolClassRepository
                        .save(SchoolClass.builder().className("22MKT").major(mktMajor).entryYear(2022).build())),
                schoolClassRepository.findByClassName("21GD").orElseGet(() -> schoolClassRepository
                        .save(SchoolClass.builder().className("21GD").major(gdMajor).entryYear(2021).build())),
                schoolClassRepository.findByClassName("22GD").orElseGet(() -> schoolClassRepository
                        .save(SchoolClass.builder().className("22GD").major(gdMajor).entryYear(2022).build()))
        };

        Random rand = new Random();
        String defaultPassword = passwordEncoder.encode("123456");

        // 3. Generate 200 students if non-existent
        long studentCount = userRepository.findAll().stream().filter(u -> u.getRole() == Role.STUDENT).count();
        if (studentCount < 200) {
            log.info("Generating Mock Students...");
            List<User> newUsers = new ArrayList<>();
            for (int i = 1; i <= 200; i++) {
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

        // 3.5 Generate 10 Lecturers if non-existent
        long lecturerCount = userRepository.findAll().stream().filter(u -> u.getRole() == Role.LECTURER).count();
        if (lecturerCount < 10) {
            log.info("Generating Mock Lecturers...");
            List<User> newLecturers = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
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
        List<Major> majors = majorRepository.findAll();
        int classIndex = 0;
        for (User user : users) {
            // Student Profiles
            if (user.getRole() == Role.STUDENT) {
                if (user.getStudentProfile() == null) {
                    SchoolClass assignedClass = classes[classIndex % classes.length];
                    Major assignedMajor = assignedClass.getMajor();
                    classIndex++;

                    StudentProfile sp = StudentProfile.builder()
                            .user(user)
                            .parentPhone("098" + (1000000 + rand.nextInt(9000000)))
                            .schoolClass(assignedClass)
                            .enrollmentYear(assignedClass.getEntryYear())
                            .major(assignedMajor)
                            .build();
                    studentProfileRepository.save(sp);
                } else {
                    StudentProfile sp = user.getStudentProfile();
                    boolean needsUpdate = false;
                    if (sp.getSchoolClass() == null) {
                        SchoolClass assignedClass = classes[classIndex % classes.length];
                        sp.setSchoolClass(assignedClass);
                        if (sp.getMajor() == null) {
                            sp.setMajor(assignedClass.getMajor());
                        }
                        if (sp.getEnrollmentYear() == null) {
                            sp.setEnrollmentYear(assignedClass.getEntryYear());
                        }
                        classIndex++;
                        needsUpdate = true;
                    }
                    if (needsUpdate) {
                        studentProfileRepository.save(sp);
                    }
                }
            }

            // Lecturer Profiles
            if (user.getRole() == Role.LECTURER) {
                Major assignedMajor = majors.isEmpty() ? null : majors.get(rand.nextInt(majors.size()));
                if (user.getLecturerProfile() == null) {
                    LecturerProfile lp = LecturerProfile.builder()
                            .user(user)
                            .degree("Ph.D")
                            .major(assignedMajor)
                            .department("IT Faculty")
                            .build();
                    lecturerProfileRepository.save(lp);
                } else {
                    LecturerProfile lp = user.getLecturerProfile();
                    if (lp.getMajor() == null) {
                        lp.setMajor(assignedMajor);
                        lecturerProfileRepository.save(lp);
                    }
                }
            }
        }

        // 5. Generate Courses if none exist
        if (courseRepository.count() == 0) {
            log.info("Generating Mock Courses...");
            String[] courseNames = { 
                    "Data Structures", "Algorithms", "AI Basics", "Web Development", "Database Systems",
                    "Machine Learning", "Deep Learning", "Software Testing",
                    "Digital Marketing Strategy", "Social Media Marketing", "Content Marketing",
                    "UI/UX Design", "Typography", "Color Theory", "3D Modeling"
            };
            for (int i = 0; i < courseNames.length; i++) {
                Major assignedMajor = javaMajor; // default
                if (i >= 5 && i <= 7) assignedMajor = aiMajor;
                else if (i >= 8 && i <= 10) assignedMajor = mktMajor;
                else if (i >= 11 && i <= 14) assignedMajor = gdMajor;
                
                Courses course = Courses.builder()
                        .title(courseNames[i])
                        .description("Fundamental course for " + courseNames[i])
                        .major(assignedMajor)
                        .isPublished(true)
                        .build();
                course = courseRepository.save(course);

                // Create 3 Lessions per Course
                for (int j = 1; j <= 3; j++) {
                    Lesson lession = Lesson.builder()
                            .courseId(course.getId())
                            .title("Lesson " + j + ": " + course.getTitle() + " Fundamentals")
                            .content("This is the detailed content for " + course.getTitle() + " Lesson " + j
                                    + ". Learn the basic principles and practically apply them in your major.")
                            .videoUrl("https://example.com/video-" + j + ".mp4")
                            .documentUrl("https://example.com/doc-" + j + ".pdf")
                            .orderIndex(j)
                            .isPublished(true)
                            .build();
                    lessionRepository.save(lession);
                }
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

        // 7. Enroll Students in Classes (ClassEnrollment)
        List<StudentProfile> studentProfiles = studentProfileRepository.findAll();
        if (!studentProfiles.isEmpty()) {
            for (StudentProfile profile : studentProfiles) {
                if (profile.getSchoolClass() != null && profile.getUser() != null) {
                    SchoolClass adminClass = profile.getSchoolClass();
                    User student = profile.getUser();
                    if (!classEnrollmentRepository.existsBySchoolClassIdAndStudentUserId(adminClass.getId(), student.getUserId())) {
                        classEnrollmentRepository.save(ClassEnrollment.builder()
                                .schoolClass(adminClass)
                                .student(student)
                                .build());
                    }
                }
            }
        }

        // 8. Generate Assignments
        if (assignmentRepository.count() == 0) {
            log.info("Generating Mock Assignments...");
            List<SchoolClass> allAdminClasses = schoolClassRepository.findAll();
            for (SchoolClass sc : allAdminClasses) {
                for (int i = 1; i <= 3; i++) {
                    Assignment assignment = Assignment.builder()
                            .classId(sc.getId())
                            .title("Assignment " + i + " for " + sc.getClassName())
                            .description("Please complete the exercises for Chapter " + i + ". Upload your work as a PDF.")
                            .dueDate(LocalDateTime.now().plusDays(i * 7))
                            .maxScore(100.0)
                            .isExam(i == 3) // The third assignment acts as an exam
                            .isPublished(true)
                            .build();
                    assignmentRepository.save(assignment);
                }
            }
        }

        log.info("Data Seeding Completed!");
    }
}
