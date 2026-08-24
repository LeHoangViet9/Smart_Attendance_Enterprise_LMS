import React from 'react';
import { Outlet, Link, useNavigate } from 'react-router-dom';
import './Layout.css';

const Layout = () => {
    const navigate = useNavigate();
    const userName = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).fullName : 'Student';

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
                    <Link to="/student-home" className="nav-logo">
                        <span className="logo-sparkle">✨</span> Edufit LMS
                    </Link>
                    <div className="nav-links">
                        <Link to="/student/quizzes" className="nav-link">Quizzes</Link>
                        <Link to="/student/quizzes/history" className="nav-link">My History</Link>
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
