import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Home.css';

const Home = () => {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            setUser(JSON.parse(storedUser));
        }
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        setUser(null);
        navigate('/');
    };

    const handleGoToSystem = () => {
        if (!user) return;
        if (user.role === 'STUDENT') navigate('/student/student-home');
        else if (user.role === 'ADMIN') navigate('/admin/dashboard');
        else if (user.role === 'LECTURER') navigate('/lecturer-home');
    };

    return (
        <div className="home-container">
            {/* Navbar Mẫu */}
            <nav className="home-navbar">
                <div className="nav-logo">
                    <span className="logo-icon">🏫</span> Educo LMS
                </div>
                <div className="nav-links">
                    <a href="#features">Features</a>
                    <a href="#about">About Us</a>
                    {user ? (
                        <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
                            <span style={{ fontWeight: '600', color: '#10b981' }}>Welcome, {user.fullName || user.email}</span>
                            <button className="nav-login-btn" style={{ background: '#3b82f6', color: 'white', border: 'none' }} onClick={handleGoToSystem}>
                                Go to System
                            </button>
                            <button className="nav-login-btn" style={{ background: '#ef4444', color: 'white', border: 'none' }} onClick={handleLogout}>
                                Logout
                            </button>
                        </div>
                    ) : (
                        <button className="nav-login-btn" onClick={() => navigate('/login')}>
                            Login
                        </button>
                    )}
                </div>
            </nav>

            {/* Vùng Hero Trung tâm (Banner chính) */}
            <main className="hero-section">
                <div className="hero-content">
                    <div className="badge-animated">🚀 The Future of Education</div>
                    <h1>
                        Smart Attendance <br />
                        <span className="text-gradient">Powered by AI</span>
                    </h1>
                    <p>
                        Advanced Learning Management System integrated with artificial intelligence for instant facial recognition,
                        completely preventing fraud with just a camera scan!
                    </p>
                    <div className="hero-actions">
                        {user ? (
                            <button className="primary-btn" onClick={handleGoToSystem}>
                                Continue Learning 👋
                            </button>
                        ) : (
                            <button className="primary-btn" onClick={() => navigate('/login')}>
                                Get Started 👋
                            </button>
                        )}
                        <button className="secondary-btn">View Guide</button>
                    </div>
                </div>

                {/* Hình minh họa phong cách kính mờ */}
                <div className="hero-graphic">
                    <div className="glass-card main-card">
                        <div className="card-header">
                            <span className="dot red"></span>
                            <span className="dot yellow"></span>
                            <span className="dot green"></span>
                        </div>
                        <div className="scan-animation">
                            <div className="face-frame">
                                <div className="scan-line"></div>
                                👤
                            </div>
                            <div className="status success">Identity Match: Face (100%)</div>
                        </div>
                    </div>

                    <div className="floating-bubble bubble-1"></div>
                    <div className="floating-bubble bubble-2"></div>
                </div>
            </main>
        </div>
    );
};

export default Home;
