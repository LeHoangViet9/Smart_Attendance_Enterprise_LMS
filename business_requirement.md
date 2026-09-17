# Tài Liệu Yêu Cầu Nghiệp Vụ (Business Requirement Document - BRD)
## Dự án: Smart Attendance Enterprise LMS (Hệ thống Quản lý Học tập & Điểm danh Thông minh)

---

## 1. Giới thiệu Tổng quan (Introduction)

### 1.1. Bối cảnh và Mục đích (Background & Purpose)
Trong bối cảnh chuyển đổi số giáo dục và quản lý đào tạo doanh nghiệp, việc tự động hóa các quy trình quản lý học tập và theo dõi sự chuyên cần là vô cùng cấp thiết. Dự án **"Smart Attendance Enterprise LMS"** được xây dựng nhằm cung cấp một nền tảng quản lý học tập (LMS) toàn diện, kết hợp công nghệ nhận diện sinh trắc học (Facial Recognition) để tự động hóa hoàn toàn khâu điểm danh.

Tài liệu BRD này quy định các yêu cầu chức năng, phi chức năng, và kiến trúc tổng thể, làm cơ sở thống nhất giữa đội ngũ phát triển, kiểm thử và các bên liên quan (Stakeholders).

### 1.2. Mục tiêu dự án (Project Objectives)
- **Số hóa quy trình học tập**: Cung cấp công cụ quản lý khóa học, bài tập và bài thi trắc nghiệm trên cùng một hệ thống.
- **Hiện đại hóa điểm danh**: Ứng dụng AI/Face Recognition giúp giảm thiểu thời gian điểm danh thủ công, chống gian lận và tăng tính chính xác.
- **Nâng cao trải nghiệm**: Giao diện trực quan, tính năng tự động lưu (autosave) bài thi, hỗ trợ làm bài trên nhiều thiết bị.

### 1.3. Phạm vi hệ thống (In-Scope)
Hệ thống bao gồm các phân hệ chính sau:
1. **User & Identity Management**: Quản lý tài khoản, phân quyền và xác thực người dùng.
2. **Smart Attendance (Điểm danh AI)**: Đăng ký dữ liệu khuôn mặt (Onboarding) và định danh học viên.
3. **Core LMS**: Quản lý khóa học, lớp học, tài liệu, và quy trình giao/nhận/chấm bài tập (Assignment).
4. **Quiz Engine**: Xây dựng ngân hàng câu hỏi và tổ chức thi trắc nghiệm trực tuyến.
5. **Notification System**: Hệ thống thông báo tự động (Real-time hoặc In-app notification).

### 1.4. Ngoài phạm vi (Out of Scope)
- Hệ thống không xử lý cổng thanh toán tài chính (Payment Gateway) cho học phí trực tiếp trong giai đoạn 1.
- Không tích hợp hệ thống họp trực tuyến (Video Conference như Zoom/Google Meet) trực tiếp vào máy chủ nội bộ.

---

## 2. Đối tượng Người dùng (User Roles)

Hệ thống được thiết kế dựa trên 3 nhóm người dùng cốt lõi:

| Vai trò (Role) | Mô tả & Trách nhiệm chính |
| :--- | :--- |
| **Admin** (Quản trị viên) | Người vận hành hệ thống. Có toàn quyền quản lý tài khoản, lớp học, khóa học, và cấu hình các thông số chung. Xem các báo cáo thống kê hoạt động tổng thể. |
| **Lecturer** (Giảng viên) | Người trực tiếp giảng dạy. Có quyền tạo bài tập, ngân hàng câu hỏi, bài thi (Quiz), chấm điểm, và theo dõi tiến độ của sinh viên trong các lớp mình phụ trách. |
| **Student** (Học viên) | Người học. Có quyền tham gia các khóa học được phân bổ, nộp bài tập, làm bài kiểm tra trắc nghiệm, xem lại điểm số, và đăng ký dữ liệu khuôn mặt (Face Onboarding). |

---

## 3. Luồng Quy trình Nghiệp vụ Cơ bản (Business Workflows)

> [!TIP]
> Các luồng này mô tả cách các Role tương tác với hệ thống để hoàn thành một nghiệp vụ cốt lõi.

