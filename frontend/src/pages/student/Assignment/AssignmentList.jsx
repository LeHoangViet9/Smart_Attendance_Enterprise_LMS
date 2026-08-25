import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './AssignmentStyles.css';

const AssignmentList = () => {
    const [assignments, setAssignments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState('ALL'); // ALL, OPEN, SUBMITTED, GRADED, OVERDUE
    const [userRole, setUserRole] = useState('STUDENT');
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [newAssignment, setNewAssignment] = useState({
        title: '',
        description: '',
        classId: '3fa85f64-5717-4562-b3fc-2c963f66afa6',
        dueDate: '',
        maxScore: 10,
        attachmentUrl: ''
    });
    const [creating, setCreating] = useState(false);
    const [createError, setCreateError] = useState(null);

    const navigate = useNavigate();

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            try {
                const parsed = JSON.parse(storedUser);
                if (parsed.role) setUserRole(parsed.role);
            } catch (e) {
                console.error(e);
            }
        }
        fetchAssignments();
    }, []);

    const fetchAssignments = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await axiosInstance.get('/v1/assignments');
            if (response.data && response.data.data) {
                setAssignments(response.data.data);
            }
        } catch (err) {
            console.error('Error fetching assignments:', err);
            setError('Không thể tải danh sách bài tập. Vui lòng kiểm tra lại kết nối!');
        } finally {
            setLoading(false);
        }
    };

    const handleCreateAssignment = async (e) => {
        e.preventDefault();
        setCreating(true);
        setCreateError(null);
        try {
            await axiosInstance.post('/v1/assignments', {
                ...newAssignment,
                dueDate: newAssignment.dueDate ? new Date(newAssignment.dueDate).toISOString() : new Date().toISOString(),
                maxScore: Number(newAssignment.maxScore) || 10
            });
            setShowCreateModal(false);
            setNewAssignment({
                title: '',
                description: '',
                classId: '3fa85f64-5717-4562-b3fc-2c963f66afa6',
                dueDate: '',
                maxScore: 10,
                attachmentUrl: ''
            });
            fetchAssignments();
        } catch (err) {
            console.error('Error creating assignment:', err);
            setCreateError(err.response?.data?.message || 'Có lỗi xảy ra khi tạo bài tập');
        } finally {
            setCreating(false);
        }
    };

    const calculateTimeStatus = (dueDate) => {
        if (!dueDate) return { text: 'Không có hạn', isOverdue: false, urgent: false };
        const deadline = new Date(dueDate).getTime();
        const now = new Date().getTime();
        const diff = deadline - now;

        if (diff <= 0) {
            return { text: 'Đã hết hạn', isOverdue: true, urgent: true };
        }

        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));

        if (days > 0) {
            return { text: `Còn ${days} ngày ${hours} giờ`, isOverdue: false, urgent: days <= 1 };
        }
        return { text: `Còn ${hours} giờ nữa`, isOverdue: false, urgent: true };
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

    // Filter assignments
    const filteredAssignments = assignments.filter((item) => {
        const matchesSearch = item.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            item.description?.toLowerCase().includes(searchTerm.toLowerCase());

        const isOverdue = item.dueDate && new Date(item.dueDate).getTime() < new Date().getTime();

        if (statusFilter === 'OVERDUE') {
            return matchesSearch && isOverdue;
        }
        if (statusFilter === 'OPEN') {
            return matchesSearch && !isOverdue;
        }
        return matchesSearch;
    });

    // KPI Counts
    const totalCount = assignments.length;
    const overdueCount = assignments.filter(a => a.dueDate && new Date(a.dueDate).getTime() < new Date().getTime()).length;
    const openCount = totalCount - overdueCount;

    return (
        <div className="assignment-container">
            {/* Header Section */}
            <div className="assignment-header">
                <div className="assignment-header-info">
                    <h1>
                        <span>📑</span> Bài Tập Lớn & Thực Hành
                    </h1>
                    <p>Quản lý danh sách bài tập, tiến độ nộp bài và nhận phản hồi từ giảng viên</p>
                </div>
                {(userRole === 'LECTURER' || userRole === 'ADMIN') && (
                    <button
                        className="btn-card-action"
                        style={{ width: 'auto', padding: '0.75rem 1.5rem' }}
                        onClick={() => setShowCreateModal(true)}
                    >
                        ➕ Tạo bài tập mới
                    </button>
                )}
            </div>

            {/* KPI Statistics */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-icon purple">📚</div>
                    <div className="stat-content">
                        <div className="stat-value">{totalCount}</div>
                        <div className="stat-label">Tổng bài tập</div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon emerald">⏳</div>
                    <div className="stat-content">
                        <div className="stat-value">{openCount}</div>
                        <div className="stat-label">Đang mở nộp</div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon amber">⚠️</div>
                    <div className="stat-content">
                        <div className="stat-value">{overdueCount}</div>
                        <div className="stat-label">Đã quá hạn</div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon blue">⭐</div>
                    <div className="stat-content">
                        <div className="stat-value">10.0</div>
                        <div className="stat-label">Thang điểm chuẩn</div>
                    </div>
                </div>
            </div>

            {/* Filter & Search Toolbar */}
            <div className="filter-bar">
                <div className="search-box">
                    <span className="search-icon">🔍</span>
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Tìm kiếm bài tập theo tên hoặc mô tả..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>

                <div className="filter-pills">
                    <button
                        className={`filter-pill ${statusFilter === 'ALL' ? 'active' : ''}`}
                        onClick={() => setStatusFilter('ALL')}
                    >
                        Tất cả ({totalCount})
                    </button>
                    <button
                        className={`filter-pill ${statusFilter === 'OPEN' ? 'active' : ''}`}
                        onClick={() => setStatusFilter('OPEN')}
                    >
                        Đang mở ({openCount})
                    </button>
                    <button
                        className={`filter-pill ${statusFilter === 'OVERDUE' ? 'active' : ''}`}
                        onClick={() => setStatusFilter('OVERDUE')}
                    >
                        Quá hạn ({overdueCount})
                    </button>
                </div>
            </div>

            {/* Error Message */}
            {error && (
                <div className="error-message">
                    <span>⚠️</span> {error}
                </div>
            )}

            {/* Loading State */}
            {loading ? (
                <div className="loader-container">
                    <div className="spinner"></div>
                    <p>Đang tải danh sách bài tập...</p>
                </div>
            ) : filteredAssignments.length === 0 ? (
                <div className="details-main" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
                    <div style={{ fontSize: '3.5rem', marginBottom: '1rem' }}>📂</div>
                    <h3 style={{ fontSize: '1.4rem', color: '#1f2937', margin: '0 0 0.5rem' }}>
                        Chưa có bài tập nào
                    </h3>
                    <p style={{ color: '#6b7280', margin: 0 }}>
                        {searchTerm ? 'Không tìm thấy bài tập nào khớp với từ khóa tìm kiếm.' : 'Hiện tại chưa có bài tập nào được giao cho bạn.'}
                    </p>
                </div>
            ) : (
                <div className="assignment-grid">
                    {filteredAssignments.map((item) => {
                        const timeStatus = calculateTimeStatus(item.dueDate);
                        return (
                            <div
                                key={item.id}
                                className={`assignment-card ${timeStatus.isOverdue ? 'overdue' : ''}`}
                            >
                                <div className="card-top">
                                    <span className="class-tag">Assignment</span>
                                    <span className={`status-badge ${timeStatus.isOverdue ? 'overdue' : 'open'}`}>
                                        {timeStatus.isOverdue ? '⛔ Đã đóng' : '🟢 Đang mở'}
                                    </span>
                                </div>

                                <h3 className="assignment-card-title">{item.title}</h3>
                                <p className="assignment-card-desc">
                                    {item.description || 'Không có mô tả chi tiết cho bài tập này.'}
                                </p>

                                <div className="card-meta-list">
                                    <div className="meta-row">
                                        <span className="meta-row-label">📅 Hạn nộp:</span>
                                        <span className="meta-row-val">{formatDateTime(item.dueDate)}</span>
                                    </div>
                                    <div className="meta-row">
                                        <span className="meta-row-label">⏱️ Trạng thái:</span>
                                        <span className={`meta-row-val ${timeStatus.urgent ? 'urgent' : ''}`}>
                                            {timeStatus.text}
                                        </span>
                                    </div>
                                    <div className="meta-row">
                                        <span className="meta-row-label">🏆 Điểm tối đa:</span>
                                        <span className="meta-row-val">{item.maxScore || 10} điểm</span>
                                    </div>
                                    {item.attachmentUrl && (
                                        <div className="meta-row">
                                            <span className="meta-row-label">📎 Đính kèm:</span>
                                            <span className="meta-row-val" style={{ color: '#6366f1' }}>Có tài liệu</span>
                                        </div>
                                    )}
                                </div>

                                <button
                                    className="btn-card-action"
                                    onClick={() => navigate(`/student/assignments/${item.id}`)}
                                >
                                    <span>Xem chi tiết & Nộp bài</span>
                                    <span>➔</span>
                                </button>
                            </div>
                        );
                    })}
                </div>
            )}

            {/* Modal Create Assignment (For Lecturer / Testing) */}
            {showCreateModal && (
                <div className="modal-overlay" onClick={() => setShowCreateModal(false)}>
                    <div className="modal-glass" style={{ maxWidth: '540px' }} onClick={(e) => e.stopPropagation()}>
                        <h3>📝 Tạo Bài Tập Mới</h3>
                        <p>Nhập thông tin đề bài và hạn nộp cho sinh viên</p>

                        {createError && (
                            <div className="error-message" style={{ marginBottom: '1rem', padding: '0.75rem' }}>
                                <span>⚠️</span> {createError}
                            </div>
                        )}

                        <form onSubmit={handleCreateAssignment}>
                            <div className="modal-form-group">
                                <label>Tiêu đề bài tập *</label>
                                <input
                                    type="text"
                                    required
                                    className="modal-form-control"
                                    placeholder="vd: Bài tập lớn môn Lập trình Web"
                                    value={newAssignment.title}
                                    onChange={(e) => setNewAssignment({ ...newAssignment, title: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-group">
                                <label>Mô tả / Yêu cầu chi tiết</label>
                                <textarea
                                    rows="4"
                                    className="modal-form-control"
                                    placeholder="Mô tả nội dung bài tập, định dạng nộp..."
                                    value={newAssignment.description}
                                    onChange={(e) => setNewAssignment({ ...newAssignment, description: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-row">
                                <div className="modal-form-group">
                                    <label>Hạn nộp (Deadline) *</label>
                                    <input
                                        type="datetime-local"
                                        required
                                        className="modal-form-control"
                                        value={newAssignment.dueDate}
                                        onChange={(e) => setNewAssignment({ ...newAssignment, dueDate: e.target.value })}
                                    />
                                </div>
                                <div className="modal-form-group">
                                    <label>Điểm tối đa</label>
                                    <input
                                        type="number"
                                        step="0.5"
                                        className="modal-form-control"
                                        value={newAssignment.maxScore}
                                        onChange={(e) => setNewAssignment({ ...newAssignment, maxScore: e.target.value })}
                                    />
                                </div>
                            </div>

                            <div className="modal-form-group">
                                <label>Link tài liệu đính kèm (URL)</label>
                                <input
                                    type="url"
                                    className="modal-form-control"
                                    placeholder="https://..."
                                    value={newAssignment.attachmentUrl}
                                    onChange={(e) => setNewAssignment({ ...newAssignment, attachmentUrl: e.target.value })}
                                />
                            </div>

                            <div className="modal-actions" style={{ marginTop: '1.5rem' }}>
                                <button
                                    type="button"
                                    className="btn-cancel"
                                    onClick={() => setShowCreateModal(false)}
                                    disabled={creating}
                                >
                                    Hủy bỏ
                                </button>
                                <button
                                    type="submit"
                                    className="btn-confirm"
                                    disabled={creating}
                                >
                                    {creating ? 'Đang tạo...' : 'Tạo bài tập'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AssignmentList;
