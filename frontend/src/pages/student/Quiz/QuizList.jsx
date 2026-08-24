import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './QuizStyles.css';

const QuizList = () => {
    const [quizzes, setQuizzes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        fetchQuizzes();
    }, []);

    const fetchQuizzes = async () => {
        try {
            const response = await axiosInstance.get('/v1/student/quizzes');
            if (response.data && response.data.data && response.data.data.content) {
                setQuizzes(response.data.data.content);
            }
        } catch (err) {
            console.error('Error fetching quizzes:', err);
            setError('Failed to load quizzes. Please try again later.');
        } finally {
            setLoading(false);
        }
    };

    const calculateRemainingTime = (endTime) => {
        if (!endTime) return 'No deadline';
        const deadline = new Date(endTime).getTime();
        const now = new Date().getTime();
        const diff = deadline - now;
        if (diff <= 0) return 'Expired';
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        return days > 0 ? `${days} days left` : 'Ends today';
    };

    if (loading) {
        return (
            <div className="loader-container">
                <div className="spinner"></div>
                <p>Loading available quizzes...</p>
            </div>
        );
    }

    return (
        <div className="quiz-container">
            <h1 className="page-title">Available Quizzes</h1>

            {error && <div className="error-message">⚠️ {error}</div>}

            {quizzes.length === 0 && !error ? (
                <div style={{ textAlign: 'center', padding: '3rem', color: '#6b7280' }}>
                    <h3>No quizzes available right now.</h3>
                    <p>Check back later or contact your instructor.</p>
                </div>
            ) : (
                <div className="quiz-grid">
                    {quizzes.map((quiz) => (
                        <div key={quiz.id} className="quiz-card">
                            <h3 className="quiz-title">{quiz.title}</h3>
                            <p className="quiz-description">{quiz.description || 'No description provided.'}</p>

                            <div className="quiz-meta">
                                <div className="meta-item">
                                    ⏱️ {quiz.timeLimitMinutes} mins
                                </div>
                                <div className="meta-item">
                                    📅 {calculateRemainingTime(quiz.endTime)}
                                </div>
                            </div>

                            <button
                                className="btn-primary"
                                onClick={() => navigate(`/student/quizzes/${quiz.id}`)}
                            >
                                View Details
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default QuizList;
