import React from 'react';
import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import './AdminLayout.css';
import NotificationBell from '../../../components/NotificationBell/NotificationBell';

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

    if (!user) {
        return <div style={{ padding: '20px', color: 'red' }}>Access denied. Please log in.</div>;
    }

    const isLecturer = user.role === 'LECTURER';
    const isStudent = user.role === 'STUDENT';
    const basePath = isLecturer ? '/lecturer' : isStudent ? '/student' : '/admin';

    return (
        <div className="admin-layout">
            <aside className="admin-sidebar glass-sidebar">
                <div className="sidebar-header">
                    <span className="sidebar-icon">{isLecturer ? '👨‍🏫' : isStudent ? '👨‍🎓' : '👨‍💼'}</span>
                    <h2>{isLecturer ? 'Lecturer Panel' : isStudent ? 'Student Panel' : 'Admin Panel'}</h2>
                </div>
                <nav className="sidebar-nav">
                    <Link to={`${basePath}/${isStudent ? 'student-home' : 'dashboard'}`} className={`nav-item ${location.pathname.includes('dashboard') || location.pathname.includes('student-home') ? 'active' : ''}`}>
                        <span className="nav-icon">📊</span> Dashboard
                    </Link>

                    {!isLecturer && !isStudent && (
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
                            <Link to="/admin/quizzes" className={`nav-item ${location.pathname.includes('/admin/quizzes') ? 'active' : ''}`}>
                                <span className="nav-icon">📝</span> Exam Bank
                            </Link>
                            <Link to="/admin/assignments" className={`nav-item ${location.pathname.includes('/admin/assignments') ? 'active' : ''}`}>
                                <span className="nav-icon">📑</span> Assignments
                            </Link>
                        </>
                    )}

                    {isLecturer && (
                        <>
                            <Link to={`${basePath}/classes`} className={`nav-item ${location.pathname.includes(`${basePath}/classes`) ? 'active' : ''}`}>
                                <span className="nav-icon">👨‍🎓</span> My Classes
                            </Link>

                            <Link to={`${basePath}/courses`} className={`nav-item ${location.pathname.includes(`${basePath}/courses`) ? 'active' : ''}`}>
                                <span className="nav-icon">📚</span> Lessons & Content
                            </Link>

                            <Link to={`${basePath}/quizzes`} className={`nav-item ${location.pathname.includes(`${basePath}/quizzes`) ? 'active' : ''}`}>
                                <span className="nav-icon">📝</span> Exam Bank
                            </Link>
                            <Link to={`${basePath}/assignments`} className={`nav-item ${location.pathname.includes(`${basePath}/assignments`) ? 'active' : ''}`}>
                                <span className="nav-icon">📑</span> Assignments
                            </Link>
                        </>
                    )}

                    {isStudent && (
                        <>
                            <Link to={`${basePath}/courses`} className={`nav-item ${location.pathname.includes(`${basePath}/courses`) ? 'active' : ''}`}>
                                <span className="nav-icon">📚</span> My Courses
                            </Link>
                            <Link to={`${basePath}/quizzes`} className={`nav-item ${location.pathname.includes(`${basePath}/quizzes`) ? 'active' : ''}`}>
                                <span className="nav-icon">📝</span> Quizzes
                            </Link>
                            <Link to={`${basePath}/assignments`} className={`nav-item ${location.pathname.includes(`${basePath}/assignments`) ? 'active' : ''}`}>
                                <span className="nav-icon">📑</span> Assignments
                            </Link>
                            <Link to={`${basePath}/gradebook`} className={`nav-item ${location.pathname.includes(`${basePath}/gradebook`) ? 'active' : ''}`}>
                                <span className="nav-icon">📊</span> My Grades
                            </Link>
                            <Link to="/face-onboarding" className={`nav-item ${location.pathname.includes('face-onboarding') ? 'active' : ''}`}>
                                <span className="nav-icon">👤</span> Face Setup
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
                        <NotificationBell />
                        <div className="header-info" onClick={() => navigate(`${basePath}/profile`)} style={{ cursor: 'pointer' }}>
                            <span className="header-name">{user.fullName || user.email}</span>
                            <span className="header-role">{isLecturer ? 'Lecturer' : isStudent ? 'Student' : 'System Admin'}</span>
                        </div>
                        <div className="header-avatar" onClick={() => navigate(`${basePath}/profile`)} style={{ cursor: 'pointer' }}>{user.fullName ? user.fullName.charAt(0).toUpperCase() : 'A'}</div>
                        <button className="btn-logout-small" onClick={() => navigate(`${basePath}/profile`)}>Profile</button>
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
