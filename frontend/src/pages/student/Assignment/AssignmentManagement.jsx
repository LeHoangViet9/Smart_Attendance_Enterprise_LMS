import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './AssignmentStyles.css';

const AssignmentManagement = () => {
    const navigate = useNavigate();

    const [assignments, setAssignments] = useState([]);
    const [selectedAssignmentId, setSelectedAssignmentId] = useState('');
    const [submissions, setSubmissions] = useState([]);
    const [loadingAssignments, setLoadingAssignments] = useState(true);
    const [loadingSubmissions, setLoadingSubmissions] = useState(false);
    const [error, setError] = useState(null);

    const [userRole, setUserRole] = useState('STUDENT');

    // Grading Modal State
    const [gradingSubmission, setGradingSubmission] = useState(null);
    const [scoreInput, setScoreInput] = useState('');
    const [feedbackInput, setFeedbackInput] = useState('');
    const [modalError, setModalError] = useState(null);
    const [savingGrade, setSavingGrade] = useState(false);

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        let currentRole = 'STUDENT';
        if (storedUser) {
            try {
                const parsed = JSON.parse(storedUser);
                if (parsed.role) {
                    setUserRole(parsed.role);
                    currentRole = parsed.role;
                }
            } catch (e) {
                console.error(e);
            }
        }
        fetchAssignments(currentRole);
    }, []);

    const fetchAssignments = async (role) => {
        setLoadingAssignments(true);
        try {
            const res = await axiosInstance.get('/v1/assignments');
            if (res.data && res.data.data) {
                let fetchedAssignments = res.data.data;

                // Admin chỉ xem đề thi quan trọng
                if (role === 'ADMIN') {
                    fetchedAssignments = fetchedAssignments.filter(a => a.isExam === true);
                }

                setAssignments(fetchedAssignments);
                if (fetchedAssignments.length > 0) {
                    const params = new URLSearchParams(window.location.search);
                    const queryId = params.get('id');
                    const targetId = fetchedAssignments.find(a => a.id === queryId) ? queryId : fetchedAssignments[0].id;

                    setSelectedAssignmentId(targetId);
                    fetchSubmissions(targetId);
                }
            }
        } catch (err) {
            console.error('Error fetching assignments for management:', err);
            setError('Cannot load assignments list.');
        } finally {
            setLoadingAssignments(false);
        }
    };

    const fetchSubmissions = async (assignmentId) => {
        if (!assignmentId) return;
        setLoadingSubmissions(true);
        setError(null);
        try {
            const res = await axiosInstance.get(`/v1/assignments/${assignmentId}/submissions`);
            if (res.data && res.data.data) {
                setSubmissions(res.data.data);
            } else {
                setSubmissions([]);
            }
        } catch (err) {
            console.error('Error fetching submissions:', err);
            setError('Cannot load submissions for this assignment.');
            setSubmissions([]);
        } finally {
            setLoadingSubmissions(false);
        }
    };

    const handleSelectAssignment = (e) => {
        const id = e.target.value;
        setSelectedAssignmentId(id);
        fetchSubmissions(id);
    };

    const handleTogglePublish = async () => {
        const currentAssignment = assignments.find(a => a.id === selectedAssignmentId);
        if (!currentAssignment) return;

        const newStatus = !currentAssignment.isPublished;
        if (!window.confirm(`Are you sure you want to ${newStatus ? 'PUBLISH' : 'UNPUBLISH'} this assignment?`)) return;

        try {
            await axiosInstance.put(`/v1/assignments/${currentAssignment.id}`, {
                isPublished: newStatus
            });

            // Cập nhật local state
            setAssignments(prev => prev.map(a =>
                a.id === currentAssignment.id ? { ...a, isPublished: newStatus } : a
            ));
            alert('Visibility status updated successfully!');
        } catch (err) {
            console.error('Error toggling publish:', err);
            alert('Error updating status!');
        }
    };

    const openGradeModal = (submission) => {
        setGradingSubmission(submission);
        setModalError(null);
        setScoreInput(submission.score !== null && submission.score !== undefined ? submission.score : '');
        setFeedbackInput(submission.feedback || '');
    };

    const handleSaveGrade = async (e) => {
        e.preventDefault();
        if (!gradingSubmission) return;

        setSavingGrade(true);
        setModalError(null);
        try {
            const currentAssignment = assignments.find(a => a.id === selectedAssignmentId);
            const maxScore = currentAssignment?.maxScore || 10;
            const scoreNum = Number(scoreInput);

            if (isNaN(scoreNum) || scoreNum < 0 || scoreNum > maxScore) {
                setModalError(`Score must be a number between 0 and ${maxScore}`);
                setSavingGrade(false);
                return;
            }

            const res = await axiosInstance.post(`/v1/submissions/${gradingSubmission.id}/grade`, {
                score: scoreNum,
                feedback: feedbackInput
            });

            if (res.data && res.data.data) {
                // Update local submissions list
                setSubmissions(prev =>
                    prev.map(sub => (sub.id === gradingSubmission.id ? res.data.data : sub))
                );
                setGradingSubmission(null);
            }
        } catch (err) {
            console.error('Error grading submission:', err);
            setModalError(err.response?.data?.message || 'Error saving score');
        } finally {
            setSavingGrade(false);
        }
    };

    const formatDateTime = (dateStr) => {
        if (!dateStr) return 'N/A';
        const d = new Date(dateStr);
        return d.toLocaleString('vi-VN', {
            hour: '2-digit',
            minute: '2-digit',
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    };

    const currentAssignment = assignments.find(a => a.id === selectedAssignmentId);
    const gradedCount = submissions.filter(s => s.isGraded).length;
    const pendingCount = submissions.length - gradedCount;

    return (
        <div className="assignment-container">
            {/* Header */}
            <div className="assignment-header">
                <div className="assignment-header-info">
                    <h1>
                        <span>👩‍🏫</span> Manage & Grade Submissions
                    </h1>
                    <p>View student submissions, evaluate scores and send feedback</p>
                </div>
                <button
                    className="btn-card-action secondary"
                    style={{ width: 'auto', padding: '0.75rem 1.5rem' }}
                    onClick={() => navigate('/student/assignments')}
                >
                    ⬅ Back to assignments
                </button>
            </div>

            {error && <div className="error-message"><span>⚠️</span> {error}</div>}

            {/* Assignment Selector & Summary */}
            <div className="filter-bar" style={{ alignItems: 'center' }}>
                <div style={{ flex: 1, minWidth: '280px' }}>
                    <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 700, color: '#374151', marginBottom: '0.4rem' }}>
                        Select Assignment to Grade:
                    </label>
                    <select
                        className="modal-form-control"
                        style={{ background: 'white' }}
                        value={selectedAssignmentId}
                        onChange={handleSelectAssignment}
                        disabled={loadingAssignments}
                    >
                        {assignments.map(a => (
                            <option key={a.id} value={a.id}>
                                {a.title} ({a.isExpired ? 'Expired' : 'Open'})
                            </option>
                        ))}
                    </select>

                    {currentAssignment && (
                        <div style={{ marginTop: '0.8rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            <label className="switch" style={{ transform: 'scale(0.8)' }}>
                                <input
                                    type="checkbox"
                                    checked={currentAssignment.isPublished !== false}
                                    onChange={handleTogglePublish}
                                />
                                <span className="slider round"></span>
                            </label>
                            <span style={{ fontSize: '0.85rem', fontWeight: 600, color: currentAssignment.isPublished !== false ? '#10b981' : '#ef4444' }}>
                                {currentAssignment.isPublished !== false ? 'Published (Public)' : 'Hidden (Unpublished)'}
                            </span>
                        </div>
                    )}
                </div>

                {currentAssignment && (
                    <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
                        <div className="stat-card" style={{ padding: '0.75rem 1.25rem', minWidth: '130px' }}>
                            <div className="stat-content">
                                <div className="stat-value" style={{ fontSize: '1.3rem' }}>{submissions.length}</div>
                                <div className="stat-label">Total Submissions</div>
                            </div>
                        </div>
                        <div className="stat-card" style={{ padding: '0.75rem 1.25rem', minWidth: '130px' }}>
                            <div className="stat-content">
                                <div className="stat-value" style={{ fontSize: '1.3rem', color: '#10b981' }}>{gradedCount}</div>
                                <div className="stat-label">Graded</div>
                            </div>
                        </div>
                        <div className="stat-card" style={{ padding: '0.75rem 1.25rem', minWidth: '130px' }}>
                            <div className="stat-content">
                                <div className="stat-value" style={{ fontSize: '1.3rem', color: '#f59e0b' }}>{pendingCount}</div>
                                <div className="stat-label">Pending Grading</div>
                            </div>
                        </div>
                    </div>
                )}
            </div>

            {/* Submissions Table */}
            {loadingSubmissions ? (
                <div className="loader-container">
                    <div className="spinner"></div>
                    <p>Loading submissions list...</p>
                </div>
            ) : submissions.length === 0 ? (
                <div className="details-main" style={{ textAlign: 'center', padding: '4rem 2rem', marginTop: '1.5rem' }}>
                    <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📭</div>
                    <h3 style={{ fontSize: '1.3rem', color: '#1f2937', margin: '0 0 0.5rem' }}>
                        No students have submitted yet
                    </h3>
                    <p style={{ color: '#6b7280', margin: 0 }}>
                        Student submissions for this assignment will appear here once sent.
                    </p>
                </div>
            ) : (
                <div className="management-table-card">
                    <table className="lms-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Student ID</th>
                                <th>Submission Time</th>
                                <th>Submission File</th>
                                <th>Status</th>
                                <th>Score</th>
                                <th>Feedback</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {submissions.map((sub, idx) => (
                                <tr key={sub.id}>
                                    <td><strong>{idx + 1}</strong></td>
                                    <td>
                                        <div style={{ fontWeight: 600, color: '#1e293b' }}>
                                            {sub.studentId?.substring(0, 8)}...
                                        </div>
                                    </td>
                                    <td>
                                        <div>{formatDateTime(sub.submittedAt)}</div>
                                        <span className={`status-badge ${sub.isLate ? 'late' : 'graded'}`} style={{ marginTop: '4px' }}>
                                            {sub.isLate ? 'Late' : 'On Time'}
                                        </span>
                                    </td>
                                    <td>
                                        <a
                                            href={sub.fileUrl}
                                            target="_blank"
                                            rel="noreferrer"
                                            style={{ color: '#6366f1', textDecoration: 'none', fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '4px' }}
                                        >
                                            📎 View File
                                        </a>
                                    </td>
                                    <td>
                                        <span className={`status-badge ${sub.isGraded ? 'graded' : 'open'}`}>
                                            {sub.isGraded ? '✅ Graded' : '⏳ Pending'}
                                        </span>
                                    </td>
                                    <td>
                                        {sub.isGraded ? (
                                            <span style={{ fontWeight: 800, color: '#10b981', fontSize: '1.1rem' }}>
                                                {sub.score} / {currentAssignment?.maxScore || 10}
                                            </span>
                                        ) : (
                                            <span style={{ color: '#9ca3af' }}>--</span>
                                        )}
                                    </td>
                                    <td>
                                        <div style={{ maxWidth: '200px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', color: '#64748b', fontSize: '0.85rem' }}>
                                            {sub.feedback || 'No feedback yet'}
                                        </div>
                                    </td>
                                    <td>
                                        {userRole === 'ADMIN' ? (
                                            <span style={{ fontSize: '0.85rem', color: '#9ca3af', fontStyle: 'italic' }}>Supervisor</span>
                                        ) : (
                                            <button
                                                className={`btn-action-small ${sub.isGraded ? 'success' : 'primary'}`}
                                                onClick={() => openGradeModal(sub)}
                                            >
                                                {sub.isGraded ? 'Edit Score' : 'Grade'}
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Grading Dialog Modal */}
            {gradingSubmission && (
                <div className="modal-overlay" onClick={() => setGradingSubmission(null)}>
                    <div className="modal-glass" style={{ maxWidth: '500px' }} onClick={(e) => e.stopPropagation()}>
                        <h3>📝 Grade Submission</h3>
                        <p>
                            Student ID: <strong>{gradingSubmission.studentId}</strong>
                        </p>
                        {modalError && (
                            <div className="error-message" style={{ marginBottom: '1rem', padding: '0.75rem' }}>
                                <span>⚠️</span> {modalError}
                            </div>
                        )}

                        <form onSubmit={handleSaveGrade}>
                            <div className="modal-form-group">
                                <label>
                                    Score (Scale: 0 - {currentAssignment?.maxScore || 10}) *
                                </label>
                                <input
                                    type="number"
                                    step="0.1"
                                    min="0"
                                    max={currentAssignment?.maxScore || 10}
                                    required
                                    className="modal-form-control"
                                    placeholder="e.g. 8.5"
                                    value={scoreInput}
                                    onChange={(e) => setScoreInput(e.target.value)}
                                />
                            </div>

                            <div className="modal-form-group">
                                <label>Lecturer Feedback / Comments</label>
                                <textarea
                                    rows="4"
                                    className="modal-form-control"
                                    placeholder="Detailed feedback notes for student improvement..."
                                    value={feedbackInput}
                                    onChange={(e) => setFeedbackInput(e.target.value)}
                                />
                            </div>

                            <div className="modal-actions" style={{ marginTop: '1.5rem' }}>
                                <button
                                    type="button"
                                    className="btn-cancel"
                                    onClick={() => setGradingSubmission(null)}
                                    disabled={savingGrade}
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="btn-confirm"
                                    disabled={savingGrade}
                                >
                                    {savingGrade ? 'Saving...' : 'Save grading result'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AssignmentManagement;
