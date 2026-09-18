import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './QuizStyles.css';

const QuizReview = () => {
    const { attemptId } = useParams();
    const navigate = useNavigate();

    const [review, setReview] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchReview = async () => {
            try {
                const res = await axiosInstance.get(`/v1/student/quizzes/attempts/${attemptId}/review`);
                if (res.data?.success) {
                    setReview(res.data.data);
                } else {
                    setError('Failed to fetch review data.');
                }
            } catch (err) {
                console.error(err);
                const backendMsg = err.response?.data?.message;
                setError(backendMsg || 'Unable to load quiz results. Make sure your attempt is completed.');
            } finally {
                setLoading(false);
            }
        };

        fetchReview();
    }, [attemptId]);

    const isOptionSelected = (question, optionId) => {
        return question.studentAnswer?.selectedOptionId === optionId;
    };

    const getOptionClass = (question, option) => {
        const selected = isOptionSelected(question, option.id);
        const correct = option.isCorrect;

        if (selected && correct) return 'option-review correct';
        if (selected && !correct) return 'option-review wrong';
        if (!selected && correct) return 'option-review correct-unselected';
        return 'option-review'; // normal
    };

    if (loading) {
        return <div className="loader-container"><div className="spinner"></div><p>Loading your results...</p></div>;
    }

    if (error) {
        return <div className="quiz-container"><div className="error-message">⚠️ {error}</div></div>;
    }

    return (
        <div className="quiz-container attempt-layout review-layout">
            <div className="attempt-header review-header">
                <h2 className="attempt-title">{review?.quizTitle} - Review Results</h2>
                <div className="review-score">
                    🏆 Score Achieved: {review?.score}
                </div>
            </div>

            {review?.proctoringImageUrl && (
                <div className="proctoring-review" style={{marginBottom: '20px', padding: '15px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0', display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                    <h4 style={{marginTop: 0, color: '#334155', marginBottom: '10px'}}>📷 Proctoring Capture</h4>
                    <img src={review.proctoringImageUrl} alt="Student Proctoring Capture" style={{width: '100%', maxWidth: '320px', borderRadius: '8px', border: '2px solid #cbd5e1', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)'}} />
                    <p style={{fontSize: '0.85rem', color: '#64748b', marginTop: '10px', marginBottom: 0}}>Image captured at submission time to verify student identity.</p>
                </div>
            )}

            <div className="attempt-content">
                <div className="review-legend">
                    <span className="legend-item"><span className="legend-box correct"></span> Correctly Selected</span>
                    <span className="legend-item"><span className="legend-box wrong"></span> Incorrectly Selected</span>
                    <span className="legend-item"><span className="legend-box correct-unselected"></span> Correct Answer</span>
                </div>

                {review?.questions?.map((q, index) => (
                    <div key={q.id} className={`question-card ${q.studentAnswer?.isAwarded ? 'card-correct' : 'card-wrong'}`}>
                        <h4 className="question-header">
                            <span className="question-number">Question {index + 1}</span>
                            <span className="question-points">({q.points} pts) - {q.studentAnswer?.isAwarded ? '✅ CORRECT' : '❌ INCORRECT'}</span>
                        </h4>
                        <div className="question-text">{q.content}</div>

                        <div className="options-container">
                            {['SINGLE_CHOICE', 'TRUE_FALSE', 'MULTIPLE_CHOICE'].includes(q.questionType) && q.options?.map(opt => (
                                <div key={opt.id} className={getOptionClass(q, opt)}>
                                    <span className="option-text">{opt.content}</span>
                                    {isOptionSelected(q, opt.id) && <span className="selection-badge">Your choice</span>}
                                </div>
                            ))}

                            {['FILL_BLANK', 'ESSAY'].includes(q.questionType) && (
                                <div className="text-answer-review">
                                    <p><strong>Your answer:</strong></p>
                                    <div className="student-textarea-review">
                                        {q.studentAnswer?.answerText || <i style={{ color: '#94a3b8' }}>No answer provided</i>}
                                    </div>
                                    <p className="status-text">{q.studentAnswer?.isAwarded ? '✅ System marked as Correct' : '❌ Incorrect or pending grading'}</p>
                                </div>
                            )}
                        </div>
                    </div>
                ))}
            </div>

            <div className="attempt-footer">
                <button
                    className="btn-submit"
                    onClick={() => navigate('/student/quizzes/history')}
                >
                    ⬅ Back to history
                </button>
            </div>
        </div>
    );
};

export default QuizReview;
