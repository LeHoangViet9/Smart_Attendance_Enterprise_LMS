import json

file_path = 'd:/Educo/LMS/Smart_Attendance_Enterprise_LMS/src/main/java/edufit_com_lms/module/quiz/service/impl/QuizAttemptServiceImpl.java'

with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
    lines = f.readlines()

replacements = {
    61: '        // Check quiz time first\n',
    78: '            // Restore draft data from Redis and return it to the student\n',
    79: '            // to continue\n',
    91: '                log.error("Error reading draft data from Redis for quiz attempt: " + existingAttempt.getId(), e);\n',
    93: '            return res; // Return existing attempt instead of throwing an error\n',
    96: '        // Check password (if the quiz requires one)\n',
    99: '                throw new ConflictException("Incorrect quiz password");\n',
    103: '        // Check max attempts (if any)\n',
    109: '                throw new ConflictException("You have exceeded the maximum number of attempts for this quiz!");\n',
    113: '        // Block retrying for exams that require face scanning\n',
    114: '        // (only allow 1 attempt)\n',
    120: '                        "This exam requires proctoring (face scanning) so you are only allowed to take it once!");\n',
    147: '        // Check if the exam has passed the absolute system time limit\n',
    148: '        // \n',
    150: '            log.warn("Late submission (passed end time) - Quiz Attempt ID: {}", attemptId);\n',
    153: '        // If quiz has a time limit, check if submitted late\n',
    157: '                throw new ConflictException("Submission past the deadline (exceeded the time limit).");\n',
    166: '                        "This exam requires face verification! Please provide an attached photo.");\n',
    177: '                // Prevent spamming the same answer multiple times to exploit points\n',
    178: '                // \n',
    188: '                    log.warn("Question {} no longer exists, skipping.", answerReq.getQuestionId());\n',
    192: '                // Prevent swapping questions from another exam\n',
    194: '                    log.warn("Warning: Question {} does not belong to the current exam!", question.getId());\n',
    195: '                    continue; // Skip instead of failing the entire request\n',
    254: '                    // Essay questions will be manually graded by the lecturer later\n',
    273: '        // Auto-fill questions left blank by the student to have a grading record\n',
    274: '        // (especially for ESSAY questions)\n',
    306: '        // Completely delete the draft on Redis to free up memory\n',
    311: '        // Send notification to the quiz creator (Lecturer / Admin)\n',
    314: '                    .title("Student submitted an exam")\n',
    315: '                    .message("Student " + savedAttempt.getStudent().getFullName() + " just completed the exam: "\n',
    322: '        // Automatically send an academic warning to the student if the score is below 5.0\n',
    326: '                    .title("Academic Warning: Low Score")\n',
    327: '                    .message("You just scored " + finalScore + " points in the exam "\n',
    328: '                            + savedAttempt.getQuiz().getTitle() + ". Please review your knowledge!")\n',
    358: '            // Save and set TTL to 1 day to free up memory\n',
    361: '            log.error("Error serializing answers to save draft in Redis for attempt: " + attemptId, e);\n',
    377: '            // Students can only review when the attempt is COMPLETED\n',
    382: '            // Lecturers cannot grade an IN_PROGRESS exam\n',
    385: '                        "This exam is still in progress by the student. Cannot grade yet.");\n',
    393: '                throw new ConflictException("The lecturer does not allow reviewing this exam.");\n',
    398: '                            "Not yet time to review the exam (Must wait until the end time passes).");\n',
    417: '                // Shuffle options using the same seed\n',
    449: '        // Shuffle questions using the same seed to match\n',
    450: '        // the exam time\n',
    501: '                    throw new ConflictException("Graded points (" + qg.getPoints()\n',
    502: '                            + ") cannot exceed the maximum points of the question (" + maxPoints + ")");\n',
    505: '                    throw new ConflictException("Graded points cannot be negative");\n',
    534: '        // Send academic warning notification if score is low after lecturer\n',
    535: '        // manual grading\n',
    538: '                    .title("Academic Warning: Low Score")\n',
    539: '                    .message("The lecturer has graded the exam " + savedAttempt.getQuiz().getTitle()\n',
    540: '                            + ". You only scored " + finalScore\n',
    541: '                            + " points. Please review your knowledge!")\n',
    589: '                    // Call submitAttempt to automatically submit and grade the exam\n',
    595: '                                .title("System auto-submission")\n',
    597: '                                        + " was automatically submitted due to time limit.")\n',
    603: '                    log.error("Error auto-submitting attempt {}. Marked as ABANDONED.", attempt.getId(), e);\n',
    698: '                    // Keep the old score because it was manually graded by the lecturer\n',
    856: '            String[] columns = { "No.", "Full Name", "Email", "Start Time", "Submit Time",\n',
    857: '                    "Status", "Score" };\n',
    896: '            log.error("Error generating Excel file for quiz scores {}", quizId, e);\n',
    897: '            throw new RuntimeException("System error when exporting Excel file");\n'
}

for line_num, new_content in replacements.items():
    idx = line_num - 1
    if idx < len(lines):
        lines[idx] = new_content

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(lines)

print('Translated successfully.')
