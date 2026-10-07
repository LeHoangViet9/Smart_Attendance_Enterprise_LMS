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
        public void run(String... args) throws Exception {
                if (!userRepository.existsByEmail("admin@edu.vn")) {
                        log.info("Báº¯t Ä‘áº§u khá»Ÿi táº¡o dá»¯ liá»‡u máº«u (Seeder)...");

                        // 1. TÃ i khoáº£n Admin
                        User admin = User.builder()
                                        .email("admin@edu.vn")
                                        .password(passwordEncoder.encode("123456"))
                                        .fullName("Admin Quáº£n Trá»‹")
                                        .phone("0988888888") // ThÃªm sá»‘ Ä‘iá»‡n thoáº¡i
                                        .code("AD001")
                                        .role(Role.ADMIN)
                                        .address("PhÃ²ng HÃ nh ChÃ­nh")
                                        .isActive(true)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        userRepository.save(admin);
                }

                if (!userRepository.existsByEmail("sinhvien@edu.vn")) {
                        // 2. TÃ i khoáº£n Sinh ViÃªn (Ä‘á»ƒ test Face Onboarding)
                        User student = User.builder()
                                        .email("sinhvien@edu.vn")
                                        .password(passwordEncoder.encode("123456"))
                                        .fullName("Nguyá»…n Kháº¯c Phá»¥c")
                                        .phone("0912345678") // ThÃªm sá»‘ Ä‘iá»‡n thoáº¡i
                                        .code("SV001")
                                        .role(Role.STUDENT)
                                        .address("KÃ½ tÃºc xÃ¡ A")
                                        .isActive(true)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        userRepository.save(student);
                }
                
                log.info("ÄÃ£ táº¡o xong dá»¯ liá»‡u máº«u! Máº­t kháº©u chung lÃ : 123456");

                if (quizRepository.count() == 0) {
                        log.info("Báº¯t Ä‘áº§u khá»Ÿi táº¡o dá»¯ liá»‡u Ä‘á» thi máº«u (Quiz Seeder)...");

                        Quiz quiz = Quiz.builder()
                                        .title("BÃ i kiá»ƒm tra JAVA Backend Ä‘áº§u vÃ o")
                                        .description(
                                                        "BÃ i kiá»ƒm tra tá»± Ä‘á»™ng Ä‘Ã¡nh giÃ¡ kiáº¿n thá»©c cÆ¡ báº£n vá» Spring Boot vÃ  Java Core, dÃ nh cho khoÃ¡ K18.")
                                        .timeLimitMinutes(15)
                                        .startTime(LocalDateTime.now().minusDays(1))
                                        .endTime(LocalDateTime.now().plusMonths(1))
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        quiz = quizRepository.save(quiz);

                        Question q1 = Question.builder()
                                        .quiz(quiz)
                                        .content("ThÃ nh pháº§n nÃ o cá»§a Spring Boot Ä‘Æ°á»£c dÃ¹ng Ä‘á»ƒ cáº¥u hÃ¬nh tá»± Ä‘á»™ng (Auto-configuration)?")
                                        .questionType(QuestionType.SINGLE_CHOICE)
                                        .points(5.0)
                                        .build();
                        q1 = questionRepository.save(q1);

                        questionOptionRepository.save(
                                        QuestionOption.builder().question(q1).content("@EnableAutoConfiguration")
                                                        .isCorrect(true).build());
                        questionOptionRepository.save(
                                        QuestionOption.builder().question(q1).content("@SpringBootApplication")
                                                        .isCorrect(false).build());
                        questionOptionRepository
                                        .save(QuestionOption.builder().question(q1).content("@ComponentScan")
                                                        .isCorrect(false).build());
                        questionOptionRepository
                                        .save(QuestionOption.builder().question(q1).content("@Configuration")
                                                        .isCorrect(false).build());

                        Question q2 = Question.builder()
                                        .quiz(quiz)
                                        .content("Java lÃ  ngÃ´n ngá»¯ láº­p trÃ¬nh thuáº§n hÆ°á»›ng Ä‘á»‘i tÆ°á»£ng 100%?")
                                        .questionType(QuestionType.TRUE_FALSE)
                                        .points(5.0)
                                        .build();
                        q2 = questionRepository.save(q2);

                        questionOptionRepository
                                        .save(QuestionOption.builder().question(q2).content("ÄÃºng").isCorrect(false)
                                                        .build());
                        questionOptionRepository.save(QuestionOption.builder().question(q2)
                                        .content("Sai (VÃ¬ váº«n há»— trá»£ cÃ¡c kiá»ƒu nguyÃªn thuá»· nhÆ° int, char)")
                                        .isCorrect(true).build());

                        // --- QUIZ 2: React JS ---
                        Quiz quiz2 = Quiz.builder()
                                        .title("React JS Mastery")
                                        .description("BÃ i kiá»ƒm tra Ä‘Ã¡nh giÃ¡ ká»¹ nÄƒng xÃ¢y dá»±ng Component vÃ  Hooks trong React 18.")
                                        .timeLimitMinutes(30)
                                        .startTime(LocalDateTime.now().minusDays(5))
                                        .endTime(LocalDateTime.now().plusMonths(2))
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        quiz2 = quizRepository.save(quiz2);

                        Question q3 = Question.builder()
                                        .quiz(quiz2)
                                        .content("Hook nÃ o Ä‘Æ°á»£c sá»­ dá»¥ng Ä‘á»ƒ quáº£n lÃ½ Side Effect trong Functional Component?")
                                        .questionType(QuestionType.SINGLE_CHOICE)
                                        .points(5.0)
                                        .build();
                        q3 = questionRepository.save(q3);
                        questionOptionRepository
                                        .save(QuestionOption.builder().question(q3).content("useState").isCorrect(false)
                                                        .build());
                        questionOptionRepository
                                        .save(QuestionOption.builder().question(q3).content("useEffect").isCorrect(true)
                                                        .build());
                        questionOptionRepository
                                        .save(QuestionOption.builder().question(q3).content("useContext")
                                                        .isCorrect(false).build());

                        Question q4 = Question.builder()
                                        .quiz(quiz2)
                                        .content("Trong Redux, tráº¡ng thÃ¡i (State) cÃ³ thá»ƒ bá»‹ thay Ä‘á»•i (mutate) trá»±c tiáº¿p khÃ´ng?")
                                        .questionType(QuestionType.TRUE_FALSE)
                                        .points(5.0)
                                        .build();
                        q4 = questionRepository.save(q4);
                        questionOptionRepository.save(
                                        QuestionOption.builder().question(q4).content("ÄÃºng (CÃ³ thá»ƒ dÃ¹ng assignment)")
                                                        .isCorrect(false).build());
                        questionOptionRepository.save(
                                        QuestionOption.builder().question(q4).content("Sai (State lÃ  Immutable)")
                                                        .isCorrect(true).build());

                        // --- QUIZ 3: CSDL ---
                        Quiz quiz3 = Quiz.builder()
                                        .title("Database & SQL Performance")
                                        .description("BÃ i kiá»ƒm tra vá» tá»‘i Æ°u hoÃ¡ cÃ¢u truy váº¥n vÃ  Ä‘Ã¡nh Index trÃªn CSDL Quan há»‡.")
                                        .timeLimitMinutes(45)
                                        .startTime(LocalDateTime.now().minusDays(10))
                                        .endTime(LocalDateTime.now().plusMonths(3))
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        quiz3 = quizRepository.save(quiz3);

                        Question q5 = Question.builder()
                                        .quiz(quiz3)
                                        .content("Lá»‡nh SQL nÃ o lÃ m sáº¡ch toÃ n bá»™ dá»¯ liá»‡u báº£ng cá»±c ká»³ nhanh vÃ  giáº£i phÃ³ng dung lÆ°á»£ng Ä‘Ä©a?")
                                        .questionType(QuestionType.SINGLE_CHOICE)
                                        .points(5.0)
                                        .build();
                        q5 = questionRepository.save(q5);
                        questionOptionRepository
                                        .save(QuestionOption.builder().question(q5).content("DELETE FROM table")
                                                        .isCorrect(false).build());
                        questionOptionRepository.save(
                                        QuestionOption.builder().question(q5).content("TRUNCATE TABLE table")
                                                        .isCorrect(true).build());

                        Question q6 = Question.builder()
                                        .quiz(quiz3)
                                        .content("Index B-Tree hoáº¡t Ä‘á»™ng hiá»‡u quáº£ cho loáº¡i truy váº¥n nÃ o? (Chá»n nhiá»u Ä‘Ã¡p Ã¡n)")
                                        .questionType(QuestionType.MULTIPLE_CHOICE)
                                        .points(5.0)
                                        .build();
                        q6 = questionRepository.save(q6);
                        questionOptionRepository.save(
                                        QuestionOption.builder().question(q6).content("TÃ¬m kiáº¿m chÃ­nh xÃ¡c (=)")
                                                        .isCorrect(true).build());
                        questionOptionRepository.save(
                                        QuestionOption.builder().question(q6).content("TÃ¬m khoáº£ng (<, >, BETWEEN)")
                                                        .isCorrect(true).build());
                        questionOptionRepository.save(QuestionOption.builder().question(q6)
                                        .content("TÃ¬m kiáº¿m theo chuá»—i (LIKE '%abc%')").isCorrect(false).build());

                        Question q7 = Question.builder()
                                        .quiz(quiz3)
                                        .content(
                                                        "Tá»« khoÃ¡ SQL Ä‘á»ƒ ná»‘i hai báº£ng, chá»‰ láº¥y cÃ¡c dÃ²ng khá»›p (matchs) á»Ÿ cáº£ 2 bÃªn (Äiá»n vÃ o chá»— trá»‘ng)")
                                        .questionType(QuestionType.FILL_BLANK)
                                        .points(5.0)
                                        .build();
                        q7 = questionRepository.save(q7);
                        questionOptionRepository
                                        .save(QuestionOption.builder().question(q7).content("INNER JOIN")
                                                        .isCorrect(true).build());
                        questionOptionRepository
                                        .save(QuestionOption.builder().question(q7).content("JOIN").isCorrect(true)
                                                        .build());

                        log.info("Khá»Ÿi táº¡o Ä‘á» thi máº«u hoÃ n táº¥t!");
                }

                // Generate 10 extra generic quizzes to fill up the table
                if (quizRepository.count() <= 3) {
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
