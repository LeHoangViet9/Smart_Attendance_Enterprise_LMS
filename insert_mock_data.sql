-- Kịch bản SQL để INSERT dữ liệu mẫu cho Quizzes, Questions và Options (LMS)
-- BỘ DỮ LIỆU LỚN KHỔNG LỒ (Mở Rộng)

-- 1. Thêm 10 Đề thi (Quizzes) với độ khó và cài đặt khác nhau
INSERT INTO quizzes (id, title, description, time_limit_minutes, start_time, end_time, created_by_user_id, show_score, review_type, created_at, updated_at) VALUES 
(1001, 'Kiểm tra ReactJS Cơ bản', 'Đề kiểm tra trắc nghiệm sơ bộ về React hooks, functional component.', 30, '2024-01-01 00:00:00', '2026-12-31 23:59:59', NULL, true, 'IMMEDIATE', NOW(), NOW()),
(1002, 'Bài thi cuối kỳ Lập trình Web', 'Gồm các câu hỏi vận dụng cao về kiến trúc web hiện đại.', 60, '2026-08-01 07:00:00', '2026-08-30 09:00:00', NULL, false, 'AFTER_DEADLINE', NOW(), NOW()),
(1003, 'Đánh giá năng lực Java Spring Boot', 'Kiểm tra tư duy Dependency Injection, JPA Hibernate và OOP.', 45, '2025-01-01 00:00:00', '2027-01-01 00:00:00', NULL, true, 'NEVER', NOW(), NOW()),
(1004, 'TOEIC Đầu vào - Reading & Listening', 'Kiểm tra tiếng Anh định kì dành cho sinh viên IT.', 120, '2026-01-01 00:00:00', '2026-12-31 23:59:59', NULL, true, 'IMMEDIATE', NOW(), NOW()),
(1005, 'Đại Số Tuyến Tính - Giữa Kỳ', 'Đề thi trắc nghiệm Toán học kết hợp tự điền kết quả ma trận.', 45, '2026-01-01 00:00:00', '2026-12-31 23:59:59', NULL, false, 'AFTER_DEADLINE', NOW(), NOW()),
(1006, 'Nhập môn Trí Tuệ Nhân Tạo (AI)', 'Quiz nhanh về Machine Learning và Deep Learning cơ bản.', 20, '2026-01-01 00:00:00', '2026-12-31 23:59:59', NULL, true, 'IMMEDIATE', NOW(), NOW()),
(1007, 'Kiến Trúc Máy Tính', 'Kỳ thi kết thúc học phần môn Kiến trúc máy tính.', 90, '2026-08-01 00:00:00', '2026-08-15 23:59:59', NULL, true, 'NEVER', NOW(), NOW()),
(1008, 'Bài tập thực hành Cấu Trúc Dữ Liệu', 'Luyện tập Tree, Graph, Hashing.', NULL, '2026-01-01 00:00:00', '2050-01-01 00:00:00', NULL, true, 'IMMEDIATE', NOW(), NOW()),
(1009, 'Test Tâm Lý Học Học Đường', 'Khảo sát dạng trắc nghiệm không tính điểm gắt gao.', 15, '2026-01-01 00:00:00', '2050-01-01 00:00:00', NULL, false, 'IMMEDIATE', NOW(), NOW()),
(1010, 'Thi vòng loại Olympic Tin Học Quốc Gia', 'Dành cho đội tuyển thuật toán cực khó.', 180, '2026-10-01 00:00:00', '2026-10-02 23:59:59', NULL, false, 'NEVER', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 2. Hệ thống Khổng Lồ Các Câu Hỏi (Questions) đa phần các loại Quiz
INSERT INTO questions (id, quiz_id, content, points, question_type) VALUES 
-- [Quiz 1001: ReactJS] 10 điểm (5 câu, 2 điểm/câu)
(1001, 1001, 'Hook nào sau đây dùng để quản lý trạng thái (state) trong Functional Component?', 2.0, 'SINGLE_CHOICE'),
(1002, 1001, 'Trong React, thuộc tính "key" dùng để làm gì khi sử dụng hàm map() để render danh sách?', 2.0, 'MULTIPLE_CHOICE'),
(1003, 1001, 'Bản chất React sử dụng ___ DOM để tối ưu hóa việc cập nhật giao diện mà không cần render lại toàn trang.', 2.0, 'FILL_BLANK'),
(1004, 1001, 'React được phát triển gốc bởi công ty nào?', 2.0, 'SINGLE_CHOICE'),
(1005, 1001, 'React Router là thư viện có sẵn được đóng gói cùng với thư viện lõi của React, đúng hay sai?', 2.0, 'TRUE_FALSE'),

-- [Quiz 1002: Web] 10 điểm
(1006, 1002, 'Đâu là Web Server phổ biến nhất?', 2.5, 'SINGLE_CHOICE'),
(1007, 1002, 'HTTPS là giao thức HTTP an toàn sử dụng port 80 mặc định. Đúng hay Sai?', 2.5, 'TRUE_FALSE'),
(1008, 1002, 'Chọn những mã trạng thái HTTP biểu thị Lỗi từ phía Server (Server Error)', 2.5, 'MULTIPLE_CHOICE'),
(1009, 1002, 'JWT là chữ viết tắt của JSON Web ___, dùng để truyền tải thông tin an toàn.', 2.5, 'FILL_BLANK'),

-- [Quiz 1003: Spring Boot]
(1010, 1003, 'Annotation nào được dùng để đánh dấu một REST API controller trong Spring?', 3.0, 'SINGLE_CHOICE'),
(1011, 1003, 'Dependency Injection bao gồm những kiểu nhúng (inject) nào?', 4.0, 'MULTIPLE_CHOICE'),
(1012, 1003, 'Cơ chế JPA (Java Persistence API) không thể tự động sinh bảng trong CSDL (DDL auto). Đúng hay sai?', 3.0, 'TRUE_FALSE'),

-- [Quiz 1004: TOEIC]
(1013, 1004, 'She has been working here ___ 10 years.', 2.5, 'SINGLE_CHOICE'),
(1014, 1004, 'Trái nghĩa với từ "Agile" (Nhanh nhẹn) trong phần mềm là?', 2.5, 'SINGLE_CHOICE'),
(1015, 1004, 'I ___ my homework before you called.', 2.5, 'SINGLE_CHOICE'),
(1016, 1004, 'The term "Bug" was originated from a real insect found in a computer. True or False?', 2.5, 'TRUE_FALSE'),

-- [Quiz 1005: Đại sô tuyến tính]
(1017, 1005, 'Định thức của Ma trận đơn vị (Identity Matrix) bằng bao nhiêu?', 3.0, 'FILL_BLANK'),
(1018, 1005, 'Nếu Det(A) = 0 thì ma trận A khả nghịch. Đúng hay Sai?', 3.0, 'TRUE_FALSE'),
(1019, 1005, 'Đâu không thuộc các phép toán biến đổi sơ cấp trên dòng?', 4.0, 'MULTIPLE_CHOICE'),

-- [Quiz 1006: Trí tuệ AI]
(1020, 1006, 'Mô hình học máy chia làm hai nhánh cơ bản là Supervised Learning (Học có giám sát) và ___ Learning (Học không giám sát).', 5.0, 'FILL_BLANK'),
(1021, 1006, 'Mô hình ChatGPT thuộc thế hệ mạng thần kinh nào dưới đây?', 5.0, 'SINGLE_CHOICE')
ON CONFLICT (id) DO NOTHING;


-- 3. Chi tiết Hàng Trăm Options (Phương án)
INSERT INTO question_options (id, question_id, content, is_correct) VALUES 
-- [Q 1001 - React]
(1001, 1001, 'useEffect', false), (1002, 1001, 'useContext', false), (1003, 1001, 'useState', true), (1004, 1001, 'useReducer', false),
-- [Q 1002 - React] (Nhiều phương án đúng)
(1005, 1002, 'Để React nhận dạng duy nhất Element lúc DOM thay đổi', true), (1006, 1002, 'Để CSS selector query theo class', false), (1007, 1002, 'Hỗ trợ Virtual DOM tối ưu năng suất Render', true), (1008, 1002, 'Dùng làm tên biến state', false),
-- [Q 1003 - React Fill Blank]
(1009, 1003, 'Virtual', true), (1010, 1003, 'virtual', true), (1011, 1003, 'ảo', true),
-- [Q 1004 - React Gốc]
(1012, 1004, 'Google', false), (1013, 1004, 'Facebook (Meta)', true), (1014, 1004, 'Microsoft', false), (1015, 1004, 'Twitter', false),
-- [Q 1005 - React TF]
(1016, 1005, 'True (Đúng)', false), (1017, 1005, 'False (Sai)', true),

-- [Q 1006 - Web]
(1018, 1006, 'Nginx / Apache', true), (1019, 1006, 'MySQL', false), (1020, 1006, 'Firefox', false), (1021, 1006, 'Postman', false),
-- [Q 1007 - Web TF]
(1022, 1007, 'Đúng', false), (1023, 1007, 'Sai', true),
-- [Q 1008 - Web Multiple]
(1024, 1008, '500 Internal Server Error', true), (1025, 1008, '503 Service Unavailable', true), (1026, 1008, '404 Not Found', false), (1027, 1008, '502 Bad Gateway', true),
-- [Q 1009 - Web Fill Blank]
(1028, 1009, 'Token', true), (1029, 1009, 'token', true),

-- [Q 1010 - Spring]
(1030, 1010, '@RestController', true), (1031, 1010, '@Service', false), (1032, 1010, '@Bean', false), (1033, 1010, '@Entity', false),
-- [Q 1011 - Spring Multiple]
(1034, 1011, 'Constructor Injection', true), (1035, 1011, 'Setter Injection', true), (1036, 1011, 'Field Injection', true), (1037, 1011, 'Model Injection', false),
-- [Q 1012 - Spring TF]
(1038, 1012, 'Đúng', false), (1039, 1012, 'Sai', true),

-- [Q 1013 - TOEIC]
(1040, 1013, 'in', false), (1041, 1013, 'since', false), (1042, 1013, 'for', true), (1043, 1013, 'at', false),
-- [Q 1014 - TOEIC]
(1044, 1014, 'Sluggish', true), (1045, 1014, 'Fast', false), (1046, 1014, 'Innovative', false), (1047, 1014, 'Interactive', false),
-- [Q 1015 - TOEIC]
(1048, 1015, 'have done', false), (1049, 1015, 'did', false), (1050, 1015, 'had done', true), (1051, 1015, 'do', false),
-- [Q 1016 - TOEIC TF]
(1052, 1016, 'True', true), (1053, 1016, 'False', false),

-- [Q 1017 - ĐSTT Fill]
(1054, 1017, '1', true), (1055, 1017, '1.0', true), (1056, 1017, 'một', true),
-- [Q 1018 - ĐSTT TF]
(1057, 1018, 'Đúng', false), (1058, 1018, 'Sai', true),
-- [Q 1019 - ĐSTT Multiple]
(1059, 1019, 'Nhân một dòng với vô số', true), (1060, 1019, 'Bình phương một dòng', true), (1061, 1019, 'Đổi chỗ hai dòng', false), (1062, 1019, 'Trừ ma trận chuyển vị', true),

-- [Q 1020 - AI Fill]
(1063, 1020, 'Unsupervised', true), (1064, 1020, 'unsupervised', true),
-- [Q 1021 - AI Single]
(1065, 1021, 'Transformer (LLM)', true), (1066, 1021, 'CNN (Convolutional)', false), (1067, 1021, 'GAN', false), (1068, 1021, 'RNN', false)
ON CONFLICT (id) DO NOTHING;

-- 4. Thêm Dữ Liệu Courses (Khóa học)
INSERT INTO courses (id, title, description, thumbnail_url, lecturer_id, is_published, created_at) VALUES 
('b301b0fc-1b4e-4f5c-897c-3f2d2d5a3b90', 'Khoá học ReactJS Thực chiến', 'Giảng viên sẽ dắt tay bạn đi làm Frontend.', NULL, 1, true, NOW()),
('b301b0fc-1b4e-4f5c-897c-3f2d2d5a3b91', 'Khoá học Java Spring Boot', 'Trở thành Backend Developer xuất sắc.', NULL, 1, true, NOW())
ON CONFLICT (id) DO NOTHING;

-- 5. Thêm Dữ Liệu Lessions (Bài giảng)
INSERT INTO lessions (id, course_id, title, content, video_url, document_url, order_index, created_at) VALUES 
('c201b0fc-1b4e-4f5c-897c-3f2d2d5a3b90', 'b301b0fc-1b4e-4f5c-897c-3f2d2d5a3b90', 'Bài 1: Giới thiệu React', 'Nội dung cơ bản giúp người mới tiếp cận dễ dàng.', NULL, NULL, 1, NOW()),
('c201b0fc-1b4e-4f5c-897c-3f2d2d5a3b91', 'b301b0fc-1b4e-4f5c-897c-3f2d2d5a3b90', 'Bài 2: UseState & UseEffect', 'Master 2 hook quan trọng nhất trong React.', NULL, NULL, 2, NOW())
ON CONFLICT (id) DO NOTHING;

-- 6. Thêm Dữ Liệu Assignments (Bài tập)
INSERT INTO assignments (id, class_id, title, description, due_date, max_score, attachment_url, created_at) VALUES 
('a101b0fc-1b4e-4f5c-897c-3f2d2d5a3b90', 'b301b0fc-1b4e-4f5c-897c-3f2d2d5a3b90', 'Bài tập Todo List', 'Viết ứng dụng todolist gửi qua link github', '2026-12-31 23:59:00', 10, NULL, NOW()),
('a101b0fc-1b4e-4f5c-897c-3f2d2d5a3b91', 'b301b0fc-1b4e-4f5c-897c-3f2d2d5a3b91', 'API Quản lý sinh viên', 'Viết API CRUD đơn giản quản lý sinh viên', '2026-12-31 23:59:00', 10, NULL, NOW())
ON CONFLICT (id) DO NOTHING;


DROP TABLE IF EXISTS lessions CASCADE;
DROP TABLE IF EXISTS courses CASCADE;

-- ===================================================================
-- 2. TẠO LẠI BẢNG CHUẨN XÁC
-- ===================================================================
CREATE TABLE courses (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         title VARCHAR(255) NOT NULL,
                         description TEXT,
                         thumbnail_url VARCHAR(500),
                         lecturer_id BIGINT,
                         is_published BOOLEAN DEFAULT true,
                         created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE lessions (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
                          title VARCHAR(255) NOT NULL,
                          content TEXT,
                          video_url VARCHAR(500),
                          document_url VARCHAR(500),
                          order_index INT DEFAULT 1,
                          created_at TIMESTAMP DEFAULT NOW()
);

-- ===================================================================
-- 3. INSERT 4 KHÓA HỌC (COURSES)
-- ===================================================================
INSERT INTO courses (id, title, description, thumbnail_url, lecturer_id, is_published, created_at)
VALUES
    (
        'a1111111-1111-1111-1111-111111111111',
        'Lập trình Hướng đối tượng (OOP) Chuyên Sâu với Java',
        'Làm chủ 4 tính chất cốt lõi của OOP: Đóng gói (Encapsulation), Kế thừa (Inheritance), Đa hình (Polymorphism) và Trừu tượng (Abstraction).',
        'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=600&auto=format&fit=crop&q=80',
        1, true, NOW()
    ),
    (
        'a2222222-2222-2222-2222-222222222222',
        'Tuyển tập 300 Bài Code Thiếu Nhi & Tư Duy Thuật Toán',
        'Tổng hợp các bài toán kinh điển từ cơ bản đến nâng cao: xử lý chuỗi, mảng 2 chiều, quy hoạch động, đệ quy quay lui.',
        'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=600&auto=format&fit=crop&q=80',
        1, true, NOW()
    ),
    (
        'a3333333-3333-3333-3333-333333333333',
        'Phát triển Ứng dụng Web Doanh nghiệp với Spring Boot 3 & React',
        'Khóa học thực chiến xây dựng hệ thống LMS, kiến trúc Microservices, bảo mật Spring Security JWT, tích hợp MinIO Storage.',
        'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=600&auto=format&fit=crop&q=80',
        1, true, NOW()
    ),
    (
        'a4444444-4444-4444-4444-444444444444',
        'Cấu trúc Dữ liệu & Giải thuật Ứng dụng (DSA Master)',
        'Đi sâu vào Linked List, Binary Tree, Heap, Graph, Hash Table và phân tích độ phức tạp thời gian/không gian Big-O.',
        'https://images.unsplash.com/photo-1509228468518-180dd4864904?w=600&auto=format&fit=crop&q=80',
        1, true, NOW()
    );

-- ===================================================================
-- 4. INSERT CÁC BÀI GIẢNG (LESSIONS)
-- ===================================================================
INSERT INTO lessions (id, course_id, title, content, video_url, document_url, order_index, created_at)
VALUES
    (gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'Bài 1: Giới thiệu Tư duy Lập trình OOP', 'Khái niệm về Class và Object.', 'https://www.youtube.com/watch?v=pTB0EiLXUC8', 'https://drive.google.com/sample', 1, NOW()),
    (gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'Bài 2: Tính Đóng gói (Encapsulation)', 'Access Modifiers, Getter/Setter.', 'https://www.youtube.com/watch?v=SiBw747zV5E', 'https://drive.google.com/sample', 2, NOW()),
    (gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'Bài 3: Tính Kế thừa & Đa hình', 'Từ khóa extends, super, override.', 'https://www.youtube.com/watch?v=4bmSL33mEBo', 'https://drive.google.com/sample', 3, NOW()),

    (gen_random_uuid(), 'a2222222-2222-2222-2222-222222222222', 'Thử thách 01: Thuật toán Two Sum', 'Kỹ thuật Two Pointers và HashMap.', 'https://www.youtube.com/watch?v=KLlXCFG5TnA', 'https://leetcode.com/problems/two-sum/', 1, NOW()),
    (gen_random_uuid(), 'a2222222-2222-2222-2222-222222222222', 'Thử thách 02: Chuỗi Đối Xứng (Palindrome)', 'Kỹ thuật duyệt chuỗi tối ưu O(1).', 'https://www.youtube.com/watch?v=0k_eXXchT5E', 'https://leetcode.com/problems/valid-palindrome/', 2, NOW()),

    (gen_random_uuid(), 'a3333333-3333-3333-3333-333333333333', 'Bài 1: Kiến trúc Spring Boot & DI', 'Tìm hiểu IoC, Beans, @Component, @Autowired.', 'https://www.youtube.com/watch?v=9SGDpanrc8U', 'https://spring.io/guides', 1, NOW()),
    (gen_random_uuid(), 'a3333333-3333-3333-3333-333333333333', 'Bài 2: Thiết kế RESTful API', 'Quy chuẩn đặt tên API, HTTP Status Codes.', 'https://www.youtube.com/watch?v=vtPkZShrvXQ', 'https://spring.io/guides', 2, NOW());

-- ===================================================================
-- 5. INSERT BÀI TẬP LỚN (ASSIGNMENTS)
-- ===================================================================
INSERT INTO assignments (id, class_id, title, description, due_date, max_score, attachment_url, created_at)
VALUES
    (
        gen_random_uuid(), '3fa85f64-5717-4562-b3fc-2c963f66afa6',
        'Bài Tập Lớn OOP: Xây Dựng Hệ Thống Quản Lý Thư Viện (Library LMS)',
        'Yêu cầu:\n1. Thiết kế các lớp Book, User, BorrowTransaction áp dụng 4 tính chất OOP.\n2. Nộp file zip hoặc link GitHub.',
        NOW() + INTERVAL '7 days', 10.0, 'https://storage.edufit.vn/docs/OOP_Assignment.pdf', NOW()
    ),
    (
        gen_random_uuid(), '3fa85f64-5717-4562-b3fc-2c963f66afa6',
        'Thử Thách 300 Bài Code: Giải 5 Bài Toán Quy Hoạch Động',
        'Yêu cầu:\n1. Giải các bài toán Maximum Subarray, Coin Change.\n2. Tối ưu thời gian chạy dưới 50ms.',
        NOW() + INTERVAL '3 days', 10.0, 'https://storage.edufit.vn/docs/Code300_Challenge.pdf', NOW()
    ),
    (
        gen_random_uuid(), '3fa85f64-5717-4562-b3fc-2c963f66afa6',
        'Project Thực Hành: Backend REST API Xác Thực JWT & MinIO',
        'Yêu cầu:\n1. Cấu hình Spring Security JWT.\n2. Tích hợp upload file MinIO S3.',
        NOW() + INTERVAL '14 days', 10.0, 'https://storage.edufit.vn/docs/SpringBoot_Project.pdf', NOW()
    );

-- HÀNG LOẠT USERS MỚI
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_1@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 1', 'LECTURER', true, NOW(), '090100001', 'GVMASS1');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_2@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 2', 'LECTURER', true, NOW(), '090100002', 'GVMASS2');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_3@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 3', 'LECTURER', true, NOW(), '090100003', 'GVMASS3');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_4@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 4', 'LECTURER', true, NOW(), '090100004', 'GVMASS4');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_5@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 5', 'LECTURER', true, NOW(), '090100005', 'GVMASS5');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_6@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 6', 'LECTURER', true, NOW(), '090100006', 'GVMASS6');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_7@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 7', 'LECTURER', true, NOW(), '090100007', 'GVMASS7');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_8@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 8', 'LECTURER', true, NOW(), '090100008', 'GVMASS8');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_9@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 9', 'LECTURER', true, NOW(), '090100009', 'GVMASS9');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_10@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 10', 'LECTURER', true, NOW(), '090100010', 'GVMASS10');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_11@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 11', 'LECTURER', true, NOW(), '090100011', 'GVMASS11');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_12@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 12', 'LECTURER', true, NOW(), '090100012', 'GVMASS12');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_13@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 13', 'LECTURER', true, NOW(), '090100013', 'GVMASS13');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_14@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 14', 'LECTURER', true, NOW(), '090100014', 'GVMASS14');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_15@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 15', 'LECTURER', true, NOW(), '090100015', 'GVMASS15');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_16@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 16', 'LECTURER', true, NOW(), '090100016', 'GVMASS16');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_17@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 17', 'LECTURER', true, NOW(), '090100017', 'GVMASS17');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_18@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 18', 'LECTURER', true, NOW(), '090100018', 'GVMASS18');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_19@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 19', 'LECTURER', true, NOW(), '090100019', 'GVMASS19');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_20@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 20', 'LECTURER', true, NOW(), '090100020', 'GVMASS20');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_21@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 21', 'LECTURER', true, NOW(), '090100021', 'GVMASS21');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_22@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 22', 'LECTURER', true, NOW(), '090100022', 'GVMASS22');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_23@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 23', 'LECTURER', true, NOW(), '090100023', 'GVMASS23');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_24@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 24', 'LECTURER', true, NOW(), '090100024', 'GVMASS24');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_25@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 25', 'LECTURER', true, NOW(), '090100025', 'GVMASS25');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_26@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 26', 'LECTURER', true, NOW(), '090100026', 'GVMASS26');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_27@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 27', 'LECTURER', true, NOW(), '090100027', 'GVMASS27');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_28@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 28', 'LECTURER', true, NOW(), '090100028', 'GVMASS28');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_29@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 29', 'LECTURER', true, NOW(), '090100029', 'GVMASS29');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_30@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 30', 'LECTURER', true, NOW(), '090100030', 'GVMASS30');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_31@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 31', 'LECTURER', true, NOW(), '090100031', 'GVMASS31');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_32@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 32', 'LECTURER', true, NOW(), '090100032', 'GVMASS32');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_33@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 33', 'LECTURER', true, NOW(), '090100033', 'GVMASS33');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_34@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 34', 'LECTURER', true, NOW(), '090100034', 'GVMASS34');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_35@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 35', 'LECTURER', true, NOW(), '090100035', 'GVMASS35');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_36@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 36', 'LECTURER', true, NOW(), '090100036', 'GVMASS36');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_37@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 37', 'LECTURER', true, NOW(), '090100037', 'GVMASS37');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_38@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 38', 'LECTURER', true, NOW(), '090100038', 'GVMASS38');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_39@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 39', 'LECTURER', true, NOW(), '090100039', 'GVMASS39');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_40@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 40', 'LECTURER', true, NOW(), '090100040', 'GVMASS40');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_41@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 41', 'LECTURER', true, NOW(), '090100041', 'GVMASS41');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_42@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 42', 'LECTURER', true, NOW(), '090100042', 'GVMASS42');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_43@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 43', 'LECTURER', true, NOW(), '090100043', 'GVMASS43');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_44@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 44', 'LECTURER', true, NOW(), '090100044', 'GVMASS44');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_45@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 45', 'LECTURER', true, NOW(), '090100045', 'GVMASS45');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_46@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 46', 'LECTURER', true, NOW(), '090100046', 'GVMASS46');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_47@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 47', 'LECTURER', true, NOW(), '090100047', 'GVMASS47');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_48@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 48', 'LECTURER', true, NOW(), '090100048', 'GVMASS48');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_49@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Giảng viên Mass 49', 'LECTURER', true, NOW(), '090100049', 'GVMASS49');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_1@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 1', 'STUDENT', true, NOW(), '090200001', 'SVMASS1');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_2@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 2', 'STUDENT', true, NOW(), '090200002', 'SVMASS2');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_3@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 3', 'STUDENT', true, NOW(), '090200003', 'SVMASS3');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_4@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 4', 'STUDENT', true, NOW(), '090200004', 'SVMASS4');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_5@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 5', 'STUDENT', true, NOW(), '090200005', 'SVMASS5');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_6@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 6', 'STUDENT', true, NOW(), '090200006', 'SVMASS6');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_7@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 7', 'STUDENT', true, NOW(), '090200007', 'SVMASS7');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_8@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 8', 'STUDENT', true, NOW(), '090200008', 'SVMASS8');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_9@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 9', 'STUDENT', true, NOW(), '090200009', 'SVMASS9');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_10@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 10', 'STUDENT', true, NOW(), '090200010', 'SVMASS10');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_11@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 11', 'STUDENT', true, NOW(), '090200011', 'SVMASS11');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_12@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 12', 'STUDENT', true, NOW(), '090200012', 'SVMASS12');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_13@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 13', 'STUDENT', true, NOW(), '090200013', 'SVMASS13');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_14@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 14', 'STUDENT', true, NOW(), '090200014', 'SVMASS14');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_15@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 15', 'STUDENT', true, NOW(), '090200015', 'SVMASS15');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_16@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 16', 'STUDENT', true, NOW(), '090200016', 'SVMASS16');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_17@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 17', 'STUDENT', true, NOW(), '090200017', 'SVMASS17');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_18@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 18', 'STUDENT', true, NOW(), '090200018', 'SVMASS18');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_19@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 19', 'STUDENT', true, NOW(), '090200019', 'SVMASS19');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_20@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 20', 'STUDENT', true, NOW(), '090200020', 'SVMASS20');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_21@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 21', 'STUDENT', true, NOW(), '090200021', 'SVMASS21');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_22@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 22', 'STUDENT', true, NOW(), '090200022', 'SVMASS22');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_23@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 23', 'STUDENT', true, NOW(), '090200023', 'SVMASS23');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_24@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 24', 'STUDENT', true, NOW(), '090200024', 'SVMASS24');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_25@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 25', 'STUDENT', true, NOW(), '090200025', 'SVMASS25');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_26@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 26', 'STUDENT', true, NOW(), '090200026', 'SVMASS26');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_27@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 27', 'STUDENT', true, NOW(), '090200027', 'SVMASS27');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_28@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 28', 'STUDENT', true, NOW(), '090200028', 'SVMASS28');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_29@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 29', 'STUDENT', true, NOW(), '090200029', 'SVMASS29');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_30@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 30', 'STUDENT', true, NOW(), '090200030', 'SVMASS30');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_31@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 31', 'STUDENT', true, NOW(), '090200031', 'SVMASS31');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_32@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 32', 'STUDENT', true, NOW(), '090200032', 'SVMASS32');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_33@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 33', 'STUDENT', true, NOW(), '090200033', 'SVMASS33');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_34@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 34', 'STUDENT', true, NOW(), '090200034', 'SVMASS34');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_35@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 35', 'STUDENT', true, NOW(), '090200035', 'SVMASS35');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_36@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 36', 'STUDENT', true, NOW(), '090200036', 'SVMASS36');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_37@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 37', 'STUDENT', true, NOW(), '090200037', 'SVMASS37');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_38@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 38', 'STUDENT', true, NOW(), '090200038', 'SVMASS38');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_39@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 39', 'STUDENT', true, NOW(), '090200039', 'SVMASS39');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_40@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 40', 'STUDENT', true, NOW(), '090200040', 'SVMASS40');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_41@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 41', 'STUDENT', true, NOW(), '090200041', 'SVMASS41');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_42@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 42', 'STUDENT', true, NOW(), '090200042', 'SVMASS42');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_43@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 43', 'STUDENT', true, NOW(), '090200043', 'SVMASS43');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_44@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 44', 'STUDENT', true, NOW(), '090200044', 'SVMASS44');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_45@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 45', 'STUDENT', true, NOW(), '090200045', 'SVMASS45');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_46@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 46', 'STUDENT', true, NOW(), '090200046', 'SVMASS46');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_47@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 47', 'STUDENT', true, NOW(), '090200047', 'SVMASS47');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_48@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 48', 'STUDENT', true, NOW(), '090200048', 'SVMASS48');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_49@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 49', 'STUDENT', true, NOW(), '090200049', 'SVMASS49');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_50@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 50', 'STUDENT', true, NOW(), '090200050', 'SVMASS50');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_51@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 51', 'STUDENT', true, NOW(), '090200051', 'SVMASS51');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_52@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 52', 'STUDENT', true, NOW(), '090200052', 'SVMASS52');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_53@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 53', 'STUDENT', true, NOW(), '090200053', 'SVMASS53');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_54@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 54', 'STUDENT', true, NOW(), '090200054', 'SVMASS54');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_55@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 55', 'STUDENT', true, NOW(), '090200055', 'SVMASS55');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_56@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 56', 'STUDENT', true, NOW(), '090200056', 'SVMASS56');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_57@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 57', 'STUDENT', true, NOW(), '090200057', 'SVMASS57');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_58@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 58', 'STUDENT', true, NOW(), '090200058', 'SVMASS58');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_59@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 59', 'STUDENT', true, NOW(), '090200059', 'SVMASS59');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_60@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 60', 'STUDENT', true, NOW(), '090200060', 'SVMASS60');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_61@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 61', 'STUDENT', true, NOW(), '090200061', 'SVMASS61');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_62@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 62', 'STUDENT', true, NOW(), '090200062', 'SVMASS62');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_63@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 63', 'STUDENT', true, NOW(), '090200063', 'SVMASS63');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_64@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 64', 'STUDENT', true, NOW(), '090200064', 'SVMASS64');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_65@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 65', 'STUDENT', true, NOW(), '090200065', 'SVMASS65');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_66@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 66', 'STUDENT', true, NOW(), '090200066', 'SVMASS66');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_67@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 67', 'STUDENT', true, NOW(), '090200067', 'SVMASS67');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_68@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 68', 'STUDENT', true, NOW(), '090200068', 'SVMASS68');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_69@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 69', 'STUDENT', true, NOW(), '090200069', 'SVMASS69');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_70@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 70', 'STUDENT', true, NOW(), '090200070', 'SVMASS70');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_71@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 71', 'STUDENT', true, NOW(), '090200071', 'SVMASS71');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_72@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 72', 'STUDENT', true, NOW(), '090200072', 'SVMASS72');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_73@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 73', 'STUDENT', true, NOW(), '090200073', 'SVMASS73');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_74@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 74', 'STUDENT', true, NOW(), '090200074', 'SVMASS74');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_75@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 75', 'STUDENT', true, NOW(), '090200075', 'SVMASS75');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_76@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 76', 'STUDENT', true, NOW(), '090200076', 'SVMASS76');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_77@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 77', 'STUDENT', true, NOW(), '090200077', 'SVMASS77');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_78@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 78', 'STUDENT', true, NOW(), '090200078', 'SVMASS78');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_79@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 79', 'STUDENT', true, NOW(), '090200079', 'SVMASS79');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_80@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 80', 'STUDENT', true, NOW(), '090200080', 'SVMASS80');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_81@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 81', 'STUDENT', true, NOW(), '090200081', 'SVMASS81');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_82@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 82', 'STUDENT', true, NOW(), '090200082', 'SVMASS82');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_83@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 83', 'STUDENT', true, NOW(), '090200083', 'SVMASS83');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_84@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 84', 'STUDENT', true, NOW(), '090200084', 'SVMASS84');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_85@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 85', 'STUDENT', true, NOW(), '090200085', 'SVMASS85');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_86@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 86', 'STUDENT', true, NOW(), '090200086', 'SVMASS86');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_87@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 87', 'STUDENT', true, NOW(), '090200087', 'SVMASS87');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_88@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 88', 'STUDENT', true, NOW(), '090200088', 'SVMASS88');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_89@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 89', 'STUDENT', true, NOW(), '090200089', 'SVMASS89');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_90@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 90', 'STUDENT', true, NOW(), '090200090', 'SVMASS90');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_91@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 91', 'STUDENT', true, NOW(), '090200091', 'SVMASS91');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_92@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 92', 'STUDENT', true, NOW(), '090200092', 'SVMASS92');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_93@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 93', 'STUDENT', true, NOW(), '090200093', 'SVMASS93');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_94@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 94', 'STUDENT', true, NOW(), '090200094', 'SVMASS94');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_95@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 95', 'STUDENT', true, NOW(), '090200095', 'SVMASS95');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_96@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 96', 'STUDENT', true, NOW(), '090200096', 'SVMASS96');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_97@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 97', 'STUDENT', true, NOW(), '090200097', 'SVMASS97');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_98@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 98', 'STUDENT', true, NOW(), '090200098', 'SVMASS98');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_99@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 99', 'STUDENT', true, NOW(), '090200099', 'SVMASS99');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_100@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 100', 'STUDENT', true, NOW(), '090200100', 'SVMASS100');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_101@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 101', 'STUDENT', true, NOW(), '090200101', 'SVMASS101');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_102@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 102', 'STUDENT', true, NOW(), '090200102', 'SVMASS102');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_103@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 103', 'STUDENT', true, NOW(), '090200103', 'SVMASS103');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_104@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 104', 'STUDENT', true, NOW(), '090200104', 'SVMASS104');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_105@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 105', 'STUDENT', true, NOW(), '090200105', 'SVMASS105');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_106@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 106', 'STUDENT', true, NOW(), '090200106', 'SVMASS106');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_107@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 107', 'STUDENT', true, NOW(), '090200107', 'SVMASS107');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_108@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 108', 'STUDENT', true, NOW(), '090200108', 'SVMASS108');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_109@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 109', 'STUDENT', true, NOW(), '090200109', 'SVMASS109');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_110@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 110', 'STUDENT', true, NOW(), '090200110', 'SVMASS110');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_111@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 111', 'STUDENT', true, NOW(), '090200111', 'SVMASS111');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_112@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 112', 'STUDENT', true, NOW(), '090200112', 'SVMASS112');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_113@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 113', 'STUDENT', true, NOW(), '090200113', 'SVMASS113');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_114@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 114', 'STUDENT', true, NOW(), '090200114', 'SVMASS114');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_115@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 115', 'STUDENT', true, NOW(), '090200115', 'SVMASS115');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_116@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 116', 'STUDENT', true, NOW(), '090200116', 'SVMASS116');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_117@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 117', 'STUDENT', true, NOW(), '090200117', 'SVMASS117');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_118@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 118', 'STUDENT', true, NOW(), '090200118', 'SVMASS118');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_119@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 119', 'STUDENT', true, NOW(), '090200119', 'SVMASS119');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_120@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 120', 'STUDENT', true, NOW(), '090200120', 'SVMASS120');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_121@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 121', 'STUDENT', true, NOW(), '090200121', 'SVMASS121');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_122@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 122', 'STUDENT', true, NOW(), '090200122', 'SVMASS122');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_123@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 123', 'STUDENT', true, NOW(), '090200123', 'SVMASS123');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_124@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 124', 'STUDENT', true, NOW(), '090200124', 'SVMASS124');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_125@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 125', 'STUDENT', true, NOW(), '090200125', 'SVMASS125');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_126@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 126', 'STUDENT', true, NOW(), '090200126', 'SVMASS126');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_127@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 127', 'STUDENT', true, NOW(), '090200127', 'SVMASS127');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_128@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 128', 'STUDENT', true, NOW(), '090200128', 'SVMASS128');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_129@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 129', 'STUDENT', true, NOW(), '090200129', 'SVMASS129');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_130@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 130', 'STUDENT', true, NOW(), '090200130', 'SVMASS130');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_131@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 131', 'STUDENT', true, NOW(), '090200131', 'SVMASS131');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_132@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 132', 'STUDENT', true, NOW(), '090200132', 'SVMASS132');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_133@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 133', 'STUDENT', true, NOW(), '090200133', 'SVMASS133');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_134@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 134', 'STUDENT', true, NOW(), '090200134', 'SVMASS134');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_135@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 135', 'STUDENT', true, NOW(), '090200135', 'SVMASS135');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_136@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 136', 'STUDENT', true, NOW(), '090200136', 'SVMASS136');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_137@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 137', 'STUDENT', true, NOW(), '090200137', 'SVMASS137');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_138@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 138', 'STUDENT', true, NOW(), '090200138', 'SVMASS138');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_139@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 139', 'STUDENT', true, NOW(), '090200139', 'SVMASS139');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_140@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 140', 'STUDENT', true, NOW(), '090200140', 'SVMASS140');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_141@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 141', 'STUDENT', true, NOW(), '090200141', 'SVMASS141');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_142@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 142', 'STUDENT', true, NOW(), '090200142', 'SVMASS142');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_143@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 143', 'STUDENT', true, NOW(), '090200143', 'SVMASS143');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_144@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 144', 'STUDENT', true, NOW(), '090200144', 'SVMASS144');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_145@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 145', 'STUDENT', true, NOW(), '090200145', 'SVMASS145');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_146@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 146', 'STUDENT', true, NOW(), '090200146', 'SVMASS146');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_147@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 147', 'STUDENT', true, NOW(), '090200147', 'SVMASS147');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_148@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 148', 'STUDENT', true, NOW(), '090200148', 'SVMASS148');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_149@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 149', 'STUDENT', true, NOW(), '090200149', 'SVMASS149');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_150@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 150', 'STUDENT', true, NOW(), '090200150', 'SVMASS150');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_151@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 151', 'STUDENT', true, NOW(), '090200151', 'SVMASS151');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_152@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 152', 'STUDENT', true, NOW(), '090200152', 'SVMASS152');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_153@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 153', 'STUDENT', true, NOW(), '090200153', 'SVMASS153');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_154@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 154', 'STUDENT', true, NOW(), '090200154', 'SVMASS154');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_155@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 155', 'STUDENT', true, NOW(), '090200155', 'SVMASS155');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_156@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 156', 'STUDENT', true, NOW(), '090200156', 'SVMASS156');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_157@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 157', 'STUDENT', true, NOW(), '090200157', 'SVMASS157');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_158@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 158', 'STUDENT', true, NOW(), '090200158', 'SVMASS158');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_159@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 159', 'STUDENT', true, NOW(), '090200159', 'SVMASS159');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_160@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 160', 'STUDENT', true, NOW(), '090200160', 'SVMASS160');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_161@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 161', 'STUDENT', true, NOW(), '090200161', 'SVMASS161');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_162@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 162', 'STUDENT', true, NOW(), '090200162', 'SVMASS162');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_163@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 163', 'STUDENT', true, NOW(), '090200163', 'SVMASS163');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_164@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 164', 'STUDENT', true, NOW(), '090200164', 'SVMASS164');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_165@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 165', 'STUDENT', true, NOW(), '090200165', 'SVMASS165');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_166@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 166', 'STUDENT', true, NOW(), '090200166', 'SVMASS166');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_167@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 167', 'STUDENT', true, NOW(), '090200167', 'SVMASS167');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_168@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 168', 'STUDENT', true, NOW(), '090200168', 'SVMASS168');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_169@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 169', 'STUDENT', true, NOW(), '090200169', 'SVMASS169');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_170@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 170', 'STUDENT', true, NOW(), '090200170', 'SVMASS170');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_171@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 171', 'STUDENT', true, NOW(), '090200171', 'SVMASS171');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_172@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 172', 'STUDENT', true, NOW(), '090200172', 'SVMASS172');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_173@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 173', 'STUDENT', true, NOW(), '090200173', 'SVMASS173');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_174@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 174', 'STUDENT', true, NOW(), '090200174', 'SVMASS174');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_175@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 175', 'STUDENT', true, NOW(), '090200175', 'SVMASS175');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_176@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 176', 'STUDENT', true, NOW(), '090200176', 'SVMASS176');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_177@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 177', 'STUDENT', true, NOW(), '090200177', 'SVMASS177');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_178@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 178', 'STUDENT', true, NOW(), '090200178', 'SVMASS178');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_179@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 179', 'STUDENT', true, NOW(), '090200179', 'SVMASS179');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_180@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 180', 'STUDENT', true, NOW(), '090200180', 'SVMASS180');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_181@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 181', 'STUDENT', true, NOW(), '090200181', 'SVMASS181');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_182@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 182', 'STUDENT', true, NOW(), '090200182', 'SVMASS182');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_183@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 183', 'STUDENT', true, NOW(), '090200183', 'SVMASS183');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_184@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 184', 'STUDENT', true, NOW(), '090200184', 'SVMASS184');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_185@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 185', 'STUDENT', true, NOW(), '090200185', 'SVMASS185');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_186@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 186', 'STUDENT', true, NOW(), '090200186', 'SVMASS186');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_187@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 187', 'STUDENT', true, NOW(), '090200187', 'SVMASS187');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_188@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 188', 'STUDENT', true, NOW(), '090200188', 'SVMASS188');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_189@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 189', 'STUDENT', true, NOW(), '090200189', 'SVMASS189');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_190@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 190', 'STUDENT', true, NOW(), '090200190', 'SVMASS190');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_191@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 191', 'STUDENT', true, NOW(), '090200191', 'SVMASS191');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_192@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 192', 'STUDENT', true, NOW(), '090200192', 'SVMASS192');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_193@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 193', 'STUDENT', true, NOW(), '090200193', 'SVMASS193');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_194@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 194', 'STUDENT', true, NOW(), '090200194', 'SVMASS194');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_195@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 195', 'STUDENT', true, NOW(), '090200195', 'SVMASS195');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_196@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 196', 'STUDENT', true, NOW(), '090200196', 'SVMASS196');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_197@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 197', 'STUDENT', true, NOW(), '090200197', 'SVMASS197');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_198@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 198', 'STUDENT', true, NOW(), '090200198', 'SVMASS198');
INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_199@edu.vn', '$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy', 'Sinh viên Mass 199', 'STUDENT', true, NOW(), '090200199', 'SVMASS199');

