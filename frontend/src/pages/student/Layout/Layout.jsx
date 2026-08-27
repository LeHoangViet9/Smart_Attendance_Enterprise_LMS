import React, { useState } from 'react';
import { Outlet, Link, NavLink, useNavigate } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './Layout.css';

const Layout = () => {
    const navigate = useNavigate();
    const userObj = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;
    const userName = userObj ? (userObj.fullName || 'Student') : 'Student';
    const userRole = userObj ? userObj.role : 'STUDENT';
    const [showModal, setShowModal] = useState(false);
    const [pwdData, setPwdData] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
    const [alert, setAlert] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        navigate('/');
    };

    const handlePasswordChange = async (e) => {
        e.preventDefault();
        if (pwdData.newPassword !== pwdData.confirmPassword) {
            setAlert({ type: 'error', text: 'New passwords do not match!' });
            return;
        }

        setIsSubmitting(true);
        setAlert(null);
        try {
            const formData = new FormData();
            formData.append('oldPassword', pwdData.oldPassword);
            formData.append('newPassword', pwdData.newPassword);
            // using the DTO format based on typical Spring forms, or JSON?
            // AuthController expects @RequestBody ChangePasswordRequest
            const response = await axiosInstance.post('/auth/change-password', {
                oldPassword: pwdData.oldPassword,
                newPassword: pwdData.newPassword
            });
            if (response.data.success) {
                setAlert({ type: 'success', text: response.data.message || 'Password changed successfully!' });
                setTimeout(() => {
                    setShowModal(false);
                    setPwdData({ oldPassword: '', newPassword: '', confirmPassword: '' });
                }, 1500);
            }
        } catch (error) {
            setAlert({ type: 'error', text: error.response?.data?.message || 'Error changing password!' });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="lms-layout">
            <nav className="lms-navbar">
                <div className="nav-container">
                    <Link to="/student/student-home" className="nav-logo">
                        <span className="logo-sparkle">✨</span> Edufit LMS
                    </Link>
                    <div className="nav-links">
                        <NavLink to="/student/courses" className="nav-link">
                            Courses
                        </NavLink>
                        <NavLink to="/student/quizzes" end className="nav-link">
                            Quizzes
                        </NavLink>
                        <NavLink to="/student/assignments" className="nav-link">
                            Assignments
                        </NavLink>
                        {userRole === 'STUDENT' && (
                            <NavLink to="/student/quizzes/history" className="nav-link">My History</NavLink>
                        )}
                        {(userRole === 'LECTURER' || userRole === 'ADMIN') && (
                            <NavLink to="/student/assignments/manage" className="nav-link" style={{ color: '#6366f1', fontWeight: 600 }}>
                                👩‍🏫 Grading & Report
                            </NavLink>
                        )}
                    </div>
                    <div className="nav-user">
                        <div className="avatar">{userName.charAt(0).toUpperCase()}</div>
                        <span className="user-name">{userName}</span>
                        <button className="btn-logout" onClick={() => { setShowModal(true); setAlert(null); }}>Change Password</button>
                        <button className="btn-logout" onClick={handleLogout}>Logout</button>
                    </div>
                </div>
            </nav>

            <main className="lms-content">
                <Outlet />
            </main>

            {showModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h3>Change Password</h3>
                            <button className="close-btn" onClick={() => setShowModal(false)}>✕</button>
                        </div>
                        {alert && <div className={`alert-box ${alert.type}`}>{alert.text}</div>}
                        <form onSubmit={handlePasswordChange}>
                            <div className="form-group">
                                <label>Current Password</label>
                                <input type="password" value={pwdData.oldPassword} onChange={e => setPwdData({ ...pwdData, oldPassword: e.target.value })} required className="layout-input" />
                            </div>
                            <div className="form-group">
                                <label>New Password</label>
                                <input type="password" value={pwdData.newPassword} onChange={e => setPwdData({ ...pwdData, newPassword: e.target.value })} required className="layout-input" />
                            </div>
                            <div className="form-group">
                                <label>Confirm New Password</label>
                                <input type="password" value={pwdData.confirmPassword} onChange={e => setPwdData({ ...pwdData, confirmPassword: e.target.value })} required className="layout-input" />
                            </div>
                            <div className="modal-actions" style={{ marginTop: '15px' }}>
                                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                                <button type="submit" className="btn-primary" disabled={isSubmitting}>
                                    {isSubmitting ? 'Processing...' : 'Change Password'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Layout;
