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
                        <h3>Khóa học đang tham gia</h3>
                        <p className="stat-value">4</p>
                    </div>
                </div>
                <div className="stat-card glass-panel">
                    <div className="stat-icon orange">📝</div>
                    <div className="stat-info">
                        <h3>Bài kiểm tra sắp tới</h3>
                        <p className="stat-value">2</p>
                    </div>
                </div>
                <div className="stat-card glass-panel">
                    <div className="stat-icon green">🏆</div>
                    <div className="stat-info">
                        <h3>Điểm trung bình</h3>
                        <p className="stat-value">8.5</p>
                    </div>
                </div>
            </div>

            <div className="dashboard-grid">
                <div className="main-panel glass-panel">
                    <div className="panel-header">
                        <h2>Hoạt động gần đây</h2>
                        <button className="btn-text">Xem tất cả</button>
                    </div>
                    <ul className="activity-list">
                        <li className="activity-item">
                            <span className="activity-dot green"></span>
                            <div className="activity-details">
                                <p><strong>Hoàn thành bài Quiz:</strong> React JS Basics</p>
                                <span className="activity-time">2 giờ trước</span>
                            </div>
                            <span className="activity-score text-green">9.0/10</span>
                        </li>
                        <li className="activity-item">
                            <span className="activity-dot blue"></span>
                            <div className="activity-details">
                                <p><strong>Tham gia điểm danh:</strong> Hệ quản trị CSDL</p>
                                <span className="activity-time">Hôm qua</span>
                            </div>
                            <span className="activity-status">Thành công</span>
                        </li>
                    </ul>
                </div>

                <div className="side-panel glass-panel">
                    <div className="panel-header">
                        <h2>Thông báo</h2>
                    </div>
                    <div className="notification-card">
                        <div className="notif-icon">🔔</div>
                        <p>Bài kiểm tra môn Kỹ thuật phần mềm sẽ mở vào 10:00 sáng mai.</p>
                    </div>
                    <div className="notification-card">
                        <div className="notif-icon">🎉</div>
                        <p>Kết quả bài thi cuối kỳ môn Java đã được cập nhật.</p>
                    </div>
                </div>
            </div>
            <div style={{ marginTop: '20px' }}>
                <button className="action-btn primary-gradient" onClick={() => navigate('/student/quizzes')}>Đi tới danh sách Quizzes</button>
            </div>
        </div>
    );

    const renderAdminDashboard = () => (
        <div className="dashboard-content admin-dashboard">
            <div className="stat-cards" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
                <div className="stat-card glass-panel">
                    <div className="stat-icon blue">👥</div>
                    <div className="stat-info">
                        <h3>Tổng số người dùng</h3>
                        <p className="stat-value">{adminStats.totalUsers}</p>
                    </div>
                </div>
                <div className="stat-card glass-panel">
                    <div className="stat-icon purple">📝</div>
                    <div className="stat-info">
                        <h3>Bài thi (Quizzes)</h3>
                        <p className="stat-value">{adminStats.totalQuizzes}</p>
                    </div>
                </div>
                <div className="stat-card glass-panel">
                    <div className="stat-icon yellow">📚</div>
                    <div className="stat-info">
                        <h3>Khóa học (Courses)</h3>
                        <p className="stat-value">{adminStats.totalCourses}</p>
                    </div>
                </div>
            </div>

            <div className="dashboard-grid full-width">
                <div className="main-panel glass-panel">
                    <div className="panel-header">
                        <h2>Quản lý hệ thống nhanh</h2>
                    </div>
                    <div className="quick-actions">
                        <button className="action-card">
                            <span className="action-icon">➕</span>
                            Thêm người dùng mới
                        </button>
                        <button className="action-card">
                            <span className="action-icon">📚</span>
                            Tạo khóa học
                        </button>
                        <button className="action-card">
                            <span className="action-icon">📊</span>
                            Xem báo cáo
                        </button>
                        <button className="action-card">
                            <span className="action-icon">⚙️</span>
                            Cài đặt hệ thống
                        </button>
                    </div>
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
                        <h3>Lớp đang phụ trách</h3>
                        <p className="stat-value">6</p>
                    </div>
                </div>
                <div className="stat-card glass-panel">
                    <div className="stat-icon orange">⏳</div>
                    <div className="stat-info">
                        <h3>Bài tập chờ chấm</h3>
                        <p className="stat-value">42</p>
                    </div>
                </div>
                <div className="stat-card glass-panel">
                    <div className="stat-icon green">📈</div>
                    <div className="stat-info">
                        <h3>Tỷ lệ điểm danh</h3>
                        <p className="stat-value">94%</p>
                    </div>
                </div>
            </div>

            <div className="dashboard-grid">
                <div className="main-panel glass-panel">
                    <div className="panel-header">
                        <h2>Lớp học của tôi</h2>
                    </div>
                    <div className="class-list">
                        <div className="class-card">
                            <div className="class-info">
                                <h3>Kỹ thuật phần mềm (SE101)</h3>
                                <p>45 Sinh viên</p>
                            </div>
                            <button className="btn-outline">Quản lý lớp</button>
                        </div>
                        <div className="class-card">
                            <div className="class-info">
                                <h3>Lập trình Web (WEB201)</h3>
                                <p>60 Sinh viên</p>
                            </div>
                            <button className="btn-outline">Quản lý lớp</button>
                        </div>
                        <div className="class-card">
                            <div className="class-info">
                                <h3>Cơ sở dữ liệu nâng cao (DB301)</h3>
                                <p>38 Sinh viên</p>
                            </div>
                            <button className="btn-outline">Quản lý lớp</button>
                        </div>
                    </div>
                </div>
                <div className="side-panel glass-panel">
                    <div className="panel-header">
                        <h2>Công việc cần làm</h2>
                    </div>
                    <div className="todo-item">
                        <input type="checkbox" id="todo1" />
                        <label htmlFor="todo1">Chấm điểm Quiz giữa kỳ SE101</label>
                    </div>
                    <div className="todo-item">
                        <input type="checkbox" id="todo2" />
                        <label htmlFor="todo2">Chuẩn bị bài giảng WEB201</label>
                    </div>
                    <div className="todo-item">
                        <input type="checkbox" id="todo3" />
                        <label htmlFor="todo3">Kiểm tra kết quả điểm danh tuần 4</label>
                    </div>
                </div>
            </div>
        </div>
    );

    return (
        <div className="dashboard-container">
            <div className="dashboard-header">
                <div className="welcome-section">
                    <h1 className="welcome-text">
                        Xin chào, <span className="highlight">{user.fullName || user.email || 'Người dùng'}</span> 👋
                    </h1>
                    <p className="subtitle">
                        Chào mừng bạn trở lại hệ thống Educo LMS (Vai trò: {user.role})
                    </p>
                </div>
                <div className="date-widget">
                    {new Date().toLocaleDateString('vi-VN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
                </div>
            </div>

            {user.role === 'STUDENT' && renderStudentDashboard()}
            {user.role === 'ADMIN' && renderAdminDashboard()}
            {user.role === 'LECTURER' && renderLecturerDashboard()}
        </div>
    );
};

export default Dashboard;
