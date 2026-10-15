# Chạy nhận diện khuôn mặt thật

Từ thư mục gốc dự án:

```powershell
docker compose up -d --build ai-service
```

Lần đầu cần tải mô hình và biên dịch dlib nên sẽ lâu hơn các lần sau. Dịch vụ chạy trên CPU, không yêu cầu GPU. Kiểm tra trạng thái tại <http://localhost:8000/health>; tài liệu API tại <http://localhost:8000/docs>.

Backend chạy trên Windows đã mặc định dùng `http://localhost:8000`. Nếu cần đổi địa chỉ, đặt biến môi trường `AI_SERVICE_URL` trước khi khởi động backend. Nếu sau này backend cũng chạy trong cùng mạng Docker Compose, dùng `http://ai-service:8000`.

## Thử với camera

1. Chạy backend ở cổng 8080 và frontend ở cổng 5173.
2. Đăng nhập bằng tài khoản sinh viên, mở <http://localhost:5173/face-onboarding>.
3. Cho phép camera, chờ hình ảnh xuất hiện, nhìn thẳng và bấm **Capture Face**. Chỉ có một khuôn mặt trong khung hình, đủ sáng.
4. Đảm bảo sinh viên đã được ghi danh vào lớp học phần.
5. Đăng nhập bằng tài khoản giảng viên phụ trách lớp, mở lớp rồi **Smart Attendance**.
6. Bấm **Start Smart Attendance**. Giao diện gửi ảnh mỗi 5 giây. Khi sinh viên được đánh dấu có mặt, bấm **Save Attendance** để lưu.

Camera được đọc bởi trình duyệt; container AI chỉ xử lý ảnh được backend gửi tới. Khi dùng máy khác qua mạng, cần cấu hình HTTPS cho trang mở camera và địa chỉ API phù hợp.

Đây là nhận diện khuôn mặt từ ảnh, chưa có kiểm tra người sống để chống dùng ảnh/video thay người thật. Không cần Gemini cho chức năng này. Vector khuôn mặt được lưu trong PostgreSQL; đăng ký lại sẽ thay vector cũ.

## Kiểm thử bằng mô hình thật

```powershell
python integration-tests/face-recognition.py
node integration-tests/backend.mjs --faces
```

Lệnh Python dùng thư viện chuẩn Python trên máy và tải ba ảnh mẫu công khai từ [dự án face_recognition](https://github.com/ageitgey/face_recognition/tree/master/examples) vào `build/face-fixtures`. Nó kiểm tra trích xuất vector, hai ảnh cùng người, ảnh khác người và dữ liệu không hợp lệ. Không sử dụng ảnh cá nhân của người dùng.

Lệnh Node cần backend kiểm thử cổng 18080 theo [hướng dẫn kiểm thử tích hợp](../integration-tests/README.md). Nó kiểm tra đăng ký khuôn mặt → lưu PostgreSQL → nhận diện đúng sinh viên trong lớp qua backend và AI thật. Không trỏ bộ kiểm thử vào dữ liệu sử dụng thực tế.

Kết quả lưu tại `build/face-recognition-results.json` và `build/integration-results.json`.
