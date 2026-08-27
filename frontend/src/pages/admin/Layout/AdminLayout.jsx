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

    if (!user || (user.role !== 'ADMIN' && user.role !== 'LECTURER')) {
        return <div style={{ padding: '20px', color: 'red' }}>Access denied. Please log in with authorized credentials.</div>;
    }

    const isLecturer = user.role === 'LECTURER';

    return (
        <div className="admin-layout">
            <aside className="admin-sidebar glass-sidebar">
                <div className="sidebar-header">
                    <span className="sidebar-icon">{isLecturer ? '👨‍🏫' : '👨‍💼'}</span>
                    <h2>{isLecturer ? 'Lecturer Panel' : 'Admin Panel'}</h2>
                </div>
                <nav className="sidebar-nav">
                    <Link to="/admin/dashboard" className={`nav-item ${location.pathname.includes('dashboard') ? 'active' : ''}`}>
                        <span className="nav-icon">📊</span> Dashboard
                    </Link>

                    {!isLecturer && (
                        <>
                            <Link to="/admin/users" className={`nav-item ${location.pathname.includes('/admin/users') ? 'active' : ''}`}>
                                <span className="nav-icon">👥</span> User Management
                            </Link>
                            <Link to="/admin/classes-admin" className={`nav-item ${location.pathname.includes('/admin/classes-admin') ? 'active' : ''}`}>
                                <span className="nav-icon">🏫</span> Class Administration
                            </Link>
                            <Link to="/admin/courses" className={`nav-item ${location.pathname.includes('/admin/courses') ? 'active' : ''}`}>
                                <span className="nav-icon">⚙️</span> Course Management
                            </Link>
                        </>
                    )}

                    {isLecturer && (
                        <>
                            <Link to="/admin/classes" className={`nav-item ${location.pathname.includes('/admin/classes') ? 'active' : ''}`}>
                                <span className="nav-icon">👨‍🎓</span> My Classes
                            </Link>

                            <Link to="/student/courses" className={`nav-item ${location.pathname.includes('/student/courses') ? 'active' : ''}`}>
                                <span className="nav-icon">📚</span> Lessons & Content
                            </Link>

                            <Link to="/student/quizzes" className={`nav-item ${location.pathname.includes('/student/quizzes') ? 'active' : ''}`}>
                                <span className="nav-icon">📝</span> Question Bank
                            </Link>
                            <Link to="/student/assignments" className={`nav-item ${location.pathname.includes('/student/assignments') ? 'active' : ''}`}>
                                <span className="nav-icon">📑</span> Assignments
                            </Link>
                        </>
                    )}
                </nav>
            </aside>
            <main className="admin-main">
                <header className="admin-header glass-header">
                    <div className="header-search">
                        <span className="search-icon">🔍</span>
                        <input type="text" placeholder="Quick search users, courses..." />
                    </div>
                    <div className="header-profile">
                        <div className="header-info">
                            <span className="header-name">{user.fullName || user.email}</span>
                            <span className="header-role">{isLecturer ? 'Lecturer' : 'System Admin'}</span>
                        </div>
                        <div className="header-avatar">{user.fullName ? user.fullName.charAt(0).toUpperCase() : 'A'}</div>
                        <button className="btn-logout-small" onClick={handleLogout}>Logout</button>
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
