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

    const [userRole, setUserRole] = useState('STUDENT');
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [creating, setCreating] = useState(false);
    const [createError, setCreateError] = useState(null);
    const [newQuiz, setNewQuiz] = useState({
        title: '',
        description: '',
        timeLimitMinutes: 30,
        startTime: '',
        endTime: ''
    });

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        let currentRole = 'STUDENT';
        if (storedUser) {
            try {
                const parsed = JSON.parse(storedUser);
                if (parsed.role) {
                    currentRole = parsed.role;
                    setUserRole(currentRole);
                }
            } catch (e) {
                console.error(e);
            }
        }
        const delayDebounceFn = setTimeout(() => {
            fetchQuizzes(searchQuery, currentRole);
        }, 500);
        return () => clearTimeout(delayDebounceFn);
    }, [searchQuery]);

    const fetchQuizzes = async (query = '', role = userRole) => {
        try {
            // Admin/Lecturer fetches all quizzes for management, Student fetches available quizzes via student endpoint
            const baseEndpoint = (role === 'ADMIN' || role === 'LECTURER') ? '/v1/quizzes' : '/v1/student/quizzes';
            const url = query ? `${baseEndpoint}?keyword=${encodeURIComponent(query)}` : baseEndpoint;
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

    const handleCreateQuiz = async (e) => {
        e.preventDefault();
        setCreating(true);
        setCreateError(null);
        try {
            const payload = {
                ...newQuiz,
                timeLimitMinutes: Number(newQuiz.timeLimitMinutes) || 30
            };
            if (newQuiz.startTime) payload.startTime = new Date(newQuiz.startTime).toISOString();
            if (newQuiz.endTime) payload.endTime = new Date(newQuiz.endTime).toISOString();
            else delete payload.endTime; // Allow unlimited time if empty

            await axiosInstance.post('/v1/quizzes', payload);
            setShowCreateModal(false);
            setNewQuiz({ title: '', description: '', timeLimitMinutes: 30, startTime: '', endTime: '' });
            fetchQuizzes(searchQuery, userRole);
        } catch (err) {
            console.error('Error creating quiz:', err);
            setCreateError(err.response?.data?.message || 'Error creating Quiz');
        } finally {
            setCreating(false);
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
            <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h1 className="page-title" style={{ margin: 0 }}>
                    {userRole === 'STUDENT' ? 'Quizzes (Exams)' : 'Exam Bank Design'}
                </h1>
                {(userRole === 'LECTURER' || userRole === 'ADMIN') && (
                    <button
                        className="btn-primary"
                        style={{ padding: '0.75rem 1.5rem', borderRadius: '8px', fontSize: '1rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}
                        onClick={() => setShowCreateModal(true)}
                    >
                        ➕ Create New Quiz
                    </button>
                )}
            </div>

            <div className="search-container" style={{ marginBottom: '30px', display: 'flex', justifyContent: 'center' }}>
                <input
                    type="text"
                    placeholder="🔍 Search for exams, quizzes..."
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
                                onClick={() => {
                                    if (userRole === 'LECTURER' || userRole === 'ADMIN') {
                                        navigate(`/student/quizzes/manage/${quiz.id}`);
                                    } else {
                                        navigate(`/student/quizzes/${quiz.id}`);
                                    }
                                }}
                            >
                                {userRole === 'LECTURER' || userRole === 'ADMIN' ? 'Manage Exam' : 'View Details'}
                            </button>
                        </div>
                    ))}
                </div>
            )}

            {/* Create Quiz Modal */}
            {showCreateModal && (
                <div className="modal-overlay" onClick={() => setShowCreateModal(false)} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 9999 }}>
                    <div className="modal-glass" style={{ maxWidth: '540px', width: '100%', backgroundColor: 'white', padding: '2rem', borderRadius: '12px', boxShadow: '0 10px 25px rgba(0,0,0,0.1)' }} onClick={(e) => e.stopPropagation()}>
                        <h3 style={{ fontSize: '1.5rem', color: '#1e293b', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>📝 Create New Quiz</h3>
                        <p style={{ color: '#64748b', marginBottom: '1.5rem', fontSize: '0.9rem' }}>Enter basic information for the online test</p>

                        {createError && (
                            <div className="error-message" style={{ marginBottom: '1rem', padding: '0.75rem', backgroundColor: '#fef2f2', color: '#b91c1c', borderRadius: '6px' }}>
                                <span>⚠️</span> {createError}
                            </div>
                        )}

                        <form onSubmit={handleCreateQuiz}>
                            <div className="modal-form-group" style={{ marginBottom: '1rem' }}>
                                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600, color: '#374151' }}>Quiz Title *</label>
                                <input
                                    type="text"
                                    required
                                    style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '6px' }}
                                    placeholder="e.g. Java Midterm Exam"
                                    value={newQuiz.title}
                                    onChange={(e) => setNewQuiz({ ...newQuiz, title: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-group" style={{ marginBottom: '1rem' }}>
                                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600, color: '#374151' }}>Description</label>
                                <textarea
                                    rows="3"
                                    style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '6px' }}
                                    placeholder="Content, requirements, scope of the test..."
                                    value={newQuiz.description}
                                    onChange={(e) => setNewQuiz({ ...newQuiz, description: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-row" style={{ display: 'flex', gap: '1rem', marginBottom: '1rem' }}>
                                <div className="modal-form-group" style={{ flex: 1 }}>
                                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600, color: '#374151' }}>Time limit (Minutes) *</label>
                                    <input
                                        type="number"
                                        min="1"
                                        required
                                        style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '6px' }}
                                        value={newQuiz.timeLimitMinutes}
                                        onChange={(e) => setNewQuiz({ ...newQuiz, timeLimitMinutes: e.target.value })}
                                    />
                                </div>
                            </div>

                            <div className="modal-form-row" style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem' }}>
                                <div className="modal-form-group" style={{ flex: 1 }}>
                                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600, color: '#374151' }}>Start time (Optional)</label>
                                    <input
                                        type="datetime-local"
                                        style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '6px' }}
                                        value={newQuiz.startTime}
                                        onChange={(e) => setNewQuiz({ ...newQuiz, startTime: e.target.value })}
                                    />
                                </div>
                                <div className="modal-form-group" style={{ flex: 1 }}>
                                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600, color: '#374151' }}>End time (Optional)</label>
                                    <input
                                        type="datetime-local"
                                        style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '6px' }}
                                        value={newQuiz.endTime}
                                        onChange={(e) => setNewQuiz({ ...newQuiz, endTime: e.target.value })}
                                    />
                                </div>
                            </div>

                            <div className="modal-actions" style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem' }}>
                                <button
                                    type="button"
                                    style={{ padding: '0.75rem 1.5rem', borderRadius: '6px', backgroundColor: '#f1f5f9', color: '#475569', fontWeight: 600, border: 'none', cursor: 'pointer' }}
                                    onClick={() => setShowCreateModal(false)}
                                    disabled={creating}
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    style={{ padding: '0.75rem 1.5rem', borderRadius: '6px', backgroundColor: '#3b82f6', color: '#ffffff', fontWeight: 600, border: 'none', cursor: 'pointer' }}
                                    disabled={creating}
                                >
                                    {creating ? 'Creating...' : 'Create Quiz'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default QuizList;
