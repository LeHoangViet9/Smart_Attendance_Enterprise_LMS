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
        role: 'STUDENT'
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [alertMsg, setAlertMsg] = useState(null);
    const [isEditMode, setIsEditMode] = useState(false);
    const [currentUserId, setCurrentUserId] = useState(null);

    // Custom Delete Modal State
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [userToDelete, setUserToDelete] = useState(null);

    // Search, Filter & Pagination State
    const [searchQuery, setSearchQuery] = useState('');
    const [roleFilter, setRoleFilter] = useState('ALL');
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
                size: 10
            };
            if (searchQuery) params.keyword = searchQuery;
            if (roleFilter !== 'ALL') params.role = roleFilter;
            if (statusFilter !== 'ALL') params.isActive = statusFilter === 'ACTIVE';

            const response = await axiosInstance.get('/v1/users', { params });
            if (response.data.success) {
                setUsers(response.data.data.content);
                setTotalPages(response.data.data.totalPages);
                setCurrentPage(response.data.data.number);
            }
        } catch (error) {
            console.error('Lỗi khi tải danh sách user:', error);
        }
    };

    // Auto load & debounce search
    useEffect(() => {
        const delayDebounceFn = setTimeout(() => {
            loadUsers(0);
        }, 400); // Wait 400ms after user stops typing to call API

        return () => clearTimeout(delayDebounceFn);
    }, [searchQuery, roleFilter, statusFilter]);

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < totalPages) {
            loadUsers(newPage);
        }
    };

    const openAddModal = () => {
        setIsEditMode(false);
        setCurrentUserId(null);
        setFormData({ email: '', fullName: '', code: '', role: 'STUDENT' });
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
            role: user.role
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
                setAlertMsg({ type: 'success', text: `Đã khóa tài khoản ${userToDelete.email} thành công!` });
                loadUsers(currentPage);
            }
        } catch (error) {
            setAlertMsg({ type: 'error', text: error.response?.data?.message || 'Có lỗi xảy ra khi khóa user!' });
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
                    setAlertMsg({ type: 'success', text: 'Cập nhật tài khoản thành công!' });
                    loadUsers(currentPage);
                    setTimeout(() => setShowModal(false), 1500);
                }
            } else {
                const response = await axiosInstance.post('/auth/register', formData);
                if (response.data.success) {
                    setAlertMsg({ type: 'success', text: 'Tạo người dùng thành công!' });
                    loadUsers(0);
                    setTimeout(() => {
                        setShowModal(false);
                        setFormData({ email: '', fullName: '', code: '', role: 'STUDENT' });
                    }, 1500);
                }
            }
        } catch (error) {
            setAlertMsg({ type: 'error', text: error.response?.data?.message || (isEditMode ? 'Có lỗi khi cập nhật!' : 'Có lỗi khi tạo user!') });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="user-management">
            <div className="page-header">
                <div>
                    <h2>Quản lý Người Dùng</h2>
                    <p>Quản lý sinh viên, giảng viên và thiết lập tài khoản mới hệ thống thực.</p>
                </div>
                <button className="btn-primary um-action-btn" onClick={openAddModal}>
                    <span className="icon">➕</span> Thêm người dùng
                </button>
            </div>

            <div className="um-filter-bar">
                <input
                    type="text"
                    className="um-search-input"
                    placeholder="Tìm kiếm theo Mã, Tên hoặc Email..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                />
                <select
                    className="um-role-select"
                    value={roleFilter}
                    onChange={(e) => setRoleFilter(e.target.value)}
                >
                    <option value="ALL">Tất cả vai trò</option>
                    <option value="STUDENT">Học viên</option>
                    <option value="LECTURER">Giảng viên</option>
                </select>
                <select
                    className="um-role-select"
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                >
                    <option value="ALL">Tất cả trạng thái</option>
                    <option value="ACTIVE">Kích hoạt</option>
                    <option value="INACTIVE">Khóa</option>
                </select>
            </div>

            <div className="um-glass-card table-card">
                <table className="user-table">
                    <thead>
                        <tr>
                            <th>Mã HV/GV</th>
                            <th>Họ & Tên</th>
                            <th>Email</th>
                            <th>Vai trò (Role)</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
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
                                        <span className={`badge-role ${u.role ? u.role.toLowerCase() : ''}`}>{u.role}</span>
                                    </td>
                                    <td><span className="badge-status active">{u.isActive === false ? 'Khóa' : 'Kích hoạt'}</span></td>
                                    <td>
                                        <button className="btn-icon" onClick={() => openEditModal(u)}>✏️</button>
                                        <button className="btn-icon delete" onClick={() => confirmDelete(u)}>🗑️</button>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan="6" style={{ textAlign: 'center', padding: '30px', color: '#6b7280' }}>
                                    Không tìm thấy tài khoản nào khớp với bộ lọc.
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
                    &laquo; Trang Trước
                </button>
                <span className="page-info">
                    Trang {currentPage + 1} / {totalPages === 0 ? 1 : totalPages}
                </span>
                <button
                    className="btn-page"
                    disabled={currentPage >= totalPages - 1 || totalPages === 0}
                    onClick={() => handlePageChange(currentPage + 1)}
                >
                    Trang Sau &raquo;
                </button>
            </div>

            {showModal && (
                <div className="modal-overlay">
                    <div className="modal-content um-glass-modal">
                        <div className="modal-header">
                            <h3>{isEditMode ? 'Cập nhật Tài Khoản' : 'Thêm Tài Khoản Hệ Thống'}</h3>
                            <button className="close-btn" onClick={() => setShowModal(false)}>✕</button>
                        </div>

                        {alertMsg && <div className={`alert-box ${alertMsg.type}`}>{alertMsg.text}</div>}

                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label>Mã Sinh Viên / Giảng Viên</label>
                                <input type="text" name="code" value={formData.code} onChange={handleInputChange} required placeholder="vd: SV202401" />
                            </div>
                            <div className="form-group">
                                <label>Họ và Tên</label>
                                <input type="text" name="fullName" value={formData.fullName} onChange={handleInputChange} required placeholder="vd: Nguyễn Văn A" />
                            </div>
                            <div className="form-group">
                                <label>Email Nội Bộ</label>
                                <input type="email" name="email" value={formData.email} onChange={handleInputChange} required placeholder="vd: a.nguyen@edu.vn" />
                            </div>
                            <div className="form-group">
                                <label>Vai Trò</label>
                                <select name="role" value={formData.role} onChange={handleInputChange} className="custom-select">
                                    <option value="STUDENT">Học viên (STUDENT)</option>
                                    <option value="LECTURER">Giảng viên (LECTURER)</option>
                                </select>
                            </div>
                            <div className="modal-actions">
                                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>Hủy</button>
                                <button type="submit" className="btn-primary" disabled={isSubmitting}>
                                    {isSubmitting ? 'Đang xử lý...' : (isEditMode ? 'Cập nhật' : 'Lưu Tài Khoản')}
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
                        <h3 style={{ fontSize: '1.4rem', color: '#1f2937', marginBottom: '10px' }}>Xác Nhận Khóa</h3>
                        <p style={{ color: '#4b5563', marginBottom: '25px', lineHeight: '1.5' }}>
                            Bạn có chắc chắn muốn khóa tài khoản:<br />
                            <strong style={{ color: '#ef4444', fontSize: '1.1rem' }}>{userToDelete?.email}</strong>?
                        </p>
                        <div className="modal-actions" style={{ justifyContent: 'center', gap: '12px' }}>
                            <button className="btn-secondary" style={{ padding: '10px 20px', borderRadius: '8px' }} onClick={() => setShowDeleteModal(false)}>Hủy Bỏ</button>
                            <button className="btn-primary" style={{ background: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)', border: 'none', padding: '10px 20px', borderRadius: '8px' }} onClick={executeDelete}>Khóa Tài Khoản</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UserManagement;
