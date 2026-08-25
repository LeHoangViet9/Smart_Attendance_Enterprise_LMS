import React from 'react';
import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import './AdminLayout.css';

const AdminLayout = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const userString = localStorage.getItem('user');
    const user = userString ? JSON.parse(userString) : null;

    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        navigate('/');
    };

    if (!user || user.role !== 'ADMIN') {
        return <div style={{ padding: '20px', color: 'red' }}>Quyền truy cập bị từ chối. Bạn không phải là Admin.</div>;
    }

    return (
        <div className="admin-layout">
            <aside className="admin-sidebar glass-sidebar">
                <div className="sidebar-header">
                    <span className="sidebar-icon">👨‍💼</span>
                    <h2>Admin Panel</h2>
                </div>
                <nav className="sidebar-nav">
                    <Link to="/admin/dashboard" className={`nav-item ${location.pathname === '/admin/dashboard' ? 'active' : ''}`}>
                        <span className="nav-icon">📊</span> Tổng quan
                    </Link>
                    <Link to="/admin/users" className={`nav-item ${location.pathname.includes('/admin/users') ? 'active' : ''}`}>
                        <span className="nav-icon">👥</span> Quản lý User
                    </Link>
                    <Link to="/admin/courses" className={`nav-item ${location.pathname.includes('/admin/courses') ? 'active' : ''}`}>
                        <span className="nav-icon">📚</span> Khóa học
                    </Link>
                    <span className="nav-item">
                        <span className="nav-icon">⚙️</span> Cài đặt (Coming soon)
                    </span>
                </nav>
            </aside>
            <main className="admin-main">
                <header className="admin-header glass-header">
                    <div className="header-search">
                        <span className="search-icon">🔍</span>
                        <input type="text" placeholder="Tìm kiếm nhanh user, khóa học..." />
                    </div>
                    <div className="header-profile">
                        <div className="header-info">
                            <span className="header-name">{user.fullName || user.email}</span>
                            <span className="header-role">System Admin</span>
                        </div>
                        <div className="header-avatar">{user.fullName ? user.fullName.charAt(0).toUpperCase() : 'A'}</div>
                        <button className="btn-logout-small" onClick={handleLogout}>Đăng xuất</button>
                    </div>
                </header>
                <div className="admin-content-area">
                    <Outlet />
                </div>
            </main>
        </div>
    );
};

export default AdminLayout;