1. **Luồng Cấu hình Lớp học (LMS Setup Flow)**: 
   - *Admin* tạo Khóa học (Course) $\rightarrow$ Tạo Lớp học (Class) $\rightarrow$ Gán Sinh viên vào Lớp $\rightarrow$ Phân công *Lecturer* (Thủ công hoặc Tự động).
2. **Luồng Bài tập (Assignment Flow)**: 
   - *Lecturer* tạo Assignment kèm tài liệu qua presigned-url $\rightarrow$ *Student* nhận thông báo $\rightarrow$ *Student* nộp bài (Submit files) $\rightarrow$ *Lecturer* xem và chấm điểm (Grade) $\rightarrow$ *Student* nhận kết quả.
3. **Luồng Trắc nghiệm (Quiz Flow)**:
   - *Lecturer* tạo bộ câu hỏi (Questions) $\rightarrow$ Gom thành Đề thi (Quiz) và cài đặt thời gian $\rightarrow$ *Student* bắt đầu làm (Hệ thống autosave đáp án) $\rightarrow$ Nộp bài $\rightarrow$ Hệ thống tự động chấm điểm $\rightarrow$ *Student* xem lại (Review History).

---

## 4. Yêu cầu Chức năng (Functional Requirements)

### 4.1. Phân hệ Xác thực & Phân quyền (Auth & Identity)
- **FR_AUTH_01**: Cho phép Đăng nhập/Đăng xuất/Đổi mật khẩu an toàn với JWT Token.
- **FR_AUTH_02**: Cho phép tạo mới người dùng (Register) và quản lý trạng thái tài khoản.
- **FR_AUTH_03**: Admin có thể quản trị toàn bộ user, phân nhóm roles. Có Dashboard xem thống kê (lượt đăng nhập, số user active/inactive).

### 4.2. Phân hệ Quản lý Học tập (LMS Module)
- **FR_LMS_01 - Quản lý Khóa học/Lớp học**: Admin và Lecturer có thể xem, thêm, sửa, xóa các Khóa học và Bài giảng (Lessons).
- **FR_LMS_02 - Quản lý Danh sách lớp**: Lecturer theo dõi được lớp chủ nhiệm (Homeroom) và danh sách sinh viên. Admin có thể thực hiện Auto-Assign Lecturer cho các lớp.
- **FR_LMS_03 - Quản lý Bài tập**: 
  - Tạo, cập nhật, xóa Bài tập. 
  - Upload file kích thước lớn thông qua việc lấy Presigned-URL từ MinIO để client upload trực tiếp.
- **FR_LMS_04 - Nộp & Chấm bài**: Student upload file để nộp bài. Lecturer truy cập danh sách submission để nhập điểm và nhận xét.

#### 3.4. Phân hệ Thi Trắc nghiệm (Quiz System Module)
- **Ngân hàng Câu hỏi (Question Bank - Lecturer)**:
  - Tạo mới, cập nhật, xóa, và liệt kê các câu hỏi trắc nghiệm (Multiple Choice, Single Choice...).
- **Quản lý Đề thi (Quiz Management - Lecturer)**:
  - Tạo mới, cập nhật, xóa các bài Quiz, cấu hình thời gian làm bài, cấu hình mở bài/đóng bài (Quiz Status).
  - Cấu hình cờ `requiresProctoring` (Cần giám sát hình ảnh) cho các bài thi quan trọng để chống thi hộ.
- **Làm bài kiểm tra (Student Quiz)**:
  - Xem danh sách các bài Quiz được giao.
  - Xem chi tiết quy chế/nội dung trước khi bắt đầu Quiz.
  - Tạo lượt làm bài (Quiz Attempt).
  - Tính năng tự động lưu bài (Autosave): Tự động lưu đáp án khi sinh viên đang làm để tránh mất dữ liệu do rớt mạng.
  - Nộp bài (Submit) hoàn chỉnh: Nếu bài thi có bật chế độ giám sát (`requiresProctoring = true`), sinh viên bắt buộc phải tải lên hình ảnh khuôn mặt qua Webcam để đính kèm vào bài thi. Nếu là bài ôn tập bình thường thì bỏ qua.
- **Xem lại kết quả (Review History)**:
  - Sinh viên xem lại lịch sử các lần thi (Attempts History).
  - Giảng viên/Admin có thể xem ảnh giám sát (`proctoringImageUrl`) để đối chiếu với hồ sơ sinh viên khi chấm hoặc rà soát.

