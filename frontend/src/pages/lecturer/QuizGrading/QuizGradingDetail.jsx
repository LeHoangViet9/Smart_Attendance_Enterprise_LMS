import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './QuizGradingDetail.css';

const QuizGradingDetail = () => {
    const { attemptId } = useParams();
    const navigate = useNavigate();
    const [reviewData, setReviewData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [grades, setGrades] = useState({});
    const [aiLoadingState, setAiLoadingState] = useState({});

    useEffect(() => {
        fetchAttemptReview();
    }, [attemptId]);

    const fetchAttemptReview = async () => {
        try {
            setLoading(true);
            const res = await axiosInstance.get(`/v1/quizzes/attempts/${attemptId}/grading`);
            if (res.data.success) {
                setReviewData(res.data.data);
                // Initialize grades state
                const initialGrades = {};
                res.data.data.questions.forEach(q => {
                    if (q.questionType === 'ESSAY') {
                        initialGrades[q.studentAnswer?.id] = {
                            points: q.studentAnswer?.earnedPoints || 0,
                            feedback: q.studentAnswer?.feedback || ''
                        };
                    }
                });
                setGrades(initialGrades);
            }
        } catch (error) {
            console.error("Failed to fetch attempt details", error);
        } finally {
            setLoading(false);
        }
    };

    const handleGradeChange = (answerId, field, value) => {
        setGrades(prev => ({
            ...prev,
            [answerId]: {
                ...prev[answerId],
                [field]: value
            }
        }));
    };

    const handleAISuggest = async (answerId) => {
        try {
            setAiLoadingState(prev => ({ ...prev, [answerId]: true }));
            const res = await axiosInstance.post(`/v1/quizzes/attempts/${attemptId}/answers/${answerId}/ai-suggest`);
            if (res.data.success) {
                const { points, feedback } = res.data.data;
                handleGradeChange(answerId, 'points', points);
                handleGradeChange(answerId, 'feedback', feedback);
            } else {
                alert("AI fail: " + res.data.message);
            }
        } catch (error) {
            console.error("AI grading failed", error);
            alert("Lỗi kết nối tới AI Service!");
        } finally {
            setAiLoadingState(prev => ({ ...prev, [answerId]: false }));
        }
    };

    const handleSubmitGrades = async () => {
        try {
            const payload = {
                grades: Object.keys(grades).map(ansId => ({
                    studentAnswerId: Number(ansId),
                    points: Number(grades[ansId].points),
                    feedback: grades[ansId].feedback
                }))
            };
            
            const res = await axiosInstance.put(`/v1/quizzes/attempts/${attemptId}/grade`, payload);
            if (res.data.success) {
                alert("Grades saved successfully!");
                navigate(-1);
            }
        } catch (error) {
            console.error("Failed to submit grades", error);
            alert("Failed to submit grades.");
        }
    };

    if (loading) return <div className="loading-spinner">Loading details...</div>;
    if (!reviewData) return <div>Failed to load data.</div>;

    return (
        <div className="quiz-grading-detail fade-in">
            <div className="grading-header">
                <h2>Grading Attempt for {reviewData.quizTitle}</h2>
                <div className="actions">
                    <button className="btn btn-secondary" onClick={() => navigate(-1)}>Back</button>
                    <button className="btn btn-primary" onClick={handleSubmitGrades}>Save Grades</button>
                </div>
            </div>

            <div className="summary-card">
                <p><strong>Status:</strong> {reviewData.status}</p>
                <p><strong>Current Score:</strong> {reviewData.score}</p>
                {reviewData.proctoringImageUrl && (
                    <div>
                        <p><strong>Proctoring Verification:</strong></p>
                        <img src={reviewData.proctoringImageUrl} alt="Proctoring" width="150" />
                    </div>
                )}
            </div>

            <div className="questions-container">
                {reviewData.questions.map((q, index) => (
                    <div key={q.id} className="question-card">
                        <h4>Question {index + 1} ({q.points} pts)</h4>
                        <p className="q-content">{q.content}</p>
                        
                        {q.questionType !== 'ESSAY' ? (
                            <div className="auto-graded">
                                <p><strong>Student Answer:</strong> {q.studentAnswer?.answerText || 'N/A'}</p>
                                <p><strong>Points Earned:</strong> {q.studentAnswer?.earnedPoints || 0}</p>
                            </div>
                        ) : (
                            <div className="manual-graded">
                                <div className="student-essay-answer">
                                    <p><strong>Student Essay:</strong></p>
                                    <div className="essay-text">{q.studentAnswer?.answerText || 'No answer provided'}</div>
                                </div>
                                {q.studentAnswer?.id && (
                                    <div className="grading-inputs">
                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                            <h5 style={{ margin: 0, color: '#3b82f6' }}>Evaluation</h5>
                                            <button 
                                                className="btn btn-outline" 
                                                style={{ padding: '4px 12px', fontSize: '0.85em', display: 'flex', alignItems: 'center', gap: '5px' }}
                                                onClick={() => handleAISuggest(q.studentAnswer.id)}
                                                disabled={aiLoadingState[q.studentAnswer.id]}
                                            >
                                                {aiLoadingState[q.studentAnswer.id] ? '⏳ Thinking...' : '✨ Auto-Grade with AI'}
                                            </button>
                                        </div>
                                        <div className="form-group" style={{ marginTop: '10px' }}>
                                            <label>Points to Award (Max: {q.points}):</label>
                                            <input 
                                                type="number" 
                                                min="0" 
                                                max={q.points} 
                                                step="0.5"
                                                value={grades[q.studentAnswer.id]?.points ?? 0}
                                                onChange={(e) => handleGradeChange(q.studentAnswer.id, 'points', e.target.value)}
                                                className="form-control"
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label>Feedback:</label>
                                            <textarea 
                                                className="form-control"
                                                value={grades[q.studentAnswer.id]?.feedback ?? ''}
                                                onChange={(e) => handleGradeChange(q.studentAnswer.id, 'feedback', e.target.value)}
                                                placeholder="Provide feedback on the essay..."
                                                rows="3"
                                            />
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default QuizGradingDetail;
