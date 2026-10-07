import sys

file_path = 'd:/Educo/LMS/Smart_Attendance_Enterprise_LMS/src/main/java/edufit_com_lms/module/quiz/service/impl/QuizAttemptServiceImpl.java'

with open(file_path, 'rb') as f:
    lines = f.readlines()

lines[77] = '            // Khôi ph?c d? li?u nháp t? Redis và nh? v? cho sinh viên thi\r\n'.encode('utf-8')
lines[78] = '            // ti?p t?c\r\n'.encode('utf-8')
lines[90] = '                log.error("L?i khi d?c d? li?u nháp t? Redis cho bài thi: " + existingAttempt.getId(), e);\r\n'.encode('utf-8')
lines[92] = '            return res; // Tr? v? attempt cu thay vì quang l?i\r\n'.encode('utf-8')
lines[95] = '        // Ki?m tra m?t kh?u (n?u d? thi yêu c?u)\r\n'.encode('utf-8')
lines[98] = '                throw new ConflictException("M?t kh?u bài thi không chính xác");\r\n'.encode('utf-8')
lines[102] = '        // Ki?m tra s? l?n làm t?i da (n?u có)\r\n'.encode('utf-8')

with open(file_path, 'wb') as f:
    f.writelines(lines)

print('Lines updated successfully.')
