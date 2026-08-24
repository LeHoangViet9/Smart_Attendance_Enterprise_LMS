package edufit_com_lms.common.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import edufit_com_lms.module.auth.entity.Role;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import edufit_com_lms.module.quiz.entity.Quiz;
import edufit_com_lms.module.quiz.entity.Question;
import edufit_com_lms.module.quiz.entity.QuestionOption;
import edufit_com_lms.module.quiz.entity.QuestionType;
import edufit_com_lms.module.quiz.repository.QuizRepository;
import edufit_com_lms.module.quiz.repository.QuestionRepository;
import edufit_com_lms.module.quiz.repository.QuestionOptionRepository;

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
                if (userRepository.count() == 0) {
                        log.info("Bắt đầu khởi tạo dữ liệu mẫu (Seeder)...");

                        // 1. Tài khoản Admin
                        User admin = User.builder()
                                        .email("admin@edu.vn")
                                        .password(passwordEncoder.encode("123456"))
                                        .fullName("Admin Quản Trị")
                                        .phone("0988888888") // Thêm số điện thoại
                                        .code("AD001")
                                        .role(Role.ADMIN)
                                        .address("Phòng Hành Chính")
                                        .isActive(true)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        userRepository.save(admin);

                        // 2. Tài khoản Sinh Viên (để test Face Onboarding)
                        User student = User.builder()
                                        .email("sinhvien@edu.vn")
                                        .password(passwordEncoder.encode("123456"))
                                        .fullName("Nguyễn Khắc Phục")
                                        .phone("0912345678") // Thêm số điện thoại
                                        .code("SV001")
                                        .role(Role.STUDENT)
                                        .address("Ký túc xá A")
                                        .isActive(true)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        userRepository.save(student);

                        log.info("Đã tạo xong dữ liệu mẫu! Mật khẩu chung là: 123456");
                }

                if (quizRepository.count() == 0) {
                        log.info("Bắt đầu khởi tạo dữ liệu đề thi mẫu (Quiz Seeder)...");

                        Quiz quiz = Quiz.builder()
                                        .title("Bài kiểm tra JAVA Backend đầu vào")
                                        .description(
                                                        "Bài kiểm tra tự động đánh giá kiến thức cơ bản về Spring Boot và Java Core, dành cho khoá K18.")
                                        .timeLimitMinutes(15)
                                        .startTime(LocalDateTime.now().minusDays(1))
                                        .endTime(LocalDateTime.now().plusMonths(1))
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        quiz = quizRepository.save(quiz);

                        Question q1 = Question.builder()
                                        .quiz(quiz)
                                        .content("Thành phần nào của Spring Boot được dùng để cấu hình tự động (Auto-configuration)?")
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
                                        .content("Java là ngôn ngữ lập trình thuần hướng đối tượng 100%?")
                                        .questionType(QuestionType.TRUE_FALSE)
                                        .points(5.0)
                                        .build();
                        q2 = questionRepository.save(q2);

                        questionOptionRepository
                                        .save(QuestionOption.builder().question(q2).content("Đúng").isCorrect(false)
                                                        .build());
                        questionOptionRepository.save(QuestionOption.builder().question(q2)
                                        .content("Sai (Vì vẫn hỗ trợ các kiểu nguyên thuỷ như int, char)")
                                        .isCorrect(true).build());

                        // --- QUIZ 2: React JS ---
                        Quiz quiz2 = Quiz.builder()
                                        .title("React JS Mastery")
                                        .description("Bài kiểm tra đánh giá kỹ năng xây dựng Component và Hooks trong React 18.")
                                        .timeLimitMinutes(30)
                                        .startTime(LocalDateTime.now().minusDays(5))
                                        .endTime(LocalDateTime.now().plusMonths(2))
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        quiz2 = quizRepository.save(quiz2);

                        Question q3 = Question.builder()
                                        .quiz(quiz2)
                                        .content("Hook nào được sử dụng để quản lý Side Effect trong Functional Component?")
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
                                        .content("Trong Redux, trạng thái (State) có thể bị thay đổi (mutate) trực tiếp không?")
                                        .questionType(QuestionType.TRUE_FALSE)
                                        .points(5.0)
                                        .build();
                        q4 = questionRepository.save(q4);
                        questionOptionRepository.save(
                                        QuestionOption.builder().question(q4).content("Đúng (Có thể dùng assignment)")
                                                        .isCorrect(false).build());
                        questionOptionRepository.save(
                                        QuestionOption.builder().question(q4).content("Sai (State là Immutable)")
                                                        .isCorrect(true).build());

                        // --- QUIZ 3: CSDL ---
                        Quiz quiz3 = Quiz.builder()
                                        .title("Database & SQL Performance")
                                        .description("Bài kiểm tra về tối ưu hoá câu truy vấn và đánh Index trên CSDL Quan hệ.")
                                        .timeLimitMinutes(45)
                                        .startTime(LocalDateTime.now().minusDays(10))
                                        .endTime(LocalDateTime.now().plusMonths(3))
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                        quiz3 = quizRepository.save(quiz3);

                        Question q5 = Question.builder()
                                        .quiz(quiz3)
                                        .content("Lệnh SQL nào làm sạch toàn bộ dữ liệu bảng cực kỳ nhanh và giải phóng dung lượng đĩa?")
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
                                        .content("Index B-Tree hoạt động hiệu quả cho loại truy vấn nào? (Chọn nhiều đáp án)")
                                        .questionType(QuestionType.MULTIPLE_CHOICE)
                                        .points(5.0)
                                        .build();
                        q6 = questionRepository.save(q6);
                        questionOptionRepository.save(
                                        QuestionOption.builder().question(q6).content("Tìm kiếm chính xác (=)")
                                                        .isCorrect(true).build());
                        questionOptionRepository.save(
                                        QuestionOption.builder().question(q6).content("Tìm khoảng (<, >, BETWEEN)")
                                                        .isCorrect(true).build());
                        questionOptionRepository.save(QuestionOption.builder().question(q6)
                                        .content("Tìm kiếm theo chuỗi (LIKE '%abc%')").isCorrect(false).build());

                        Question q7 = Question.builder()
                                        .quiz(quiz3)
                                        .content(
                                                        "Từ khoá SQL để nối hai bảng, chỉ lấy các dòng khớp (matchs) ở cả 2 bên (Điền vào chỗ trống)")
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

                        log.info("Khởi tạo đề thi mẫu hoàn tất!");
                }
        }
}
