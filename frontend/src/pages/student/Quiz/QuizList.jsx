import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './QuizStyles.css';

const QuizList = () => {
    const [quizzes, setQuizzes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchQuery, setSearchQuery] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        const delayDebounceFn = setTimeout(() => {
            fetchQuizzes(searchQuery);
        }, 500);
        return () => clearTimeout(delayDebounceFn);
    }, [searchQuery]);

    const fetchQuizzes = async (query = '') => {
        try {
            const url = query ? `/v1/student/quizzes?keyword=${encodeURIComponent(query)}` : '/v1/student/quizzes';
            const response = await axiosInstance.get(url);
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
            <h1 className="page-title">Danh sách bài Quiz</h1>

            <div className="search-container" style={{ marginBottom: '30px', display: 'flex', justifyContent: 'center' }}>
                <input
                    type="text"
                    placeholder="🔍 Tìm kiếm kỳ thi, bài quiz..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    style={{
                        padding: '14px 24px',
                        width: '100%',
                        maxWidth: '500px',
                        borderRadius: '30px',
                        border: '1px solid #e5e7eb',
                        outline: 'none',
                        boxShadow: '0 4px 15px rgba(0,0,0,0.05)',
                        fontSize: '1rem',
                        transition: 'all 0.3s ease'
                    }}
                    onFocus={(e) => {
                        e.target.style.boxShadow = '0 4px 20px rgba(59, 130, 246, 0.2)';
                        e.target.style.borderColor = '#3b82f6';
                    }}
                    onBlur={(e) => {
                        e.target.style.boxShadow = '0 4px 15px rgba(0,0,0,0.05)';
                        e.target.style.borderColor = '#e5e7eb';
                    }}
                />
            </div>

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
