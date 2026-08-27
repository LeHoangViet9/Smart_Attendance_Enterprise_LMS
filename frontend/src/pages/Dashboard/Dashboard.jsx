import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../api/axios';
import './Dashboard.css';

const Dashboard = () => {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [adminStats, setAdminStats] = useState({
        totalUsers: 0,
        totalQuizzes: 0,
        totalCourses: 0,
        totalSubmissions: 0
    });
    const [adminNotifications, setAdminNotifications] = useState([]);

    // Lecturer Stats State
    const [lecturerStats, setLecturerStats] = useState({
        totalCourses: 0,
        pendingGradingSubmissions: 0,
        attendanceRate: '0%',
        activeCourses: []
    });

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            const parsed = JSON.parse(storedUser);
            setUser(parsed);
            if (parsed.role === 'ADMIN') {
                axiosInstance.get('/admin/stats').then(res => {
                    if (res.data && res.data.success) {
                        setAdminStats(res.data.data);
                    }
                }).catch(err => console.error("Error fetching admin stats:", err));

                axiosInstance.get('/v1/notifications').then(res => {
                    if (res.data && res.data.success) {
                        setAdminNotifications(res.data.data);
                    }
                }).catch(err => console.error("Error fetching notifications:", err));
            } else if (parsed.role === 'LECTURER') {
                axiosInstance.get('/v1/lecturer/stats').then(res => {
                    if (res.data && res.data.success) {
                        setLecturerStats(res.data.data);
                    }
                }).catch(err => console.error("Error fetching lecturer stats:", err));
            }
        } else {
            navigate('/login');
        }
    }, [navigate]);

    if (!user) return <div className="loading-state">Loading...</div>;

    const renderStudentDashboard = () => (
        <div className="dashboard-content student-dashboard">
            <div className="stat-cards">
                <div className="stat-card glass-panel">
                    <div className="stat-icon purple">📚</div>
                    <div className="stat-info">
                        <h3>Enrolled Courses</h3>
                        <p className="stat-value">4</p>
                    </div>
                </div>
                <div className="stat-card glass-panel">
                    <div className="stat-icon orange">📝</div>
                    <div className="stat-info">
                        <h3>Upcoming Quizzes</h3>
                        <p className="stat-value">2</p>
                    </div>
                </div>
                <div className="stat-card glass-panel">
                    <div className="stat-icon green">🏆</div>
                    <div className="stat-info">
                        <h3>Average Score</h3>
                        <p className="stat-value">8.5</p>
                    </div>
                </div>
            </div>

            <div className="dashboard-grid">
                <div className="main-panel glass-panel">
                    <div className="panel-header">
                        <h2>Recent Activity</h2>
                        <button className="btn-text">View All</button>
                    </div>
                    <ul className="activity-list">
                        <li className="activity-item">
                            <span className="activity-dot green"></span>
                            <div className="activity-details">
                                <p><strong>Completed Quiz:</strong> React JS Basics</p>
                                <span className="activity-time">2 hours ago</span>
                            </div>
                            <span className="activity-score text-green">9.0/10</span>
                        </li>
                        <li className="activity-item">
                            <span className="activity-dot blue"></span>
                            <div className="activity-details">
                                <p><strong>Attended Session:</strong> Database Management</p>
                                <span className="activity-time">Yesterday</span>
                            </div>
                            <span className="activity-status">Success</span>
                        </li>
                    </ul>
                </div>

                <div className="side-panel glass-panel">
                    <div className="panel-header">
                        <h2>Notifications</h2>
                    </div>
                    <div className="notification-card">
                        <div className="notif-icon">🔔</div>
                        <p>Software Engineering Midterm will open at 10:00 AM tomorrow.</p>
                    </div>
                    <div className="notification-card">
                        <div className="notif-icon">🎉</div>
                        <p>Final Exam grades for Java Programming have been updated.</p>
                    </div>
                </div>
            </div>
            <div style={{ marginTop: '20px' }}>
                <button className="action-btn primary-gradient" onClick={() => navigate('/student/quizzes')}>Go to Quizzes</button>
            </div>
        </div>
    );

    const renderAdminDashboard = () => (
        <div className="dashboard-content admin-dashboard">
            <div className="stat-cards" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
                <div className="stat-card glass-panel">
                    <div className="stat-icon blue">👥</div>
                    <div className="stat-info">
                        <h3>Total Users</h3>
                        <p className="stat-value">{adminStats.totalUsers}</p>
                    </div>
                </div>
                <div className="stat-card glass-panel">
                    <div className="stat-icon purple">📝</div>
                    <div className="stat-info">
                        <h3>Total Quizzes</h3>
                        <p className="stat-value">{adminStats.totalQuizzes}</p>
                    </div>
                </div>
                <div className="stat-card glass-panel">
                    <div className="stat-icon yellow">📚</div>
                    <div className="stat-info">
                        <h3>Total Courses</h3>
                        <p className="stat-value">{adminStats.totalCourses}</p>
                    </div>
                </div>
            </div>

            <div className="dashboard-grid full-width" style={{ display: 'grid', gridTemplateColumns: 'revert' }}>
                <div className="main-panel glass-panel">
                    <div className="panel-header">
                        <h2>System Audit Logs & Reports</h2>
                        <button className="btn-text">Refresh</button>
                    </div>
                    {adminNotifications.length === 0 ? (
                        <p style={{ color: '#64748b' }}>No new notifications at this time.</p>
                    ) : (
                        <ul className="activity-list">
                            {adminNotifications.map(notif => (
                                <li key={notif.id} className="activity-item" style={{ opacity: notif.isRead ? 0.7 : 1 }}>
                                    <span className={`activity-dot ${notif.type === 'REPORT' ? 'red' : 'blue'}`}></span>
                                    <div className="activity-details">
                                        <p>
                                            <strong>{notif.title}</strong>
                                            {notif.isRead ? '' : <span className="stat-badge active" style={{ marginLeft: '10px', fontSize: '0.7em', padding: '2px 6px' }}>NEW</span>}
                                        </p>
                                        <p style={{ fontSize: '0.9em', color: '#475569', marginTop: '4px' }}>{notif.message}</p>
                                        <span className="activity-time">{new Date(notif.createdAt).toLocaleString('en-US')}</span>
                                    </div>
                                    <div className="activity-actions" style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                                        {notif.relatedCourseId && (
                                            <button className="btn-outline" onClick={() => navigate(`/student/courses/${notif.relatedCourseId}`)}>
                                                View Course
                                            </button>
                                        )}
                                        {!notif.isRead && (
                                            <button className="btn-outline" style={{ borderColor: '#6366f1', color: '#6366f1' }} onClick={async () => {
                                                await axiosInstance.put(`/v1/notifications/${notif.id}/read`);
                                                setAdminNotifications(prev => prev.map(n => n.id === notif.id ? { ...n, isRead: true } : n));
                                            }}>
                                                Mark as Read
                                            </button>
                                        )}
                                    </div>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>
        </div>
    );

    const renderLecturerDashboard = () => (
        <div className="dashboard-content lecturer-dashboard">
            <div className="stat-cards">
                <div className="stat-card glass-panel">
                    <div className="stat-icon blue">👨‍🏫</div>
                    <div className="stat-info">
                        <h3>Assigned Courses</h3>
                        <p className="stat-value">{lecturerStats.totalCourses}</p>
                    </div>
                </div>
                <div className="stat-card glass-panel">
                    <div className="stat-icon orange">⏳</div>
                    <div className="stat-info">
                        <h3>Pending Grading</h3>
                        <p className="stat-value">{lecturerStats.pendingGradingSubmissions}</p>
                    </div>
                </div>
                <div className="stat-card glass-panel">
                    <div className="stat-icon green">📈</div>
                    <div className="stat-info">
                        <h3>Attendance Rate</h3>
                        <p className="stat-value">{lecturerStats.attendanceRate}</p>
                    </div>
                </div>
            </div>

            <div className="dashboard-grid">
                <div className="main-panel glass-panel">
                    <div className="panel-header">
                        <h2>My Classes</h2>
                    </div>
                    <div className="class-list">
                        {lecturerStats.activeCourses && lecturerStats.activeCourses.length > 0 ? (
                            lecturerStats.activeCourses.map(course => (
                                <div className="class-card" key={course.id} style={{ cursor: 'pointer' }} onClick={() => navigate(`/student/courses/${course.id}`)}>
                                    <div className="class-info">
                                        <h3>{course.title}</h3>
                                        <p>{course.description ? course.description.substring(0, 50) + '...' : 'No description'}</p>
                                    </div>
                                    <button className="btn-outline">Manage Class</button>
                                </div>
                            ))
                        ) : (
                            <p style={{ color: '#64748b' }}>You have no assigned classes yet.</p>
                        )}
                    </div>
                </div>
                <div className="side-panel glass-panel">
                    <div className="panel-header">
                        <h2>To-do List</h2>
                    </div>
                    {lecturerStats.pendingGradingSubmissions > 0 ? (
                        <div className="todo-item" style={{ cursor: 'pointer' }} onClick={() => navigate('/student/assignments/manage')}>
                            <input type="checkbox" id="todo1" readOnly />
                            <label htmlFor="todo1">Grade {lecturerStats.pendingGradingSubmissions} pending submissions</label>
                        </div>
                    ) : (
                        <p style={{ color: '#64748b', fontSize: '0.9em' }}>No pending tasks at the moment.</p>
                    )}
                </div>
            </div>
        </div>
    );

    return (
        <div className="dashboard-container">
            <div className="dashboard-header">
                <div className="welcome-section">
                    <h1 className="welcome-text">
                        Welcome, <span className="highlight">{user.fullName || user.email || 'User'}</span> 👋
                    </h1>
                    <p className="subtitle">
                        Welcome back to Educo LMS (Role: {user.role})
                    </p>
                </div>
                <div className="date-widget">
                    {new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
                </div>
            </div>

            {user.role === 'STUDENT' && renderStudentDashboard()}
            {user.role === 'ADMIN' && renderAdminDashboard()}
            {user.role === 'LECTURER' && renderLecturerDashboard()}
        </div>
    );
};

export default Dashboard;
