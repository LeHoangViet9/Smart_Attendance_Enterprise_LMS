# -*- coding: utf-8 -*-
file_path = 'd:/Educo/LMS/Smart_Attendance_Enterprise_LMS/src/main/java/edufit_com_lms/module/quiz/service/impl/QuizAttemptServiceImpl.java'

with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
    lines = f.readlines()

lines[77] = '            // Khôi phục dữ liệu nháp từ Redis và nhả về cho sinh viên thi\n'
lines[78] = '            // tiếp tục\n'
lines[90] = '                log.error("Lỗi khi đọc dữ liệu nháp từ Redis cho bài thi: " + existingAttempt.getId(), e);\n'
lines[92] = '            return res; // Trả về attempt cũ thay vì quăng lỗi\n'
lines[95] = '        // Kiểm tra mật khẩu (nếu đề thi yêu cầu)\n'
lines[98] = '                throw new ConflictException("Mật khẩu bài thi không chính xác");\n'
lines[102] = '        // Kiểm tra số lần làm tối đa (nếu có)\n'

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(lines)

print('Lines updated successfully.')
