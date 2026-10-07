import os

file_path = 'src/main/java/edufit_com_lms/module/quiz/service/impl/QuizAttemptServiceImpl.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

start_idx = content.find('    @org.springframework.scheduling.annotation.Async')
if start_idx != -1:
    content = content[:start_idx]

async_method = '''
    @org.springframework.scheduling.annotation.Async
    @Override
    public void batchGradeQuizWithAIAsync(Long quizId, Long lecturerId) {
        edufit_com_lms.module.quiz.entity.Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new edufit_com_lms.common.exception.ResourceNotFound("Can not found quiz"));
        
        // Find all completed attempts for this quiz
        java.util.List<edufit_com_lms.module.quiz.entity.QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);
        
        int gradedAttempts = 0;
        
        for (edufit_com_lms.module.quiz.entity.QuizAttempt attempt : attempts) {
            if (attempt.getStatus() != edufit_com_lms.module.quiz.entity.QuizStatus.COMPLETED) {
                continue;
            }
            
            boolean updated = false;
            double totalScore = attempt.getScore() != null ? attempt.getScore() : 0.0;
            
            for (edufit_com_lms.module.quiz.entity.StudentAnswer answer : attempt.getStudentAnswers()) {
                if (answer.getQuestion().getQuestionType() == edufit_com_lms.module.quiz.entity.QuestionType.ESSAY) {
                    if (answer.getEarnedPoints() == null) {
                        try {
                            String qContent = answer.getQuestion().getContent();
                            String aText = answer.getAnswerText();
                            double maxPts = answer.getQuestion().getPoints() != null ? answer.getQuestion().getPoints() : 0.0;
                            
                            edufit_com_lms.module.quiz.dto.response.AIGradeSuggestionResponse aiRes = aiGradingService.suggestGrade(qContent, aText, maxPts);
                            
                            answer.setEarnedPoints(aiRes.getPoints());
                            answer.setFeedback(aiRes.getFeedback());
                            studentAnswerRepository.save(answer);
                            
                            totalScore += aiRes.getPoints();
                            updated = true;
                        } catch (Exception e) {
                            log.error("Error batch grading answer " + answer.getId(), e);
                        }
                    }
                }
            }
            
            if (updated) {
                attempt.setScore(totalScore);
                quizAttemptRepository.save(attempt);
                gradedAttempts++;
            }
        }
        
        // Send notification to lecturer
        if (lecturerId != null && eventPublisher != null) {
            String message = String.format("Hệ thống đã hoàn tất chấm điểm tự động AI cho bài thi '%s'. Đã chấm %d bài nộp.", quiz.getTitle(), gradedAttempts);
            edufit_com_lms.module.notification.event.NotificationEvent event = edufit_com_lms.module.notification.event.NotificationEvent.builder()
                .title("Chấm điểm AI hoàn tất")
                .message(message)
                .type("SYSTEM_LOG")
                .recipientId(lecturerId)
                .build();
            eventPublisher.publishEvent(event);
        }
    }
}
'''

last_brace_idx = content.rfind('}')
if last_brace_idx != -1:
    content = content[:last_brace_idx] + async_method

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
