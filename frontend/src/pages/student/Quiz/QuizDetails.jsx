import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './QuizStyles.css';

const QuizDetails = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [quiz, setQuiz] = useState(null);
    const [loading, setLoading] = useState(true);
    const [starting, setStarting] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchQuizDetails();
    }, [id]);

    const fetchQuizDetails = async () => {
        try {
            const response = await axiosInstance.get(`/v1/student/quizzes/${id}/details`);
            if (response.data && response.data.data) {
                setQuiz(response.data.data);
            }
        } catch (err) {
            console.error('Error fetching quiz details:', err);
            setError('Failed to load quiz details.');
        } finally {
            setLoading(false);
        }
    };

    const startAttempt = async () => {
        setStarting(true);
        try {
            const response = await axiosInstance.post(`/v1/student/quizzes/${id}/attempts`);
            if (response.data && response.data.data) {
                const attemptId = response.data.data.id;
                navigate(`/student/quizzes/attempts/${attemptId}`);
            }
        } catch (err) {
            console.error('Error starting attempt:', err);
            setError(err.response?.data?.message || 'Failed to start quiz attempt.');
            setStarting(false);
        }
    };

    if (loading) {
        return (
            <div className="loader-container">
                <div className="spinner"></div>
                <p>Loading quiz details...</p>
            </div>
        );
    }

    if (error || !quiz) {
        return <div className="quiz-container"><div className="error-message">⚠️ {error}</div></div>;
    }

    return (
        <div className="quiz-container" style={{ maxWidth: '800px', margin: '0 auto' }}>
            <button className="btn-secondary" onClick={() => navigate('/student/quizzes')} style={{ marginBottom: '1rem', background: 'none', border: 'none', color: '#6366f1', cursor: 'pointer', fontWeight: 'bold' }}>
                &larr; Back to Quizzes
            </button>

            <div className="quiz-card" style={{ padding: '2rem' }}>
                <h1 className="page-title" style={{ marginBottom: '1rem' }}>{quiz.title}</h1>
                <p className="quiz-description" style={{ fontSize: '1.1rem', marginBottom: '2rem' }}>
                    {quiz.description || 'No description provided for this quiz.'}
                </p>

                <div className="quiz-meta" style={{ display: 'flex', flexDirection: 'column', gap: '1rem', background: '#f8fafc', padding: '1.5rem', borderRadius: '12px' }}>
                    <div className="meta-item" style={{ fontSize: '1.1rem' }}>
                        <span>⏱️ <strong>Time Limit:</strong></span>
                        <span>{quiz.timeLimitMinutes} minutes</span>
                    </div>
                    <div className="meta-item" style={{ fontSize: '1.1rem' }}>
                        <span>📝 <strong>Questions:</strong></span>
                        <span>{quiz.questions ? quiz.questions.length : 0} questions</span>
                    </div>
                    <div className="meta-item" style={{ fontSize: '1.1rem' }}>
                        <span>📅 <strong>Deadline:</strong></span>
                        <span>{quiz.endTime ? new Date(quiz.endTime).toLocaleString() : 'No deadline'}</span>
                    </div>
                </div>

                <div style={{ marginTop: '2rem', textAlign: 'center' }}>
                    <button
                        className="btn-primary"
                        style={{ padding: '1rem 3rem', fontSize: '1.2rem', maxWidth: '300px' }}
                        onClick={startAttempt}
                        disabled={starting}
                    >
                        {starting ? 'Starting...' : 'Start Attempt'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default QuizDetails;
