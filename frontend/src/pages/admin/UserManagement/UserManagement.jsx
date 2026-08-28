import React, { useState, useEffect } from 'react';
import axiosInstance from '../../../api/axios';
import './UserManagement.css';

const UserManagement = () => {
    const [users, setUsers] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [formData, setFormData] = useState({
        email: '',
        fullName: '',
        code: '',
        role: 'STUDENT',
        parentPhone: '',
        className: '',
        enrollmentYear: '',
        degree: '',
        major: '',
        department: ''
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [alertMsg, setAlertMsg] = useState(null);
    const [isEditMode, setIsEditMode] = useState(false);
    const [currentUserId, setCurrentUserId] = useState(null);

    // Auth Check
    const userString = localStorage.getItem('user');
    const loggedInUser = userString ? JSON.parse(userString) : null;
    const isLecturer = loggedInUser?.role === 'LECTURER';

    // Custom Delete Modal State
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [userToDelete, setUserToDelete] = useState(null);

    // Search, Filter & Pagination State
    const [searchQuery, setSearchQuery] = useState('');
    const [activeTab, setActiveTab] = useState('STUDENT'); // Replaces roleFilter
    const [statusFilter, setStatusFilter] = useState('ALL');
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const handleInputChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const loadUsers = async (page = 0) => {
        try {
            const params = {
                page: page,
                size: 10,
                role: activeTab // Force filter by active tab
            };
            if (searchQuery) params.keyword = searchQuery;
            if (statusFilter !== 'ALL') params.isActive = statusFilter === 'ACTIVE';

            const response = await axiosInstance.get('/v1/users', { params });
            if (response.data.success) {
                setUsers(response.data.data.content);
                setTotalPages(response.data.data.totalPages);
                setCurrentPage(response.data.data.number);
            }
        } catch (error) {
            console.error('Error loading users:', error);
        }
    };

    // Auto load & debounce search
    useEffect(() => {
        const delayDebounceFn = setTimeout(() => {
            loadUsers(0);
        }, 400); // Wait 400ms after user stops typing to call API

        return () => clearTimeout(delayDebounceFn);
    }, [searchQuery, activeTab, statusFilter]);

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < totalPages) {
            loadUsers(newPage);
        }
    };

    const openAddModal = () => {
        setIsEditMode(false);
        setCurrentUserId(null);
        setFormData({
            email: '', fullName: '', code: '', role: activeTab,
            parentPhone: '', className: '', enrollmentYear: '',
            degree: '', major: '', department: ''
        });
        setAlertMsg(null);
        setShowModal(true);
    };

    const openEditModal = (user) => {
        setIsEditMode(true);
        setCurrentUserId(user.userId || user.id);
        setFormData({
            email: user.email,
            fullName: user.fullName,
            code: user.code,
            role: user.role,
            parentPhone: user.parentPhone || '',
            className: user.className || '',
            enrollmentYear: user.enrollmentYear || '',
            degree: user.degree || '',
            major: user.major || '',
            department: user.department || ''
        });
        setAlertMsg(null);
        setShowModal(true);
    };

    const confirmDelete = (user) => {
        setUserToDelete(user);
        setShowDeleteModal(true);
    };

    const executeDelete = async () => {
        if (!userToDelete) return;
        try {
            const response = await axiosInstance.delete(`/v1/users/${userToDelete.userId || userToDelete.id}`);
            if (response.data.success) {
                setAlertMsg({ type: 'success', text: `Account ${userToDelete.email} locked successfully!` });
                loadUsers(currentPage);
            }
        } catch (error) {
            setAlertMsg({ type: 'error', text: error.response?.data?.message || 'Error locking user!' });
        } finally {
            setShowDeleteModal(false);
            setUserToDelete(null);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        setAlertMsg(null);
        try {
            if (isEditMode) {
                const response = await axiosInstance.put(`/v1/users/${currentUserId}`, formData);
                if (response.data.success) {
                    setAlertMsg({ type: 'success', text: 'Account updated successfully!' });
                    loadUsers(currentPage);
                    setTimeout(() => setShowModal(false), 1500);
                }
            } else {
                const response = await axiosInstance.post('/auth/register', formData);
                if (response.data.success) {
                    setAlertMsg({ type: 'success', text: 'User created successfully!' });
                    loadUsers(0);
                    setTimeout(() => {
                        setShowModal(false);
                        setFormData({
                            email: '', fullName: '', code: '', role: activeTab,
                            parentPhone: '', className: '', enrollmentYear: '',
                            degree: '', major: '', department: ''
                        });
                    }, 1500);
                }
            }
        } catch (error) {
            setAlertMsg({ type: 'error', text: error.response?.data?.message || (isEditMode ? 'Error updating account!' : 'Error creating user!') });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="user-management">
            <div className="page-header">
                <div>
                    <h2>{isLecturer ? 'Student Directory' : 'User Management'}</h2>
                    <p>{isLecturer ? 'View and manage student enrollments' : 'Manage system accounts and users.'}</p>
                </div>
                {!isLecturer && (
                    <button className="btn-primary um-action-btn" onClick={openAddModal}>
                        <span className="icon">➕</span> Add User
                    </button>
                )}
            </div>

            <div className="um-tabs-container">
                <button
                    className={`um-tab-btn ${activeTab === 'STUDENT' ? 'active' : ''}`}
                    onClick={() => { setActiveTab('STUDENT'); setCurrentPage(0); }}
                >
                    👨‍🎓 {isLecturer ? 'Student Directory' : 'Manage Students'}
                </button>
                {!isLecturer && (
                    <button
                        className={`um-tab-btn ${activeTab === 'LECTURER' ? 'active' : ''}`}
                        onClick={() => { setActiveTab('LECTURER'); setCurrentPage(0); }}
                    >
                        👨‍🏫 Manage Lecturers
                    </button>
                )}
            </div>

            <div className="um-filter-bar">
                <input
                    type="text"
                    className="um-search-input"
                    placeholder="Search by Code, Name, or Email..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                />
                <select
                    className="um-role-select"
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                >
                    <option value="ALL">All Statuses</option>
                    <option value="ACTIVE">Active</option>
                    <option value="INACTIVE">Locked</option>
                </select>
            </div>

            <div className="um-glass-card table-card">
                <table className="user-table">
                    <thead>
                        <tr>
                            <th>{activeTab === 'STUDENT' ? 'Student ID' : 'Lecturer ID'}</th>
                            <th>Full Name</th>
                            <th>Email</th>
                            <th>Academic Details</th>
                            <th>Status</th>
                            {!isLecturer && <th>Actions</th>}
                        </tr>
                    </thead>
                    <tbody>
                        {users.length > 0 ? (
                            users.map(u => (
                                <tr key={u.userId || u.id}>
                                    <td><span className="badge-code">{u.code}</span></td>
                                    <td className="name-cell">
                                        <div className="avatar-small">{u.fullName ? u.fullName.charAt(0) : 'U'}</div>
                                        {u.fullName}
                                    </td>
                                    <td>{u.email}</td>
                                    <td>
                                        {activeTab === 'STUDENT' ? (
                                            <div style={{ fontSize: '0.85rem' }}>
                                                Class: <strong style={{ color: '#6366f1' }}>{u.className || 'N/A'}</strong><br />
                                                Parent Phone: {u.parentPhone || 'N/A'}
                                            </div>
                                        ) : (
                                            <div style={{ fontSize: '0.85rem' }}>
                                                Degree: <strong style={{ color: '#10b981' }}>{u.degree || 'N/A'}</strong><br />
                                                Major: {u.major || 'N/A'}
                                            </div>
                                        )}
                                    </td>
                                    <td><span className="badge-status active">{u.isActive === false ? 'Locked' : 'Active'}</span></td>
                                    {!isLecturer && (
                                        <td>
                                            <button className="btn-icon" onClick={() => openEditModal(u)}>✏️</button>
                                            <button className="btn-icon delete" onClick={() => confirmDelete(u)}>🗑️</button>
                                        </td>
                                    )}
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan="6" style={{ textAlign: 'center', padding: '30px', color: '#6b7280' }}>
                                    No users found matching the filter.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>

            <div className="um-pagination-wrapper">
                <button
                    className="btn-page"
                    disabled={currentPage === 0}
                    onClick={() => handlePageChange(currentPage - 1)}
                >
                    &laquo; Prev
                </button>
                <span className="page-info">
                    Page {currentPage + 1} / {totalPages === 0 ? 1 : totalPages}
                </span>
                <button
                    className="btn-page"
                    disabled={currentPage >= totalPages - 1 || totalPages === 0}
                    onClick={() => handlePageChange(currentPage + 1)}
                >
                    Next &raquo;
                </button>
            </div>

            {showModal && (
                <div className="modal-overlay">
                    <div className="modal-content um-glass-modal">
                        <div className="modal-header">
                            <h3>{isEditMode ? 'Update Account' : 'Add System Account'}</h3>
                            <button className="close-btn" onClick={() => setShowModal(false)}>✕</button>
                        </div>

                        {alertMsg && <div className={`alert-box ${alertMsg.type}`}>{alertMsg.text}</div>}

                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label>ID Code</label>
                                <input type="text" name="code" value={formData.code} onChange={handleInputChange} required placeholder="e.g. STU2024" />
                            </div>
                            <div className="form-group">
                                <label>Full Name</label>
                                <input type="text" name="fullName" value={formData.fullName} onChange={handleInputChange} required placeholder="e.g. John Doe" />
                            </div>
                            <div className="form-group">
                                <label>Email Address</label>
                                <input type="email" name="email" value={formData.email} onChange={handleInputChange} required placeholder="e.g. john@edu.vn" />
                            </div>
                            <div className="form-group">
                                <label>Role</label>
                                <select name="role" value={formData.role} onChange={handleInputChange} className="custom-select">
                                    <option value="STUDENT">Student (STUDENT)</option>
                                    <option value="LECTURER">Lecturer (LECTURER)</option>
                                </select>
                            </div>

                            {formData.role === 'STUDENT' && (
                                <div className="form-group-row" style={{ display: 'flex', gap: '15px' }}>
                                    <div className="form-group" style={{ flex: 1 }}>
                                        <label>Homeroom Class</label>
                                        <input type="text" name="className" value={formData.className} onChange={handleInputChange} placeholder="e.g. K65-SE" />
                                    </div>
                                    <div className="form-group" style={{ flex: 1 }}>
                                        <label>Enrollment Year</label>
                                        <input type="number" name="enrollmentYear" value={formData.enrollmentYear} onChange={handleInputChange} placeholder="e.g. 2024" />
                                    </div>
                                    <div className="form-group" style={{ flex: 1 }}>
                                        <label>Parent Phone</label>
                                        <input type="text" name="parentPhone" value={formData.parentPhone} onChange={handleInputChange} placeholder="e.g. 0988..." />
                                    </div>
                                </div>
                            )}

                            {formData.role === 'LECTURER' && (
                                <div className="form-group-row" style={{ display: 'flex', gap: '15px' }}>
                                    <div className="form-group" style={{ flex: 1 }}>
                                        <label>Degree</label>
                                        <input type="text" name="degree" value={formData.degree} onChange={handleInputChange} placeholder="e.g. Ph.D" />
                                    </div>
                                    <div className="form-group" style={{ flex: 1 }}>
                                        <label>Major</label>
                                        <input type="text" name="major" value={formData.major} onChange={handleInputChange} placeholder="e.g. Software Engineering" />
                                    </div>
                                    <div className="form-group" style={{ flex: 1 }}>
                                        <label>Department</label>
                                        <input type="text" name="department" value={formData.department} onChange={handleInputChange} placeholder="e.g. IT Faculty" />
                                    </div>
                                </div>
                            )}

                            <div className="modal-actions" style={{ marginTop: '1.5rem' }}>
                                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                                <button type="submit" className="btn-primary" disabled={isSubmitting}>
                                    {isSubmitting ? 'Processing...' : (isEditMode ? 'Update' : 'Save Account')}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
            {showDeleteModal && (
                <div className="modal-overlay">
                    <div className="modal-content um-glass-modal" style={{ maxWidth: '400px', textAlign: 'center' }}>
                        <div className="modal-icon" style={{ fontSize: '3.5rem', margin: '15px 0', display: 'inline-block', filter: 'drop-shadow(0 4px 6px rgba(239, 68, 68, 0.3))' }}>
                            ⚠️
                        </div>
                        <h3 style={{ fontSize: '1.4rem', color: '#1f2937', marginBottom: '10px' }}>Confirm Lock</h3>
                        <p style={{ color: '#4b5563', marginBottom: '25px', lineHeight: '1.5' }}>
                            Are you sure you want to lock the account:<br />
                            <strong style={{ color: '#ef4444', fontSize: '1.1rem' }}>{userToDelete?.email}</strong>?
                        </p>
                        <div className="modal-actions" style={{ justifyContent: 'center', gap: '12px' }}>
                            <button className="btn-secondary" style={{ padding: '10px 20px', borderRadius: '8px' }} onClick={() => setShowDeleteModal(false)}>Cancel</button>
                            <button className="btn-primary" style={{ background: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)', border: 'none', padding: '10px 20px', borderRadius: '8px' }} onClick={executeDelete}>Lock Account</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UserManagement;
