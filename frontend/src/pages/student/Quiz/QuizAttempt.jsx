import React, { useState, useEffect } from 'react';
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
    const [showTimeUpModal, setShowTimeUpModal] = useState(false);
    const [submitError, setSubmitError] = useState(null);

    // Ref for timer
    const [timeLeft, setTimeLeft] = useState(null);

    useEffect(() => {
        loadAttemptData();
    }, [attemptId]);

    const loadAttemptData = async () => {
        try {
            // 1. Fetch attempt history to find our attempt and its quizId
            const historyRes = await axiosInstance.get('/v1/student/quizzes/attempts/history');
            const history = historyRes.data?.data?.content || [];
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
        setShowTimeUpModal(true);
        setShowSubmitModal(false);
        setTimeout(() => {
            executeSubmit();
        }, 1500);
    };

    const executeSubmit = async () => {
        setSubmitting(true);
        setSubmitError(null);
        try {
            await axiosInstance.post(`/v1/student/quizzes/attempts/${attemptId}/submit`, {
                quizAttemptId: parseInt(attemptId),
                answers: answers
            });
            navigate('/student/quizzes/history');
        } catch (err) {
            console.error('Submit failed', err);
            setSubmitError('Server error during submission. Please retry.');
            setSubmitting(false);
        }
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



    const isChecked = (questionId, optionId) => {
        const ans = answers.find(a => a.questionId === questionId);
        return ans?.selectedOptionId === optionId;
    };

    const getTextAnswer = (questionId) => {
        const ans = answers.find(a => a.questionId === questionId);
        return ans?.answerText || '';
    };

    const handleConfirmSubmit = async () => {
        setShowSubmitModal(false);
        await executeSubmit();
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
                <div className="modal-overlay" onClick={() => setShowSubmitModal(false)}>
                    <div className="modal-glass" onClick={(e) => e.stopPropagation()}>
                        <h3>Finish Quiz?</h3>
                        <p>You have answered {answers.filter(a => a.selectedOptionId || (a.answerText && a.answerText.trim() !== '')).length} out of {quiz?.questions?.length} questions. Are you sure you want to submit your quiz right now?</p>
                        <div className="modal-actions">
                            <button className="btn-cancel" onClick={() => setShowSubmitModal(false)} disabled={submitting}>Continue taking quiz</button>
                            <button className="btn-confirm" onClick={handleConfirmSubmit} disabled={submitting}>
                                {submitting ? 'Submitting...' : 'Submit Now'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Custom Time Is Up Modal (Auto-Submitting) */}
            {showTimeUpModal && (
                <div className="modal-overlay">
                    <div className="modal-glass" style={{ maxWidth: '420px', textAlign: 'center' }}>
                        <div style={{ fontSize: '3rem', marginBottom: '0.75rem', animation: 'pulse 1s infinite alternate' }}>⏰</div>
                        <h3 style={{ color: '#dc2626', margin: '0 0 0.5rem 0' }}>Time's Up!</h3>
                        <p style={{ color: '#4b5563', margin: '0 0 1.5rem 0', lineHeight: 1.5 }}>
                            Your time limit has been reached. The system is automatically processing and submitting your answers...
                        </p>
                        {submitError ? (
                            <div>
                                <div className="error-message" style={{ marginBottom: '1rem', textAlign: 'left' }}>
                                    <span>⚠️</span> {submitError}
                                </div>
                                <button className="btn-confirm" onClick={executeSubmit}>
                                    Retry submission
                                </button>
                            </div>
                        ) : (
                            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.75rem' }}>
                                <div className="spinner"></div>
                                <span style={{ fontSize: '0.9rem', color: '#6b7280', fontWeight: 600 }}>Submitting automatically...</span>
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default QuizAttempt;
