import React, {useEffect, useState} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './QuizGradingList.css'; // Optional, but can use inline or generic classes

const QuizGradingList = () => {
    const { quizId } = useParams();
    const navigate = useNavigate();
    const [attempts, setAttempts] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchAttempts();
    }, [quizId]);

    const fetchAttempts = async () => {
        try {
            setLoading(true);
            const res = await axiosInstance.get(`/v1/quizzes/${quizId}/attempts`);
            if (res.data.success) {
                setAttempts(res.data.data.content);
            }
        } catch (error) {
            console.error("Failed to fetch attempts", error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="loading-spinner">Loading attempts...</div>;

    return (
        <div className="quiz-grading-list-container fade-in">
            <div className="grading-header">
                <h2>Quiz Submissions</h2>
                <button className="btn btn-secondary" onClick={() => navigate(-1)}>Back</button>
            </div>

            <div className="table-responsive mt-4">
                <table className="grading-table">
                    <thead>
                        <tr>
                            <th>Student ID</th>
                            <th>Student Name</th>
                            <th>Status</th>
                            <th>Score</th>
                            <th>Submitted At</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {attempts.length === 0 ? (
                            <tr>
                                <td colSpan="6" className="text-center">No submissions yet.</td>
                            </tr>
                        ) : (
                            attempts.map((attempt) => (
                                <tr key={attempt.id}>
                                    <td>{attempt.studentName || 'N/A'}</td>
                                    <td>{attempt.studentName || 'Student'}</td>
                                    <td>
                                        <span className={`status-badge ${attempt.status.toLowerCase()}`}>
                                            {attempt.status}
                                        </span>
                                    </td>
                                    <td>{attempt.score !== null ? attempt.score : 'N/A'}</td>
                                    <td>
                                        {attempt.endTime ? new Date(attempt.endTime).toLocaleString('en-GB') : 'N/A'}
                                    </td>
                                    <td>
                                        <button 
                                            className="btn btn-primary btn-sm"
                                            onClick={() => navigate(`/lecturer/quizzes/attempts/${attempt.id}/grading`)}
                                        >
                                            Grade
                                        </button>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default QuizGradingList;
