import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './QuizStyles.css';

const QuizAttempt = () => {
    const { attemptId } = useParams();
    const navigate = useNavigate();

    const [quiz, setQuiz] = useState(null);
    const [attempt, setAttempt] = useState(null);
    const [answers, setAnswers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);
    const [showSubmitModal, setShowSubmitModal] = useState(false);

    // Ref for timer
    const [timeLeft, setTimeLeft] = useState(null);

    useEffect(() => {
        loadAttemptData();
    }, [attemptId]);

    const loadAttemptData = async () => {
        try {
            // 1. Fetch attempt history to find our attempt and its quizId
            const historyRes = await axiosInstance.get('/v1/student/quizzes/attempts/history');
            const history = historyRes.data?.data || [];
            const currentAttempt = history.find(a => a.id.toString() === attemptId);

            if (!currentAttempt) {
                setError('Attempt not found or unauthorized.');
                setLoading(false);
                return;
            }

            if (currentAttempt.status === 'COMPLETED') {
                navigate('/student/quizzes/history');
                return;
            }

            setAttempt(currentAttempt);

            // 2. Load cached answers if any
            if (currentAttempt.cachedAnswers) {
                const initialAnswers = currentAttempt.cachedAnswers.map(ans => ({
                    questionId: ans.questionId,
                    selectedOptionId: ans.selectedOptionId,
                    answerText: ans.answerText
                }));
                setAnswers(initialAnswers);
            }

            // 3. Fetch Quiz Details to get the questions
            const quizRes = await axiosInstance.get(`/v1/student/quizzes/${currentAttempt.quizId}/details`);
            const quizData = quizRes.data?.data;
            setQuiz(quizData);

            // Set initial timer based on timeLimit
            if (quizData && currentAttempt.startTime) {
                const start = new Date(currentAttempt.startTime).getTime();
                const limitMs = quizData.timeLimitMinutes * 60000;
                const now = new Date().getTime();
                const remaining = Math.max(0, Math.floor((start + limitMs - now) / 1000));
                setTimeLeft(remaining);
            }

        } catch (err) {
            console.error('Error loading attempt:', err);
            setError('Failed to load quiz data.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (timeLeft === null || timeLeft <= 0) return;
        const interval = setInterval(() => {
            setTimeLeft(prev => {
                if (prev <= 1) {
                    clearInterval(interval);
                    handleTimeUp();
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);
        return () => clearInterval(interval);
    }, [timeLeft]);

    // Handle autosave
    useEffect(() => {
        if (loading || !attempt) return;

        const autosaveTimer = setTimeout(() => {
            triggerAutosave();
        }, 15000); // Autosave every 15 secs if changes occur

        return () => clearTimeout(autosaveTimer);
    }, [answers, loading, attempt]);

    const triggerAutosave = () => {
        axiosInstance.post(`/v1/student/quizzes/attempts/${attemptId}/autosave`, {
            quizAttemptId: parseInt(attemptId),
            answers: answers
        }).catch(err => console.error("Autosave failed", err));
    };

    const handleTimeUp = () => {
        alert("Time is up! Your answers will be submitted automatically.");
        setTimeout(() => handleConfirmSubmit(), 100);
    };

    const handleOptionSelect = (questionId, optionId) => {
        setAnswers(prev => {
            const existing = prev.find(a => a.questionId === questionId);
            if (existing) {
                return prev.map(a => a.questionId === questionId ? { ...a, selectedOptionId: optionId } : a);
            }
            return [...prev, { questionId, selectedOptionId: optionId, answerText: null }];
        });
    };

    const handleTextChange = (questionId, text) => {
        setAnswers(prev => {
            const existing = prev.find(a => a.questionId === questionId);
            if (existing) {
                return prev.map(a => a.questionId === questionId ? { ...a, answerText: text } : a);
            }
            return [...prev, { questionId, selectedOptionId: null, answerText: text }];
        });
    };

    const hasAnswered = (questionId) => {
        const ans = answers.find(a => a.questionId === questionId);
        return ans && (ans.selectedOptionId || (ans.answerText && ans.answerText.trim() !== ''));
    };

    const isChecked = (questionId, optionId) => {
        const ans = answers.find(a => a.questionId === questionId);
        return ans?.selectedOptionId === optionId;
    };

    const getTextAnswer = (questionId) => {
        const ans = answers.find(a => a.questionId === questionId);
        return ans?.answerText || '';
    };

    const handleConfirmSubmit = async () => {
        setSubmitting(true);
        setShowSubmitModal(false);
        try {
            await axiosInstance.post(`/v1/student/quizzes/attempts/${attemptId}/submit`, {
                quizAttemptId: parseInt(attemptId),
                answers: answers
            });
            navigate('/student/quizzes/history');
        } catch (err) {
            console.error('Submit failed', err);
            alert('Failed to submit quiz. Please try again.');
            setSubmitting(false);
        }
    };

    const handleSubmitClick = () => {
        setShowSubmitModal(true);
    };

    const formatTime = (seconds) => {
        const m = Math.floor(seconds / 60);
        const s = seconds % 60;
        return `${m}:${s < 10 ? '0' : ''}${s}`;
    };

    if (loading) {
        return <div className="loader-container"><div className="spinner"></div><p>Preparing your exam...</p></div>;
    }

    if (error) {
        return <div className="quiz-container"><div className="error-message">⚠️ {error}</div></div>;
    }

    return (
        <div className="quiz-container attempt-layout">
            {/* Sticky Header with Timer */}
            <div className="attempt-header">
                <h2 className="attempt-title">{quiz?.title}</h2>
                <div className={`attempt-timer ${timeLeft < 60 ? 'timer-danger' : ''}`}>
                    ⏰ {timeLeft !== null ? formatTime(timeLeft) : '--:--'}
                </div>
            </div>

            <div className="attempt-content">
                {quiz?.questions?.map((q, index) => (
                    <div key={q.id} className="question-card" id={`question-${q.id}`}>
                        <h4 className="question-header">
                            <span className="question-number">Question {index + 1}</span>
                            <span className="question-points">({q.points} pts)</span>
                        </h4>
                        <div className="question-text">{q.content}</div>

                        <div className="options-container">
                            {['SINGLE_CHOICE', 'TRUE_FALSE', 'MULTIPLE_CHOICE'].includes(q.questionType) && q.options?.map(opt => (
                                <label key={opt.id} className={`option-label ${isChecked(q.id, opt.id) ? 'selected' : ''}`}>
                                    <input
                                        type="radio"
                                        name={`q-${q.id}`}
                                        value={opt.id}
                                        checked={isChecked(q.id, opt.id)}
                                        onChange={() => handleOptionSelect(q.id, opt.id)}
                                        className="radio-input"
                                    />
                                    <span className="option-text">{opt.content}</span>
                                </label>
                            ))}

                            {['FILL_BLANK', 'ESSAY'].includes(q.questionType) && (
                                <textarea
                                    className="text-answer-input"
                                    placeholder="Type your answer here..."
                                    value={getTextAnswer(q.id)}
                                    onChange={(e) => handleTextChange(q.id, e.target.value)}
                                    rows={q.questionType === 'ESSAY' ? 5 : 2}
                                />
                            )}
                        </div>
                    </div>
                ))}
            </div>

            <div className="attempt-footer">
                <div className="progress-info">
                    Answered: {answers.filter(a => a.selectedOptionId || (a.answerText && a.answerText.trim() !== '')).length} / {quiz?.questions?.length}
                </div>
                <button
                    className="btn-submit"
                    onClick={handleSubmitClick}
                    disabled={submitting}
                >
                    {submitting ? 'Submitting...' : 'Submit Quiz 🚀'}
                </button>
            </div>

            {/* Custom Submit Confirmation Modal */}
            {showSubmitModal && (
                <div className="modal-overlay">
                    <div className="modal-glass">
                        <h3>Hoàn thành bài thi?</h3>
                        <p>Bạn đã trả lời {answers.filter(a => a.selectedOptionId || (a.answerText && a.answerText.trim() !== '')).length} trên tổng số {quiz?.questions?.length} câu hỏi. Bạn có chắc chắn muốn nộp bài ngay lúc này không?</p>
                        <div className="modal-actions">
                            <button className="btn-cancel" onClick={() => setShowSubmitModal(false)}>Quay lại làm tiếp</button>
                            <button className="btn-confirm" onClick={handleConfirmSubmit}>Nộp Bài Ngay</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default QuizAttempt;
