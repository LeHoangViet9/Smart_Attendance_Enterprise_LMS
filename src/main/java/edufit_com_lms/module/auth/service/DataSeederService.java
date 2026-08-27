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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void seedData() {
        log.info("Starting Data Seeding...");

        // 1. Create Majors
        Major aiMajor = majorRepository.findByCode("AI").orElseGet(() -> majorRepository
                .save(Major.builder().code("AI").name("Artificial Intelligence").description("AI Major").build()));
        Major javaMajor = majorRepository.findByCode("SE").orElseGet(() -> majorRepository
                .save(Major.builder().code("SE").name("Software Engineering").description("SE Major").build()));

        // 2. Create Classes
        SchoolClass classAI = schoolClassRepository.findByClassName("21AI").orElseGet(() -> schoolClassRepository
                .save(SchoolClass.builder().className("21AI").major(aiMajor).entryYear(2021).build()));
        SchoolClass classSE = schoolClassRepository.findByClassName("21SE").orElseGet(() -> schoolClassRepository
                .save(SchoolClass.builder().className("21SE").major(javaMajor).entryYear(2021).build()));

        SchoolClass[] classes = { classAI, classSE };
        Random rand = new Random();

        // 3. Process Users (Fill nulls and profiles)
        List<User> users = userRepository.findAll();
        for (User user : users) {
            boolean updated = false;

            if (user.getPhone() == null || user.getPhone().isEmpty()) {
                user.setPhone("09" + (10000000 + rand.nextInt(90000000)));
                updated = true;
            }
            if (user.getAddress() == null || user.getAddress().isEmpty()) {
                user.setAddress("Hanoi, Vietnam");
                updated = true;
            }

            if (updated) {
                userRepository.save(user);
            }

            // Student Profiles
            if (user.getRole() == Role.STUDENT) {
                if (user.getStudentProfile() == null) {
                    StudentProfile sp = StudentProfile.builder()
                            .user(user)
                            .parentPhone("098" + (1000000 + rand.nextInt(9000000)))
                            .schoolClass(classes[rand.nextInt(classes.length)])
                            .enrollmentYear(2021)
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

        // 4. Update Courses to have Majors
        List<Courses> courses = courseRepository.findAll();
        for (Courses c : courses) {
            if (c.getMajor() == null) {
                // Randomly assign AI or SE to courses for the mock
                c.setMajor(rand.nextBoolean() ? aiMajor : javaMajor);
                courseRepository.save(c);
            }
        }

        // 5. Enroll Students in Courses
        // Give each student 2-3 courses.
        List<User> students = userRepository.findAll().stream().filter(u -> u.getRole() == Role.STUDENT).toList();

        if (!courses.isEmpty() && !students.isEmpty()) {
            for (User student : students) {
                // Ensure enrolled in 2 random courses
                int enrollmentsToCreate = Math.min(2, courses.size());
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