-- HÀNG LOẠT MAJORS, CLASSES, COURSES
INSERT INTO majors (id, name, created_at, updated_at) VALUES ('9a6b4898-a8e1-49f5-8e60-b008c5e417e5', 'Công nghệ Web', NOW(), NOW());
INSERT INTO majors (id, name, created_at, updated_at) VALUES ('58e8a38c-a01b-45f2-9f47-5fdc3e3838b2', 'Hệ thống thông tin', NOW(), NOW());
INSERT INTO majors (id, name, created_at, updated_at) VALUES ('2a798974-4df6-45d1-b0fa-9dbdbab6b8e2', 'Marketing số', NOW(), NOW());
INSERT INTO majors (id, name, created_at, updated_at) VALUES ('d84d4505-7075-460a-9ecf-12be6f9e1ab1', 'Thiết kế đồ hoạ', NOW(), NOW());
INSERT INTO majors (id, name, created_at, updated_at) VALUES ('251d8855-ae5b-4a1b-8e7b-8d7b878ba579', 'An toàn thông tin', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('0b57f8aa-ef99-4ae5-91b0-85fb44bef2b4', 'CLASS-MASS-1', '251d8855-ae5b-4a1b-8e7b-8d7b878ba579', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('3ea40d01-906e-4541-9bd8-996931f00fda', 'CLASS-MASS-2', '251d8855-ae5b-4a1b-8e7b-8d7b878ba579', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('6ecc4fb4-6830-4f45-b5c8-f53f2e39a36b', 'CLASS-MASS-3', '58e8a38c-a01b-45f2-9f47-5fdc3e3838b2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('b64582c1-d415-4fb2-85a6-1d4e5ece79d7', 'CLASS-MASS-4', '9a6b4898-a8e1-49f5-8e60-b008c5e417e5', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('7deb96c2-6cda-40bd-a345-63f7951605c1', 'CLASS-MASS-5', '2a798974-4df6-45d1-b0fa-9dbdbab6b8e2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('cd4a33de-e902-4243-bac8-8da878744b0b', 'CLASS-MASS-6', '2a798974-4df6-45d1-b0fa-9dbdbab6b8e2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('702ffeef-004a-4de8-8053-6952bdd59db2', 'CLASS-MASS-7', '9a6b4898-a8e1-49f5-8e60-b008c5e417e5', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('ce195c5c-361c-48b9-a22f-a2fd87bbeded', 'CLASS-MASS-8', 'd84d4505-7075-460a-9ecf-12be6f9e1ab1', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('6508a8ea-ab9e-464a-96bf-9909907eb430', 'CLASS-MASS-9', '9a6b4898-a8e1-49f5-8e60-b008c5e417e5', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('f1778710-e74d-4e72-9a35-5baaf39ddf22', 'CLASS-MASS-10', '9a6b4898-a8e1-49f5-8e60-b008c5e417e5', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('3c6658a6-1a3d-4901-bd92-597f14804138', 'CLASS-MASS-11', '9a6b4898-a8e1-49f5-8e60-b008c5e417e5', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('c3c6ccb0-9580-424c-ac19-5771c40c185c', 'CLASS-MASS-12', '58e8a38c-a01b-45f2-9f47-5fdc3e3838b2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('7eda5c3f-87b2-49c6-afbf-39b5b012fbca', 'CLASS-MASS-13', 'd84d4505-7075-460a-9ecf-12be6f9e1ab1', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('4d383cb3-e2b7-4cef-bf16-1b7877982af8', 'CLASS-MASS-14', 'd84d4505-7075-460a-9ecf-12be6f9e1ab1', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('f90554dd-bf42-4d2f-8b0e-cf3fdb759054', 'CLASS-MASS-15', '9a6b4898-a8e1-49f5-8e60-b008c5e417e5', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('f1631b8b-c46b-4d61-bbe2-4bfb5cedaa84', 'CLASS-MASS-16', '2a798974-4df6-45d1-b0fa-9dbdbab6b8e2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('fcd663e7-1bfb-4119-b023-d6e513d30c66', 'CLASS-MASS-17', '251d8855-ae5b-4a1b-8e7b-8d7b878ba579', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('f9d05bbf-4102-4881-9a88-258beae4ad37', 'CLASS-MASS-18', '58e8a38c-a01b-45f2-9f47-5fdc3e3838b2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('67283dd0-36c3-4ee2-b451-a03a478ccf78', 'CLASS-MASS-19', '9a6b4898-a8e1-49f5-8e60-b008c5e417e5', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('6adf97d3-122e-4ab3-b247-42f404dc5e2e', 'CLASS-MASS-20', '2a798974-4df6-45d1-b0fa-9dbdbab6b8e2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('d55c3c36-b29a-423f-ac91-b4d526c83a47', 'CLASS-MASS-21', '251d8855-ae5b-4a1b-8e7b-8d7b878ba579', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('5d8e7f84-5a38-4476-89ca-8b69f1726bd7', 'CLASS-MASS-22', '58e8a38c-a01b-45f2-9f47-5fdc3e3838b2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('debe028c-cf9f-407b-a6be-a7ed7518530c', 'CLASS-MASS-23', '2a798974-4df6-45d1-b0fa-9dbdbab6b8e2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('8adfef37-1d99-4b28-a914-deb378aa79b4', 'CLASS-MASS-24', 'd84d4505-7075-460a-9ecf-12be6f9e1ab1', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('a117d211-e28f-4e5f-b9d5-d023ec1bafe9', 'CLASS-MASS-25', '2a798974-4df6-45d1-b0fa-9dbdbab6b8e2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('8faed867-c7c3-4193-828c-235e8b322577', 'CLASS-MASS-26', '58e8a38c-a01b-45f2-9f47-5fdc3e3838b2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('726066ce-becb-4982-be87-002cf6201e6a', 'CLASS-MASS-27', '58e8a38c-a01b-45f2-9f47-5fdc3e3838b2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('1eb3ad12-311b-40f3-a522-a02f2fe9a4fa', 'CLASS-MASS-28', '58e8a38c-a01b-45f2-9f47-5fdc3e3838b2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('81d21a85-64c5-4a61-b0af-a1086bee10a4', 'CLASS-MASS-29', '58e8a38c-a01b-45f2-9f47-5fdc3e3838b2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('0ec35b82-4765-4727-a223-14e1d0e11d52', 'CLASS-MASS-30', '2a798974-4df6-45d1-b0fa-9dbdbab6b8e2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('ba5bd4a5-7cbb-4a8e-a766-e5f79c510f99', 'CLASS-MASS-31', '58e8a38c-a01b-45f2-9f47-5fdc3e3838b2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('0b382af2-50fa-49c2-ba42-6a3a9197defb', 'CLASS-MASS-32', 'd84d4505-7075-460a-9ecf-12be6f9e1ab1', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('624ac62c-9639-42ca-af9f-d1d278437a76', 'CLASS-MASS-33', '9a6b4898-a8e1-49f5-8e60-b008c5e417e5', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('e710301e-8fc5-4283-8c63-7ceb97b2ad3d', 'CLASS-MASS-34', 'd84d4505-7075-460a-9ecf-12be6f9e1ab1', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('c7303706-571e-4ce4-bfc6-cdb0a20b936d', 'CLASS-MASS-35', '58e8a38c-a01b-45f2-9f47-5fdc3e3838b2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('60d6cab8-2eb0-46c4-8e81-a0eada640713', 'CLASS-MASS-36', '58e8a38c-a01b-45f2-9f47-5fdc3e3838b2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('61d7ce5c-cea3-49dd-b792-8b6ef9652926', 'CLASS-MASS-37', '251d8855-ae5b-4a1b-8e7b-8d7b878ba579', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('d8214e87-51f6-4237-80ed-f23d8744232f', 'CLASS-MASS-38', '58e8a38c-a01b-45f2-9f47-5fdc3e3838b2', NOW(), NOW());
INSERT INTO school_classes (id, class_name, major_id, created_at, updated_at) VALUES ('a317386e-e286-436d-99ea-6bedd6ebafd6', 'CLASS-MASS-39', '58e8a38c-a01b-45f2-9f47-5fdc3e3838b2', NOW(), NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('bcf38c77-f4cc-4473-b894-5ad23a81fa1d', 'Khóa học Mass chuyên sâu 1', 'Nội dung khóa 1', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('d7e21f27-0dce-4e8b-aac0-907935f1955b', 'Khóa học Mass chuyên sâu 2', 'Nội dung khóa 2', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('19160a1b-b829-4b10-afa1-65aa7c70441c', 'Khóa học Mass chuyên sâu 3', 'Nội dung khóa 3', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('fa951c09-8b13-462a-a86a-4764bba5a507', 'Khóa học Mass chuyên sâu 4', 'Nội dung khóa 4', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('884a74a5-463a-4eea-acaf-9a430d198cca', 'Khóa học Mass chuyên sâu 5', 'Nội dung khóa 5', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('c9a54f60-0e93-4451-b77b-92481f1e0eb9', 'Khóa học Mass chuyên sâu 6', 'Nội dung khóa 6', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('924c0586-8dbc-4dcf-87b3-b34bd6c9cf34', 'Khóa học Mass chuyên sâu 7', 'Nội dung khóa 7', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('e76d5d6d-9846-463c-8abf-e4ae1345a2db', 'Khóa học Mass chuyên sâu 8', 'Nội dung khóa 8', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('6777e021-2780-4fd8-8bcb-42aaae90fad3', 'Khóa học Mass chuyên sâu 9', 'Nội dung khóa 9', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('6c2387df-48c8-46cf-abb6-64cab91bf926', 'Khóa học Mass chuyên sâu 10', 'Nội dung khóa 10', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('8434d674-58e3-4aba-b0f2-1cf1e73d6a1d', 'Khóa học Mass chuyên sâu 11', 'Nội dung khóa 11', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('c0adba8d-46fd-4b62-b03a-a6e8005bb409', 'Khóa học Mass chuyên sâu 12', 'Nội dung khóa 12', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('c1a9d7a7-c8a7-4a53-b391-c38c1531260e', 'Khóa học Mass chuyên sâu 13', 'Nội dung khóa 13', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('275716b1-1d7b-4475-b4ab-6e2d222fe412', 'Khóa học Mass chuyên sâu 14', 'Nội dung khóa 14', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('278223b8-2fcf-4e24-8218-74bbe2da9a70', 'Khóa học Mass chuyên sâu 15', 'Nội dung khóa 15', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('e286918b-cc1b-45b0-89da-b04e6cdcc119', 'Khóa học Mass chuyên sâu 16', 'Nội dung khóa 16', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('30f19c6c-3fb5-45f3-9222-843877f0d4ef', 'Khóa học Mass chuyên sâu 17', 'Nội dung khóa 17', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('a890a9ea-7305-4c4b-a88c-5b9dcffd93b8', 'Khóa học Mass chuyên sâu 18', 'Nội dung khóa 18', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('93391c94-e63d-4f0d-b962-c1b40cd6637e', 'Khóa học Mass chuyên sâu 19', 'Nội dung khóa 19', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('0c6219a7-77dd-4de3-92e4-328cf67fdd08', 'Khóa học Mass chuyên sâu 20', 'Nội dung khóa 20', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('b5d85af4-b999-4a84-b16d-d2114baeb125', 'Khóa học Mass chuyên sâu 21', 'Nội dung khóa 21', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('fb2f10f1-8105-4ed2-9a08-fc616197ac86', 'Khóa học Mass chuyên sâu 22', 'Nội dung khóa 22', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('5b00f9f6-27f0-404f-b8a8-15dff5e61d42', 'Khóa học Mass chuyên sâu 23', 'Nội dung khóa 23', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('c7535ab1-d8bb-4de6-a208-010043db7b8d', 'Khóa học Mass chuyên sâu 24', 'Nội dung khóa 24', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('513cf073-b4e8-408f-958e-f49913c4970a', 'Khóa học Mass chuyên sâu 25', 'Nội dung khóa 25', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('fab3972d-6d01-4585-a77d-ce0c8c3028f9', 'Khóa học Mass chuyên sâu 26', 'Nội dung khóa 26', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('70b34d35-5db1-4703-af0c-af0e812bb2ea', 'Khóa học Mass chuyên sâu 27', 'Nội dung khóa 27', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('a305f9a7-383f-4ce7-8921-69d01c9d3249', 'Khóa học Mass chuyên sâu 28', 'Nội dung khóa 28', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('fd8b75a0-927e-487d-a452-d2c8fbe1ef91', 'Khóa học Mass chuyên sâu 29', 'Nội dung khóa 29', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('36328138-d2a0-40c2-86f9-21f9aaf74daa', 'Khóa học Mass chuyên sâu 30', 'Nội dung khóa 30', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('219e458b-6da2-4f67-a979-6624979b979e', 'Khóa học Mass chuyên sâu 31', 'Nội dung khóa 31', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('7615de4a-755f-48c7-b196-5178e812f9aa', 'Khóa học Mass chuyên sâu 32', 'Nội dung khóa 32', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('90af83fd-84b6-4a6b-8989-f15e38e40a97', 'Khóa học Mass chuyên sâu 33', 'Nội dung khóa 33', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('60d33183-d504-484f-a56b-c74a5d7bffa6', 'Khóa học Mass chuyên sâu 34', 'Nội dung khóa 34', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('7e3a1860-7834-4b82-9a55-d129ff521015', 'Khóa học Mass chuyên sâu 35', 'Nội dung khóa 35', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('c2fd7e52-6791-495d-abb2-fd73c3ab46f9', 'Khóa học Mass chuyên sâu 36', 'Nội dung khóa 36', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('29aa5206-feb2-4366-ab87-0d9e458e758c', 'Khóa học Mass chuyên sâu 37', 'Nội dung khóa 37', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('3a14897c-72b1-4b6c-a842-85d885f80db7', 'Khóa học Mass chuyên sâu 38', 'Nội dung khóa 38', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('db1ac187-3fed-4c81-8a60-8465b3e5c2db', 'Khóa học Mass chuyên sâu 39', 'Nội dung khóa 39', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('a7463ef1-ee7c-4654-91b8-9f063d15f2a8', 'Khóa học Mass chuyên sâu 40', 'Nội dung khóa 40', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('19ad62a0-1ede-40d4-885e-ba15635502c7', 'Khóa học Mass chuyên sâu 41', 'Nội dung khóa 41', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('518b9086-a9df-44f4-a31e-52a34fb5d5ba', 'Khóa học Mass chuyên sâu 42', 'Nội dung khóa 42', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('6e638636-dc38-4bfb-b739-acabe3d526bc', 'Khóa học Mass chuyên sâu 43', 'Nội dung khóa 43', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('0d7035a2-c6c0-4bba-84eb-26cf7937ddcc', 'Khóa học Mass chuyên sâu 44', 'Nội dung khóa 44', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('16367eaa-5a01-4790-b53d-51a1f0478d57', 'Khóa học Mass chuyên sâu 45', 'Nội dung khóa 45', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('5924adc9-cbc7-46d7-9c28-da3d9bbda0d7', 'Khóa học Mass chuyên sâu 46', 'Nội dung khóa 46', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('26be63e9-69c3-4553-9f98-1dcb761b2672', 'Khóa học Mass chuyên sâu 47', 'Nội dung khóa 47', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('46b0f08a-a834-430a-94ef-b6c83b4cde75', 'Khóa học Mass chuyên sâu 48', 'Nội dung khóa 48', true, NOW());
INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('7628f402-c62e-417a-9220-ebc89f613ccf', 'Khóa học Mass chuyên sâu 49', 'Nội dung khóa 49', true, NOW());