#### 3.5. Phân hệ Điểm danh Sinh trắc học (Smart Attendance)
- **FR_ATD_01 - Face Onboarding**: Cho phép Học viên tải lên (hoặc chụp qua webcam) hình ảnh khuôn mặt. Hệ thống gọi AI xử lý để trích xuất đặc trưng sinh trắc học và lưu trữ an toàn.
- **FR_ATD_02 - Điểm danh tại lớp học**:
  - Giảng viên mở chế độ "Điểm danh" thông qua giao diện Web/App. Camera của giảng viên sẽ quét qua lớp.
  - Hệ thống AI phân tích nhận diện, gửi danh sách các `studentId` có mặt về Backend.
  - Backend cập nhật danh sách vào cơ sở dữ liệu (đánh dấu PRESENT cho người có mặt, ABSENT cho những người còn lại trong lớp).
- **FR_ATD_03 - Lịch sử điểm danh**:
  - Giảng viên xem báo cáo điểm danh của lớp trong 1 ngày bất kỳ.
  - Sinh viên có thể xem lịch sử vắng/có mặt của bản thân.

#### 3.6. Phân hệ Thông báo & Báo cáo (Notification Module)
- **FR_NOTI_01**: Gửi thông báo trong ứng dụng (In-app AppNotification) khi có sự kiện: Có bài tập mới, có điểm bài tập, được gán vào lớp học.
- **FR_NOTI_02**: Học viên/Giảng viên có thể gửi Báo cáo sự cố (Report) cho Admin thông qua hệ thống.

---

## 5. Yêu cầu Phi chức năng (Non-Functional Requirements)

> [!IMPORTANT]
> Các yêu cầu này đảm bảo hệ thống vận hành ổn định, bảo mật và thân thiện với người dùng.

1. **Hiệu năng (Performance)**: 
   - API phản hồi trung bình < 500ms (ngoại trừ các tác vụ liên quan đến AI Onboarding có thể kéo dài hơn nhưng không quá 5s).
   - Hệ thống cho phép ít nhất 1000 sinh viên truy cập và làm Quiz đồng thời mà không bị treo.
2. **Bảo mật (Security)**:
   - Các API phải được bảo vệ bởi Spring Security Filters. Kiểm tra chặt chẽ Authorization ở cấp độ phương thức (Method-level Security) để đảm bảo Student không gọi được API của Lecturer.
   - Mật khẩu lưu trong CSDL phải được hash (băm) an toàn (BCrypt).
3. **Quản lý Tệp tin (Storage Reliability)**:
   - File bài tập, hình ảnh, tài liệu không lưu vào server backend nội bộ mà phải đẩy qua **MinIO/S3** Object Storage để đảm bảo khả năng sao lưu và chống đầy ổ cứng hệ thống.
4. **Tính sẵn sàng (Availability & Recovery)**:
   - Triển khai theo dạng Container hóa (Docker) để dễ dàng restart, scale khi có tải đột biến.
   - Cơ chế Autosave cho tính năng Quiz giúp Học viên không mất bài khi bị rớt mạng đột ngột.

---

## 6. Tổng quan Công nghệ (Tech Stack)

Để đáp ứng các yêu cầu trên, kiến trúc công nghệ được sử dụng bao gồm:
- **Backend Core**: `Java` với Framework `Spring Boot 3.x`.
- **Database**: Sử dụng cơ sở dữ liệu quan hệ kết hợp `Spring Data JPA` (tương thích MySQL/PostgreSQL).
- **Lưu trữ tĩnh (Object Storage)**: `MinIO` Server (Cung cấp API giống AWS S3 để quản lý file upload).
- **Authentication**: Stateless Auth với `JWT (JSON Web Token)`.
- **Triển khai (Deployment)**: `Docker` và `docker-compose` (Sẵn file `docker-compose.yml` trong source) dùng để build OCI images và khởi chạy services.
- **Build Tool**: `Gradle`.

---
*Tài liệu này là phiên bản tổng hợp và chuẩn hóa, sẵn sàng làm tài liệu tham khảo cốt lõi cho các đội ngũ Frontend, Backend và AI trong quá trình phát triển, kiểm thử.*
