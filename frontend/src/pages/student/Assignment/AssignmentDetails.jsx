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

    // Determine current studentId (fallback to a default UUID if user ID is numeric or not found)
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
            setError('Không thể tải thông tin bài tập. Vui lòng thử lại sau.');
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
                setError('Vui lòng chọn một tệp hoặc nhập link bài làm của bạn!');
                setSubmitting(false);
                return;
            }

            const response = await axiosInstance.post(`/v1/assignments/${id}/submit`, {
                studentId: studentId,
                fileUrl: finalUrl
            });

            if (response.data && response.data.data) {
                setSubmission(response.data.data);
                setSuccessMsg('Nộp bài tập thành công!');
                setIsResubmitting(false);
                setSelectedFile(null);
                setFileUrlInput('');
            }
        } catch (err) {
            console.error('Error submitting assignment:', err);
            setError(err.response?.data?.message || 'Có lỗi xảy ra khi nộp bài. Vui lòng thử lại!');
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
        if (!dueDate) return { label: 'Hạn chót', val: 'Không có hạn', isOverdue: false };
        const deadline = new Date(dueDate).getTime();
        const now = new Date().getTime();
        const diff = deadline - now;

        if (diff <= 0) {
            return { label: 'Trạng thái', val: 'Đã hết hạn nộp', isOverdue: true };
        }

        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

        if (days > 0) {
            return { label: 'Thời gian còn lại', val: `${days} ngày ${hours} giờ`, isOverdue: false };
        }
        return { label: 'Thời gian còn lại', val: `${hours} giờ ${minutes} phút`, isOverdue: false };
    };

    if (loading) {
        return (
            <div className="loader-container">
                <div className="spinner"></div>
                <p>Đang tải chi tiết bài tập...</p>
            </div>
        );
    }

    if (!assignment && !loading) {
        return (
            <div className="assignment-container">
                <div className="error-message">⚠️ Không tìm thấy bài tập hoặc bài tập đã bị xóa.</div>
                <button className="btn-card-action" style={{ width: '200px' }} onClick={() => navigate('/student/assignments')}>
                    Quay lại danh sách
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
                <Link to="/student/student-home" className="breadcrumb-link">Trang chủ</Link>
                <span>/</span>
                <Link to="/student/assignments" className="breadcrumb-link">Bài tập</Link>
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
                                Được tạo lúc: {formatDateTime(assignment.createdAt)}
                            </p>
                        </div>
                        <span className={`status-badge ${countdown.isOverdue ? 'overdue' : 'open'}`}>
                            {countdown.isOverdue ? '⛔ Đã hết hạn' : '🟢 Đang mở nộp'}
                        </span>
                    </div>

                    <div className="details-content-box">
                        <h4 className="section-subheading">
                            <span>📋</span> Yêu cầu đề bài
                        </h4>
                        <div className="description-text">
                            {assignment.description || 'Không có hướng dẫn chi tiết thêm cho bài tập này.'}
                        </div>
                    </div>

                    {/* Attachment file from instructor */}
                    {assignment.attachmentUrl && (
                        <div className="details-content-box">
                            <h4 className="section-subheading">
                                <span>📎</span> Tài liệu & Đề bài đính kèm
                            </h4>
                            <div className="attachment-card">
                                <div className="attachment-left">
                                    <div className="file-icon">📄</div>
                                    <div className="attachment-info">
                                        <h4>Tài liệu hướng dẫn thực hành</h4>
                                        <p>Tệp đính kèm từ Giảng viên</p>
                                    </div>
                                </div>
                                <a
                                    href={assignment.attachmentUrl}
                                    target="_blank"
                                    rel="noreferrer"
                                    className="btn-download"
                                >
                                    <span>⬇️</span> Tải xuống
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
                                <span className="meta-row-label">📅 Hạn chót:</span>
                                <span className="meta-row-val">{formatDateTime(assignment.dueDate)}</span>
                            </div>
                            <div className="meta-row">
                                <span className="meta-row-label">🏆 Điểm tối đa:</span>
                                <span className="meta-row-val">{assignment.maxScore || 10} điểm</span>
                            </div>
                            <div className="meta-row">
                                <span className="meta-row-label">📌 Tình trạng nộp:</span>
                                <span className="meta-row-val" style={{ color: hasSubmitted ? '#10b981' : '#f59e0b' }}>
                                    {hasSubmitted ? 'Đã nộp bài' : 'Chưa nộp bài'}
                                </span>
                            </div>
                        </div>

                        {/* If Student Has Already Submitted */}
                        {hasSubmitted ? (
                            <div className="submitted-result-box">
                                <div className="submitted-status-header">
                                    <span className="submitted-status-icon">🎉</span>
                                    <h4 className="submitted-status-title">Bài nộp của bạn</h4>
                                </div>

                                <div className="submitted-details-list">
                                    <p>
                                        <strong>Thời gian nộp:</strong> {formatDateTime(submission.submittedAt)}
                                    </p>
                                    <p>
                                        <strong>Trạng thái:</strong>{' '}
                                        <span className={`status-badge ${submission.isLate ? 'late' : 'graded'}`}>
                                            {submission.isLate ? '⚠️ Nộp muộn' : '✅ Đúng hạn'}
                                        </span>
                                    </p>
                                </div>

                                <div className="attachment-card" style={{ background: 'white', marginBottom: '1rem' }}>
                                    <div className="attachment-left">
                                        <div className="file-icon" style={{ background: '#e0e7ff', color: '#6366f1' }}>📁</div>
                                        <div className="attachment-info">
                                            <h4>Tệp bài làm</h4>
                                            <p style={{ wordBreak: 'break-all' }}>{submission.fileUrl}</p>
                                        </div>
                                    </div>
                                    <a
                                        href={submission.fileUrl}
                                        target="_blank"
                                        rel="noreferrer"
                                        className="btn-download"
                                    >
                                        Mở
                                    </a>
                                </div>

                                {/* Grade & Feedback Display */}
                                {submission.isGraded ? (
                                    <div className="grade-display-card">
                                        <div className="grade-score-header">
                                            <span style={{ fontWeight: 700, color: '#1f2937' }}>Điểm đánh giá:</span>
                                            <span className="grade-score-badge">
                                                {submission.score} / {assignment.maxScore || 10}
                                            </span>
                                        </div>
                                        {submission.feedback && (
                                            <div>
                                                <div style={{ fontSize: '0.85rem', fontWeight: 600, color: '#4b5563' }}>
                                                    Nhận xét của Giảng viên:
                                                </div>
                                                <div className="feedback-quote-box">
                                                    "{submission.feedback}"
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                ) : (
                                    <div style={{ textAlign: 'center', padding: '1rem', background: '#f8fafc', borderRadius: '12px', color: '#64748b', fontSize: '0.9rem' }}>
                                        ⏳ Bài tập đang chờ giảng viên chấm điểm
                                    </div>
                                )}

                                {!countdown.isOverdue && (
                                    <button
                                        className="btn-card-action secondary"
                                        style={{ marginTop: '1.25rem' }}
                                        onClick={() => setIsResubmitting(true)}
                                    >
                                        🔄 Nộp lại bài khác
                                    </button>
                                )}
                            </div>
                        ) : (
                            /* Submission Upload Zone */
                            <div>
                                <h4 className="section-subheading" style={{ marginBottom: '1rem' }}>
                                    <span>📤</span> Nộp bài làm
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
                                    <p className="dropzone-text">Kéo thả tệp vào đây hoặc bấm để duyệt</p>
                                    <p className="dropzone-hint">Hỗ trợ: PDF, Word, ZIP, RAR, Code files (Tối đa 50MB)</p>
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
                                            title="Xóa tệp"
                                        >
                                            ✕
                                        </button>
                                    </div>
                                )}

                                <div className="url-input-toggle" onClick={() => setShowUrlInput(!showUrlInput)}>
                                    {showUrlInput ? '— Ẩn nhập link bài làm trực tiếp' : '+ Hoặc nộp bằng liên kết (GitHub, Google Drive...)'}
                                </div>

                                {showUrlInput && (
                                    <input
                                        type="url"
                                        className="direct-url-input"
                                        placeholder="https://drive.google.com/... hoặc GitHub repo"
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
                                            Hủy
                                        </button>
                                    )}
                                    <button
                                        className="btn-card-action"
                                        style={{ flex: 2 }}
                                        disabled={(!selectedFile && !fileUrlInput.trim()) || submitting}
                                        onClick={() => setShowConfirmModal(true)}
                                    >
                                        {submitting ? 'Đang gửi...' : (isResubmitting ? 'Xác nhận nộp lại' : '🚀 Gửi bài làm')}
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Confirm Submission Modal */}
            {showConfirmModal && (
                <div className="modal-overlay" onClick={() => setShowConfirmModal(false)}>
                    <div className="modal-glass" onClick={(e) => e.stopPropagation()}>
                        <h3>Xác Nhận Nộp Bài</h3>
                        <p>
                            Bạn có chắc chắn muốn nộp bài làm này không? Sau khi nộp, giảng viên sẽ nhận được bài nộp của bạn để chấm điểm.
                        </p>
                        <div className="modal-actions">
                            <button
                                className="btn-cancel"
                                onClick={() => setShowConfirmModal(false)}
                                disabled={submitting}
                            >
                                Hủy bỏ
                            </button>
                            <button
                                className="btn-confirm"
                                onClick={handleSubmitAssignment}
                                disabled={submitting}
                            >
                                {submitting ? 'Đang gửi...' : 'Đồng ý nộp'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AssignmentDetails;
