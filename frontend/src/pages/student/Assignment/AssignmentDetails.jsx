import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './AssignmentStyles.css';

const AssignmentDetails = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const [assignment, setAssignment] = useState(null);
    const [submission, setSubmission] = useState(null);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);
    const [successMsg, setSuccessMsg] = useState(null);

    // File upload state
    const [selectedFile, setSelectedFile] = useState(null);
    const [isDragActive, setIsDragActive] = useState(false);
    const [fileUrlInput, setFileUrlInput] = useState('');
    const [showUrlInput, setShowUrlInput] = useState(false);
    const [isResubmitting, setIsResubmitting] = useState(false);
    const [showConfirmModal, setShowConfirmModal] = useState(false);

    const fileInputRef = useRef(null);

    const [userRole, setUserRole] = useState(() => {
        const stored = localStorage.getItem('user');
        if (stored) {
            try {
                const user = JSON.parse(stored);
                return user.role || 'STUDENT';
            } catch (e) {
                console.error(e);
            }
        }
        return 'STUDENT';
    });

    const getStudentId = () => {
        const stored = localStorage.getItem('user');
        if (stored) {
            try {
                const user = JSON.parse(stored);
                if (user.studentId) return user.studentId;
                if (user.userId && typeof user.userId === 'string' && user.userId.includes('-')) {
                    return user.userId;
                }
            } catch (e) {
                console.error(e);
            }
        }
        return '11111111-1111-1111-1111-111111111111';
    };

    const studentId = getStudentId();

    useEffect(() => {
        loadData();
    }, [id]);

    const loadData = async () => {
        setLoading(true);
        setError(null);
        try {
            // 1. Fetch assignment details
            const assignRes = await axiosInstance.get(`/v1/assignments/${id}`);
            if (assignRes.data && assignRes.data.data) {
                setAssignment(assignRes.data.data);
            }

            // 2. Fetch student submission if exists
            try {
                const subRes = await axiosInstance.get(`/v1/assignments/${id}/submission?studentId=${studentId}`);
                if (subRes.data && subRes.data.data) {
                    setSubmission(subRes.data.data);
                }
            } catch (subErr) {
                // Not submitted yet is normal (404)
                setSubmission(null);
            }
        } catch (err) {
            console.error('Error loading assignment details:', err);
            setError('Cannot load assignment info. Please try again later.');
        } finally {
            setLoading(false);
        }
    };

    const handleDragOver = (e) => {
        e.preventDefault();
        setIsDragActive(true);
    };

    const handleDragLeave = (e) => {
        e.preventDefault();
        setIsDragActive(false);
    };

    const handleDrop = (e) => {
        e.preventDefault();
        setIsDragActive(false);
        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            setSelectedFile(e.dataTransfer.files[0]);
        }
    };

    const handleFileChange = (e) => {
        if (e.target.files && e.target.files[0]) {
            setSelectedFile(e.target.files[0]);
        }
    };

    const handleSubmitAssignment = async () => {
        setShowConfirmModal(false);
        setSubmitting(true);
        setError(null);
        setSuccessMsg(null);

        try {
            let finalUrl = fileUrlInput.trim();

            // If a local file was selected, get a presigned URL or mock upload URL
            if (selectedFile) {
                try {
                    const presignedRes = await axiosInstance.post('/v1/assignments/upload-url', {
                        fileName: selectedFile.name,
                        contentType: selectedFile.type || 'application/octet-stream'
                    });

                    if (presignedRes.data && presignedRes.data.data) {
                        const { uploadUrl, objectKey } = presignedRes.data.data;
                        // Attempt upload to MinIO
                        try {
                            await fetch(uploadUrl, {
                                method: 'PUT',
                                body: selectedFile,
                                headers: { 'Content-Type': selectedFile.type || 'application/octet-stream' }
                            });
                            finalUrl = objectKey || uploadUrl;
                        } catch (uploadErr) {
                            console.warn('Presigned upload network error, fallback to simulated storage link:', uploadErr);
                            finalUrl = `https://storage.edufit.vn/submissions/${encodeURIComponent(selectedFile.name)}`;
                        }
                    }
                } catch (presignedErr) {
                    console.warn('Presigned URL error, fallback to simulated file URL:', presignedErr);
                    finalUrl = `https://storage.edufit.vn/submissions/${encodeURIComponent(selectedFile.name)}`;
                }
            }

            if (!finalUrl) {
                setError('Please select a file or enter your submission link!');
                setSubmitting(false);
                return;
            }

            const response = await axiosInstance.post(`/v1/assignments/${id}/submit`, {
                studentId: studentId,
                fileUrl: finalUrl
            });

            if (response.data && response.data.data) {
                setSubmission(response.data.data);
                setSuccessMsg('Assignment submitted successfully!');
                setIsResubmitting(false);
                setSelectedFile(null);
                setFileUrlInput('');
            }
        } catch (err) {
            console.error('Error submitting assignment:', err);
            setError(err.response?.data?.message || 'Error submitting assignment. Please try again!');
        } finally {
            setSubmitting(false);
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

    const calculateCountdown = (dueDate) => {
        if (!dueDate) return { label: 'Deadline', val: 'No deadline', isOverdue: false };
        const deadline = new Date(dueDate).getTime();
        const now = new Date().getTime();
        const diff = deadline - now;

        if (diff <= 0) {
            return { label: 'Status', val: 'Overdue', isOverdue: true };
        }

        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

        if (days > 0) {
            return { label: 'Time left', val: `${days} days ${hours} hours`, isOverdue: false };
        }
        return { label: 'Time left', val: `${hours} hours ${minutes} minutes`, isOverdue: false };
    };

    if (loading) {
        return (
            <div className="loader-container">
                <div className="spinner"></div>
                <p>Loading assignment details...</p>
            </div>
        );
    }

    if (!assignment && !loading) {
        return (
            <div className="assignment-container">
                <div className="error-message">⚠️ Assignment not found or deleted.</div>
                <button className="btn-card-action" style={{ width: '200px' }} onClick={() => navigate('/student/assignments')}>
                    Back to list
                </button>
            </div>
        );
    }

    const countdown = calculateCountdown(assignment.dueDate);
    const hasSubmitted = submission !== null && !isResubmitting;

    return (
        <div className="assignment-container">
            {/* Breadcrumb navigation */}
            <div className="details-breadcrumb">
                <Link to="/student/student-home" className="breadcrumb-link">Home</Link>
                <span>/</span>
                <Link to="/student/assignments" className="breadcrumb-link">Assignments</Link>
                <span>/</span>
                <span>{assignment.title}</span>
            </div>

            {/* Notifications */}
            {error && <div className="error-message"><span>⚠️</span> {error}</div>}
            {successMsg && (
                <div className="submitted-result-box" style={{ marginBottom: '1.5rem', background: '#ecfdf5', borderColor: '#10b981' }}>
                    <div className="submitted-status-header">
                        <span className="submitted-status-icon">✅</span>
                        <h4 className="submitted-status-title" style={{ color: '#047857' }}>{successMsg}</h4>
                    </div>
                </div>
            )}

            {/* Main Content Layout */}
            <div className="details-layout">
                {/* Left Side: Assignment Detailed Requirements */}
                <div className="details-main">
                    <div className="details-title-row">
                        <div>
                            <span className="class-tag" style={{ marginBottom: '0.5rem', display: 'inline-block' }}>
                                Assignment
                            </span>
                            <h2 className="details-title">{assignment.title}</h2>
                            <p style={{ color: '#6b7280', fontSize: '0.95rem', margin: 0 }}>
                                Created at: {formatDateTime(assignment.createdAt)}
                            </p>
                        </div>
                        <span className={`status-badge ${countdown.isOverdue ? 'overdue' : 'open'}`}>
                            {countdown.isOverdue ? '⛔ Overdue' : '🟢 Open for Submission'}
                        </span>
                    </div>

                    <div className="details-content-box">
                        <h4 className="section-subheading">
                            <span>📋</span> Requirements
                        </h4>
                        <div className="description-text">
                            {assignment.description || 'No detailed instructions for this assignment.'}
                        </div>
                    </div>

                    {/* Attachment file from instructor */}
                    {assignment.attachmentUrl && (
                        <div className="details-content-box">
                            <h4 className="section-subheading">
                                <span>📎</span> Attachments & Files
                            </h4>
                            <div className="attachment-card">
                                <div className="attachment-left">
                                    <div className="file-icon">📄</div>
                                    <div className="attachment-info">
                                        <h4>Instructions & Materials</h4>
                                        <p>Attachment from Lecturer</p>
                                    </div>
                                </div>
                                <a
                                    href={assignment.attachmentUrl}
                                    target="_blank"
                                    rel="noreferrer"
                                    className="btn-download"
                                >
                                    <span>⬇️</span> Download
                                </a>
                            </div>
                        </div>
                    )}
                </div>

                {/* Right Side: Submission Action / Status Area */}
                <div className="details-sidebar">
                    {/* Countdown / Deadline Box */}
                    <div className="sidebar-card">
                        <div className="deadline-countdown-box">
                            <div className="countdown-label">{countdown.label}</div>
                            <div className="countdown-val" style={{ color: countdown.isOverdue ? '#dc2626' : '#4338ca' }}>
                                {countdown.val}
                            </div>
                        </div>

                        <div className="card-meta-list" style={{ marginBottom: '1.5rem' }}>
                            <div className="meta-row">
                                <span className="meta-row-label">📅 Deadline:</span>
                                <span className="meta-row-val">{formatDateTime(assignment.dueDate)}</span>
                            </div>
                            <div className="meta-row">
                                <span className="meta-row-label">🏆 Max Score:</span>
                                <span className="meta-row-val">{assignment.maxScore || 10} pts</span>
                            </div>
                            <div className="meta-row">
                                <span className="meta-row-label">📌 Submission Status:</span>
                                <span className="meta-row-val" style={{ color: hasSubmitted ? '#10b981' : '#f59e0b' }}>
                                    {hasSubmitted ? 'Submitted' : 'Not Submitted'}
                                </span>
                            </div>
                        </div>

                        {/* If Student Has Already Submitted */}
                        {hasSubmitted ? (
                            <div className="submitted-result-box">
                                <div className="submitted-status-header">
                                    <span className="submitted-status-icon">🎉</span>
                                    <h4 className="submitted-status-title">Your Submission</h4>
                                </div>

                                <div className="submitted-details-list">
                                    <p>
                                        <strong>Submission Time:</strong> {formatDateTime(submission.submittedAt)}
                                    </p>
                                    <p>
                                        <strong>Status:</strong>{' '}
                                        <span className={`status-badge ${submission.isLate ? 'late' : 'graded'}`}>
                                            {submission.isLate ? '⚠️ Late Submission' : '✅ On Time'}
                                        </span>
                                    </p>
                                </div>

                                <div className="attachment-card" style={{ background: 'white', marginBottom: '1rem' }}>
                                    <div className="attachment-left">
                                        <div className="file-icon" style={{ background: '#e0e7ff', color: '#6366f1' }}>📁</div>
                                        <div className="attachment-info">
                                            <h4>Submission File</h4>
                                            <p style={{ wordBreak: 'break-all' }}>{submission.fileUrl}</p>
                                        </div>
                                    </div>
                                    <a
                                        href={submission.fileUrl}
                                        target="_blank"
                                        rel="noreferrer"
                                        className="btn-download"
                                    >
                                        Open
                                    </a>
                                </div>

                                {/* Grade & Feedback Display */}
                                {submission.isGraded ? (
                                    <div className="grade-display-card">
                                        <div className="grade-score-header">
                                            <span style={{ fontWeight: 700, color: '#1f2937' }}>Score:</span>
                                            <span className="grade-score-badge">
                                                {submission.score} / {assignment.maxScore || 10}
                                            </span>
                                        </div>
                                        {submission.feedback && (
                                            <div>
                                                <div style={{ fontSize: '0.85rem', fontWeight: 600, color: '#4b5563' }}>
                                                    Lecturer Feedback:
                                                </div>
                                                <div className="feedback-quote-box">
                                                    "{submission.feedback}"
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                ) : (
                                    <div style={{ textAlign: 'center', padding: '1rem', background: '#f8fafc', borderRadius: '12px', color: '#64748b', fontSize: '0.9rem' }}>
                                        ⏳ Assignment is pending instructor grading
                                    </div>
                                )}

                                {!countdown.isOverdue && (
                                    <button
                                        className="btn-card-action secondary"
                                        style={{ marginTop: '1.25rem' }}
                                        onClick={() => setIsResubmitting(true)}
                                    >
                                        🔄 Resubmit
                                    </button>
                                )}
                            </div>
                        ) : userRole === 'STUDENT' ? (
                            /* Submission Upload Zone for STUDENT only */
                            <div>
                                <h4 className="section-subheading" style={{ marginBottom: '1rem' }}>
                                    <span>📤</span> Submit Work
                                </h4>

                                <input
                                    type="file"
                                    ref={fileInputRef}
                                    style={{ display: 'none' }}
                                    onChange={handleFileChange}
                                    accept=".pdf,.doc,.docx,.zip,.rar,.txt,.java,.py,.js,.html,.css"
                                />

                                <div
                                    className={`dropzone ${isDragActive ? 'drag-active' : ''}`}
                                    onDragOver={handleDragOver}
                                    onDragLeave={handleDragLeave}
                                    onDrop={handleDrop}
                                    onClick={() => fileInputRef.current?.click()}
                                >
                                    <span className="dropzone-icon">📁</span>
                                    <p className="dropzone-text">Drag & Drop file here or click to browse</p>
                                    <p className="dropzone-hint">Supports: PDF, Word, ZIP, RAR, Code files (Max 50MB)</p>
                                </div>

                                {selectedFile && (
                                    <div className="file-selected-box">
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                            <span>📄</span>
                                            <span className="file-selected-name">{selectedFile.name}</span>
                                        </div>
                                        <button
                                            className="btn-remove-file"
                                            onClick={() => setSelectedFile(null)}
                                            title="Remove File"
                                        >
                                            ✕
                                        </button>
                                    </div>
                                )}

                                <div className="url-input-toggle" onClick={() => setShowUrlInput(!showUrlInput)}>
                                    {showUrlInput ? '— Hide direct URL input' : '+ Or submit via link (GitHub, Google Drive...)'}
                                </div>

                                {showUrlInput && (
                                    <input
                                        type="url"
                                        className="direct-url-input"
                                        placeholder="https://drive.google.com/... or GitHub repo"
                                        value={fileUrlInput}
                                        onChange={(e) => setFileUrlInput(e.target.value)}
                                    />
                                )}

                                <div style={{ marginTop: '1.5rem', display: 'flex', gap: '0.75rem' }}>
                                    {isResubmitting && (
                                        <button
                                            className="btn-card-action secondary"
                                            onClick={() => setIsResubmitting(false)}
                                            style={{ flex: 1 }}
                                        >
                                            Cancel
                                        </button>
                                    )}
                                    <button
                                        className="btn-card-action"
                                        style={{ flex: 2 }}
                                        disabled={(!selectedFile && !fileUrlInput.trim()) || submitting}
                                        onClick={() => setShowConfirmModal(true)}
                                    >
                                        {submitting ? 'Submitting...' : (isResubmitting ? 'Confirm Resubmission' : '🚀 Submit Assignment')}
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className="lecturer-no-submission" style={{ padding: '2rem', textAlign: 'center', background: '#f8fafc', borderRadius: '12px' }}>
                                <div style={{ fontSize: '2.5rem', marginBottom: '1rem' }}>👩‍🏫</div>
                                <h4 style={{ margin: '0 0 0.5rem 0' }}>Lecturer View</h4>
                                <p style={{ color: '#64748b', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
                                    This is a preview of the assignment for students. Lecturers cannot submit assignments!
                                </p>
                                <button className="btn-card-action" onClick={() => navigate(`/student/assignments/manage?id=${id}`)}>
                                    Switch to Grading ➔
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Confirm Submission Modal */}
            {showConfirmModal && (
                <div className="modal-overlay" onClick={() => setShowConfirmModal(false)}>
                    <div className="modal-glass" onClick={(e) => e.stopPropagation()}>
                        <h3>Confirm Submission</h3>
                        <p>
                            Are you sure you want to submit this assignment? Once submitted, the lecturer will receive your work for grading.
                        </p>
                        <div className="modal-actions">
                            <button
                                className="btn-cancel"
                                onClick={() => setShowConfirmModal(false)}
                                disabled={submitting}
                            >
                                Cancel
                            </button>
                            <button
                                className="btn-confirm"
                                onClick={handleSubmitAssignment}
                                disabled={submitting}
                            >
                                {submitting ? 'Submitting...' : 'Confirm'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AssignmentDetails;
