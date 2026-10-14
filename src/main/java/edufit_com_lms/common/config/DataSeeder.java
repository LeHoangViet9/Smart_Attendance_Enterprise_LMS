package edufit_com_lms.common.config;

import edufit_com_lms.module.auth.entity.Role;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.quiz.entity.Question;
import edufit_com_lms.module.quiz.entity.QuestionOption;
import edufit_com_lms.module.quiz.entity.QuestionType;
import edufit_com_lms.module.quiz.entity.Quiz;
import edufit_com_lms.module.quiz.repository.QuestionOptionRepository;
import edufit_com_lms.module.quiz.repository.QuestionRepository;
import edufit_com_lms.module.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final QuizRepository quizRepository;
        private final QuestionRepository questionRepository;
        private final QuestionOptionRepository questionOptionRepository;

        @Override
        @Transactional
        public void run(String... args) {
                if (!userRepository.existsByEmail("admin@edu.vn")) {
                        log.info("Starting sample data seeding...");

                        User admin = User.builder()
                                        .email("admin@edu.vn")
                                        .password(passwordEncoder.encode("123456"))
                                        .fullName("Admin Quan Tri")
                                        .phone("0988888888")
                                        .code("AD001")
                                        .role(Role.ADMIN)
                                        .address("Phong Hanh Chinh")
                                        .isActive(true)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        userRepository.save(admin);
                }

                if (!userRepository.existsByEmail("sinhvien@edu.vn")) {
                        User student = User.builder()
                                        .email("sinhvien@edu.vn")
                                        .password(passwordEncoder.encode("123456"))
                                        .fullName("Nguyen Khac Phuc")
                                        .phone("0912345678")
                                        .code("SV001")
                                        .role(Role.STUDENT)
                                        .address("Ky tuc xa A")
                                        .isActive(true)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        userRepository.save(student);
                }

                log.info("Sample data seeding completed. Default password: 123456");

                long quizCount = quizRepository.count();
                if (quizCount == 0) {
                        log.info("Starting sample quiz seeding...");

                        Quiz quiz = Quiz.builder()
                                        .title("Java Backend Entry Quiz")
                                        .description("Auto-generated quiz for basic Spring Boot and Java Core knowledge.")
                                        .timeLimitMinutes(15)
                                        .startTime(LocalDateTime.now().minusDays(1))
                                        .endTime(LocalDateTime.now().plusMonths(1))
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        quiz = quizRepository.save(quiz);

                        Question q1 = Question.builder()
                                        .quiz(quiz)
                                        .content("Which Spring Boot component is used for auto-configuration?")
                                        .questionType(QuestionType.SINGLE_CHOICE)
                                        .points(5.0)
                                        .build();
                        q1 = questionRepository.save(q1);

                        questionOptionRepository.save(QuestionOption.builder().question(q1).content("@EnableAutoConfiguration").isCorrect(true).build());
                        questionOptionRepository.save(QuestionOption.builder().question(q1).content("@SpringBootApplication").isCorrect(false).build());
                        questionOptionRepository.save(QuestionOption.builder().question(q1).content("@ComponentScan").isCorrect(false).build());
                        questionOptionRepository.save(QuestionOption.builder().question(q1).content("@Configuration").isCorrect(false).build());

                        Question q2 = Question.builder()
                                        .quiz(quiz)
                                        .content("Is Java a purely object-oriented programming language?")
                                        .questionType(QuestionType.TRUE_FALSE)
                                        .points(5.0)
                                        .build();
                        q2 = questionRepository.save(q2);

                        questionOptionRepository.save(QuestionOption.builder().question(q2).content("True").isCorrect(false).build());
                        questionOptionRepository.save(QuestionOption.builder().question(q2).content("False, because Java still supports primitive types such as int and char.").isCorrect(true).build());

                        Quiz quiz2 = Quiz.builder()
                                        .title("React JS Mastery")
                                        .description("Quiz for evaluating component and hook skills in React.")
                                        .timeLimitMinutes(30)
                                        .startTime(LocalDateTime.now().minusDays(5))
                                        .endTime(LocalDateTime.now().plusMonths(2))
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        quiz2 = quizRepository.save(quiz2);

                        Question q3 = Question.builder()
                                        .quiz(quiz2)
                                        .content("Which hook is used to manage side effects in a functional component?")
                                        .questionType(QuestionType.SINGLE_CHOICE)
                                        .points(5.0)
                                        .build();
                        q3 = questionRepository.save(q3);
                        questionOptionRepository.save(QuestionOption.builder().question(q3).content("useState").isCorrect(false).build());
                        questionOptionRepository.save(QuestionOption.builder().question(q3).content("useEffect").isCorrect(true).build());
                        questionOptionRepository.save(QuestionOption.builder().question(q3).content("useContext").isCorrect(false).build());

                        Question q4 = Question.builder()
                                        .quiz(quiz2)
                                        .content("Can Redux state be mutated directly?")
                                        .questionType(QuestionType.TRUE_FALSE)
                                        .points(5.0)
                                        .build();
                        q4 = questionRepository.save(q4);
                        questionOptionRepository.save(QuestionOption.builder().question(q4).content("True, direct assignment is acceptable.").isCorrect(false).build());
                        questionOptionRepository.save(QuestionOption.builder().question(q4).content("False, Redux state should be immutable.").isCorrect(true).build());

                        Quiz quiz3 = Quiz.builder()
                                        .title("Database and SQL Performance")
                                        .description("Quiz about query optimization and indexing in relational databases.")
                                        .timeLimitMinutes(45)
                                        .startTime(LocalDateTime.now().minusDays(10))
                                        .endTime(LocalDateTime.now().plusMonths(3))
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        quiz3 = quizRepository.save(quiz3);

                        Question q5 = Question.builder()
                                        .quiz(quiz3)
                                        .content("Which SQL command removes all rows from a table quickly and releases storage more efficiently?")
                                        .questionType(QuestionType.SINGLE_CHOICE)
                                        .points(5.0)
                                        .build();
                        q5 = questionRepository.save(q5);
                        questionOptionRepository.save(QuestionOption.builder().question(q5).content("DELETE FROM table").isCorrect(false).build());
                        questionOptionRepository.save(QuestionOption.builder().question(q5).content("TRUNCATE TABLE table").isCorrect(true).build());

                        Question q6 = Question.builder()
                                        .quiz(quiz3)
                                        .content("Which query types can use a B-Tree index efficiently? Select multiple answers.")
                                        .questionType(QuestionType.MULTIPLE_CHOICE)
                                        .points(5.0)
                                        .build();
                        q6 = questionRepository.save(q6);
                        questionOptionRepository.save(QuestionOption.builder().question(q6).content("Exact match search (=)").isCorrect(true).build());
                        questionOptionRepository.save(QuestionOption.builder().question(q6).content("Range search (<, >, BETWEEN)").isCorrect(true).build());
                        questionOptionRepository.save(QuestionOption.builder().question(q6).content("Contains search (LIKE '%abc%')").isCorrect(false).build());

                        Question q7 = Question.builder()
                                        .quiz(quiz3)
                                        .content("Which SQL keyword joins two tables and returns only matching rows from both sides?")
                                        .questionType(QuestionType.FILL_BLANK)
                                        .points(5.0)
                                        .build();
                        q7 = questionRepository.save(q7);
                        questionOptionRepository.save(QuestionOption.builder().question(q7).content("INNER JOIN").isCorrect(true).build());
                        questionOptionRepository.save(QuestionOption.builder().question(q7).content("JOIN").isCorrect(true).build());

                        log.info("Sample quiz seeding completed.");
                }

                if (quizCount <= 3) {
                        for (int i = 4; i <= 15; i++) {
                                Quiz extraQuiz = Quiz.builder()
                                                .title("General Knowledge Quiz " + i)
                                                .description("This is an auto-generated mock quiz for testing purposes.")
                                                .timeLimitMinutes(15 + (i * 5))
                                                .startTime(LocalDateTime.now().minusDays(i))
                                                .endTime(LocalDateTime.now().plusMonths(3))
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build();
                                extraQuiz = quizRepository.save(extraQuiz);

                                Question eq1 = Question.builder()
                                                .quiz(extraQuiz)
                                                .content("Mock Question 1 for Quiz " + i)
                                                .questionType(QuestionType.TRUE_FALSE)
                                                .points(10.0)
                                                .build();
                                eq1 = questionRepository.save(eq1);
                                questionOptionRepository.save(QuestionOption.builder().question(eq1).content("True").isCorrect(true).build());
                                questionOptionRepository.save(QuestionOption.builder().question(eq1).content("False").isCorrect(false).build());
                        }
                }
        }
}
