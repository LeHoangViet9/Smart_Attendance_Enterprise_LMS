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
