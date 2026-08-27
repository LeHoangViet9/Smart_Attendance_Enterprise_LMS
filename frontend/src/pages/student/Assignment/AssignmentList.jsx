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
        attachmentUrl: '',
        isExam: false,
        isPublished: true
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
            setError('Cannot load assignment list. Please check your connection!');
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
                attachmentUrl: '',
                isExam: false,
                isPublished: true
            });
            fetchAssignments();
        } catch (err) {
            console.error('Error creating assignment:', err);
            setCreateError(err.response?.data?.message || 'Error creating assignment');
        } finally {
            setCreating(false);
        }
    };

    const calculateTimeStatus = (dueDate) => {
        if (!dueDate) return { text: 'No deadline', isOverdue: false, urgent: false };
        const deadline = new Date(dueDate).getTime();
        const now = new Date().getTime();
        const diff = deadline - now;

        if (diff <= 0) {
            return { text: 'Overdue', isOverdue: true, urgent: true };
        }

        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));

        if (days > 0) {
            return { text: `${days} days ${hours} hours left`, isOverdue: false, urgent: days <= 1 };
        }
        return { text: `${hours} hours left`, isOverdue: false, urgent: true };
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
        // Quyền hiển thị
        if (userRole === 'STUDENT' && item.isPublished === false) return false;
        if (userRole === 'ADMIN' && item.isExam === false) return false;

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
    const totalCount = filteredAssignments.length;
    const overdueCount = filteredAssignments.filter(a => a.dueDate && new Date(a.dueDate).getTime() < new Date().getTime()).length;
    const openCount = totalCount - overdueCount;

    return (
        <div className="assignment-container">
            {/* Header Section */}
            <div className="assignment-header">
                <div className="assignment-header-info">
                    <h1>
                        <span>📑</span> {userRole === 'STUDENT' ? 'Assignments & Practices' : 'Assignment Management'}
                    </h1>
                    <p>{userRole === 'STUDENT' ? 'Manage your assignments, submission progress, and lecturer feedback' : 'Assign tasks, manage submission progress, and evaluate students'}</p>
                </div>
                {(userRole === 'LECTURER' || userRole === 'ADMIN') && (
                    <button
                        className="btn-card-action"
                        style={{ width: 'auto', padding: '0.75rem 1.5rem' }}
                        onClick={() => setShowCreateModal(true)}
                    >
                        ➕ Create Assignment
                    </button>
                )}
            </div>

            {/* KPI Statistics */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-icon purple">📚</div>
                    <div className="stat-content">
                        <div className="stat-value">{totalCount}</div>
                        <div className="stat-label">Total Assignments</div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon emerald">⏳</div>
                    <div className="stat-content">
                        <div className="stat-value">{openCount}</div>
                        <div className="stat-label">Open</div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon amber">⚠️</div>
                    <div className="stat-content">
                        <div className="stat-value">{overdueCount}</div>
                        <div className="stat-label">Overdue</div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon blue">⭐</div>
                    <div className="stat-content">
                        <div className="stat-value">10.0</div>
                        <div className="stat-label">Standard Max Score</div>
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
                        placeholder="Search assignments by title or description..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>

                <div className="filter-pills">
                    <button
                        className={`filter-pill ${statusFilter === 'ALL' ? 'active' : ''}`}
                        onClick={() => setStatusFilter('ALL')}
                    >
                        All ({totalCount})
                    </button>
                    <button
                        className={`filter-pill ${statusFilter === 'OPEN' ? 'active' : ''}`}
                        onClick={() => setStatusFilter('OPEN')}
                    >
                        Open ({openCount})
                    </button>
                    <button
                        className={`filter-pill ${statusFilter === 'OVERDUE' ? 'active' : ''}`}
                        onClick={() => setStatusFilter('OVERDUE')}
                    >
                        Overdue ({overdueCount})
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
                    <p>Loading assignment list...</p>
                </div>
            ) : filteredAssignments.length === 0 ? (
                <div className="details-main" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
                    <div style={{ fontSize: '3.5rem', marginBottom: '1rem' }}>📂</div>
                    <h3 style={{ fontSize: '1.4rem', color: '#1f2937', margin: '0 0 0.5rem' }}>
                        No assignments available
                    </h3>
                    <p style={{ color: '#6b7280', margin: 0 }}>
                        {searchTerm ? 'No assignments matching your search.' : 'No assignments have been assigned to you yet.'}
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
                                        {timeStatus.isOverdue ? '⛔ Closed' : '🟢 Open'}
                                    </span>
                                </div>

                                <h3 className="assignment-card-title">{item.title}</h3>
                                <p className="assignment-card-desc">
                                    {item.description || 'No detailed description for this assignment.'}
                                </p>

                                <div className="card-meta-list">
                                    <div className="meta-row">
                                        <span className="meta-row-label">📅 Deadline:</span>
                                        <span className="meta-row-val">{formatDateTime(item.dueDate)}</span>
                                    </div>
                                    <div className="meta-row">
                                        <span className="meta-row-label">⏱️ Status:</span>
                                        <span className={`meta-row-val ${timeStatus.urgent ? 'urgent' : ''}`}>
                                            {timeStatus.text}
                                        </span>
                                    </div>
                                    <div className="meta-row">
                                        <span className="meta-row-label">🏆 Max Score:</span>
                                        <span className="meta-row-val">{item.maxScore || 10} pts</span>
                                    </div>
                                    {item.attachmentUrl && (
                                        <div className="meta-row">
                                            <span className="meta-row-label">📎 Attachment:</span>
                                            <span className="meta-row-val" style={{ color: '#6366f1' }}>Has document</span>
                                        </div>
                                    )}
                                </div>

                                <button
                                    className={`btn-card-action ${userRole !== 'STUDENT' ? 'secondary' : ''}`}
                                    onClick={() => {
                                        if (userRole === 'STUDENT') {
                                            navigate(`/student/assignments/${item.id}`);
                                        } else {
                                            navigate(`/student/assignments/manage?id=${item.id}`);
                                        }
                                    }}
                                >
                                    <span>{userRole === 'STUDENT' ? 'View Details & Submit' : 'Grade / Manage'}</span>
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
                        <h3>📝 Create New Assignment</h3>
                        <p>Enter assignment details and deadline for students</p>

                        {createError && (
                            <div className="error-message" style={{ marginBottom: '1rem', padding: '0.75rem' }}>
                                <span>⚠️</span> {createError}
                            </div>
                        )}

                        <form onSubmit={handleCreateAssignment}>
                            <div className="modal-form-group">
                                <label>Assignment Title *</label>
                                <input
                                    type="text"
                                    required
                                    className="modal-form-control"
                                    placeholder="e.g. Web Programming Project"
                                    value={newAssignment.title}
                                    onChange={(e) => setNewAssignment({ ...newAssignment, title: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-group">
                                <label>Description / Detailed Requirements</label>
                                <textarea
                                    rows="4"
                                    className="modal-form-control"
                                    placeholder="Describe assignment content, submission format..."
                                    value={newAssignment.description}
                                    onChange={(e) => setNewAssignment({ ...newAssignment, description: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-row">
                                <div className="modal-form-group">
                                    <label>Deadline *</label>
                                    <input
                                        type="datetime-local"
                                        required
                                        className="modal-form-control"
                                        value={newAssignment.dueDate}
                                        onChange={(e) => setNewAssignment({ ...newAssignment, dueDate: e.target.value })}
                                    />
                                </div>
                                <div className="modal-form-group">
                                    <label>Max Score</label>
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
                                <label>Attachment URL</label>
                                <input
                                    type="url"
                                    className="modal-form-control"
                                    placeholder="https://..."
                                    value={newAssignment.attachmentUrl}
                                    onChange={(e) => setNewAssignment({ ...newAssignment, attachmentUrl: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-row">
                                <div className="modal-form-group checkbox-group" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginTop: '1rem' }}>
                                    <label className="switch">
                                        <input
                                            type="checkbox"
                                            checked={newAssignment.isExam}
                                            onChange={(e) => setNewAssignment({ ...newAssignment, isExam: e.target.checked })}
                                        />
                                        <span className="slider round"></span>
                                    </label>
                                    <span style={{ color: '#ef4444', fontWeight: 600 }}>Exam Flag (For Admin Monitoring)</span>
                                </div>
                                <div className="modal-form-group checkbox-group" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginTop: '1rem' }}>
                                    <label className="switch">
                                        <input
                                            type="checkbox"
                                            checked={newAssignment.isPublished}
                                            onChange={(e) => setNewAssignment({ ...newAssignment, isPublished: e.target.checked })}
                                        />
                                        <span className="slider round"></span>
                                    </label>
                                    <span>Visible (Public) to students</span>
                                </div>
                            </div>

                            <div className="modal-actions" style={{ marginTop: '1.5rem' }}>
                                <button
                                    type="button"
                                    className="btn-cancel"
                                    onClick={() => setShowCreateModal(false)}
                                    disabled={creating}
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="btn-confirm"
                                    disabled={creating}
                                >
                                    {creating ? 'Creating...' : 'Create Assignment'}
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
