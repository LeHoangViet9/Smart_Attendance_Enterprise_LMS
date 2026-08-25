import React from 'react';
import { Outlet, Link, useNavigate } from 'react-router-dom';
import './Layout.css';

const Layout = () => {
    const navigate = useNavigate();
    const userObj = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;
    const userName = userObj ? (userObj.fullName || 'Student') : 'Student';
    const userRole = userObj ? userObj.role : 'STUDENT';

    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        navigate('/login');
    };

    return (
        <div className="lms-layout">
            <nav className="lms-navbar">
                <div className="nav-container">
                    <Link to="/student/student-home" className="nav-logo">
                        <span className="logo-sparkle">✨</span> Edufit LMS
                    </Link>
                    <div className="nav-links">
                        <Link to="/student/courses" className="nav-link">Courses</Link>
                        <Link to="/student/quizzes" className="nav-link">Quizzes</Link>
                        <Link to="/student/assignments" className="nav-link">Assignments</Link>
                        <Link to="/student/quizzes/history" className="nav-link">My History</Link>
                        {(userRole === 'LECTURER' || userRole === 'ADMIN') && (
                            <Link to="/student/assignments/manage" className="nav-link" style={{ color: '#6366f1', fontWeight: 600 }}>
                                👩‍🏫 Chấm bài
                            </Link>
                        )}
                    </div>
                    <div className="nav-user">
                        <div className="avatar">{userName.charAt(0).toUpperCase()}</div>
                        <span className="user-name">{userName}</span>
                        <button className="btn-logout" onClick={handleLogout}>Logout</button>
                    </div>
                </div>
            </nav>
            <main className="lms-content">
                <Outlet />
            </main>
        </div>
    );
};

export default Layout;
