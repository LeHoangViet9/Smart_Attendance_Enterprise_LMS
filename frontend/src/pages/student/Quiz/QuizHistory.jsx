import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './QuizStyles.css';

const QuizHistory = () => {
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        fetchHistory();
    }, []);

    const fetchHistory = async () => {
        try {
            const response = await axiosInstance.get('/v1/student/quizzes/attempts/history');
            if (response.data && response.data.data) {
                const historyData = response.data.data.content ? response.data.data.content : response.data.data;
                const sortedHistory = historyData.sort((a, b) => new Date(b.startTime) - new Date(a.startTime));
                setHistory(sortedHistory);
            }
        } catch (err) {
            console.error('Error fetching history:', err);
            setError('Failed to load attempt history.');
        } finally {
            setLoading(false);
        }
    };

    const calculateDuration = (start, end) => {
        if (!start || !end) return '-';
        const s = new Date(start).getTime();
        const e = new Date(end).getTime();
        const diff = Math.floor((e - s) / 60000); // minutes
        return `${diff} mins`;
    };

    const getStatusBadge = (status) => {
        switch (status) {
            case 'COMPLETED':
                return <span style={{ background: '#dcfce7', color: '#166534', padding: '0.3rem 0.6rem', borderRadius: '12px', fontSize: '0.8rem', fontWeight: 'bold' }}>Completed</span>;
            case 'IN_PROGRESS':
                return <span style={{ background: '#fef9c3', color: '#854d0e', padding: '0.3rem 0.6rem', borderRadius: '12px', fontSize: '0.8rem', fontWeight: 'bold' }}>In Progress</span>;
            default:
                return <span style={{ background: '#f3f4f6', color: '#374151', padding: '0.3rem 0.6rem', borderRadius: '12px', fontSize: '0.8rem', fontWeight: 'bold' }}>{status}</span>;
        }
    };

    if (loading) {
        return <div className="loader-container"><div className="spinner"></div><p>Loading your history...</p></div>;
    }

    return (
        <div className="quiz-container" style={{ maxWidth: '1000px', margin: '0 auto' }}>
            <h1 className="page-title">My Attempt History</h1>

            {error && <div className="error-message">⚠️ {error}</div>}

            {history.length === 0 && !error ? (
                <div style={{ textAlign: 'center', padding: '4rem', background: 'white', borderRadius: '16px', color: '#6b7280' }}>
                    <h3>No quiz attempts found.</h3>
                    <p>You haven't taken any quizzes yet.</p>
                    <button className="btn-primary" style={{ marginTop: '1rem', width: 'auto' }} onClick={() => navigate('/student/quizzes')}>Explore Quizzes</button>
                </div>
            ) : (
                <div className="history-table-container" style={{ background: 'white', borderRadius: '16px', overflow: 'hidden', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                        <thead style={{ background: '#f8fafc', borderBottom: '1px solid #e2e8f0' }}>
                            <tr>
                                <th style={{ padding: '1.2rem', color: '#475569', fontWeight: '600' }}>Attempt ID</th>
                                <th style={{ padding: '1.2rem', color: '#475569', fontWeight: '600' }}>Date</th>
                                <th style={{ padding: '1.2rem', color: '#475569', fontWeight: '600' }}>Duration</th>
                                <th style={{ padding: '1.2rem', color: '#475569', fontWeight: '600' }}>Score</th>
                                <th style={{ padding: '1.2rem', color: '#475569', fontWeight: '600' }}>Status</th>
                                <th style={{ padding: '1.2rem', color: '#475569', fontWeight: '600' }}>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {history.map((attempt) => (
                                <tr key={attempt.id} style={{ borderBottom: '1px solid #f1f5f9', transition: 'background 0.2s ease' }} onMouseOver={e => e.currentTarget.style.background = '#f8fafc'} onMouseOut={e => e.currentTarget.style.background = 'white'}>
                                    <td style={{ padding: '1.2rem', fontWeight: '500' }}>#{attempt.id}</td>
                                    <td style={{ padding: '1.2rem', color: '#64748b' }}>{new Date(attempt.startTime).toLocaleString()}</td>
                                    <td style={{ padding: '1.2rem', color: '#64748b' }}>{calculateDuration(attempt.startTime, attempt.endTime)}</td>
                                    <td style={{ padding: '1.2rem', fontWeight: 'bold', color: attempt.score >= 5 ? '#16a34a' : (attempt.score ? '#dc2626' : '#94a3b8') }}>
                                        {attempt.score !== null ? attempt.score : 'Pending'}
                                    </td>
                                    <td style={{ padding: '1.2rem' }}>{getStatusBadge(attempt.status)}</td>
                                    <td style={{ padding: '1.2rem' }}>
                                        <div className="history-actions">
                                            {attempt.status === 'IN_PROGRESS' ? (
                                                <button
                                                    className="btn-resume"
                                                    onClick={() => navigate(`/student/quizzes/attempts/${attempt.id}`)}
                                                >
                                                    Tiếp Tục Thi
                                                </button>
                                            ) : attempt.status === 'COMPLETED' ? (
                                                <button
                                                    className="btn-resume"
                                                    style={{ background: '#3b82f6', color: 'white' }}
                                                    onClick={() => navigate(`/student/quizzes/attempts/${attempt.id}/review`)}
                                                >
                                                    Xem Lại Kết Quả
                                                </button>
                                            ) : null}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default QuizHistory;
