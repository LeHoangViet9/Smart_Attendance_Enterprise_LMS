import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './CourseStyles.css';
import '../Assignment/AssignmentStyles.css';

const CourseList = () => {
    const navigate = useNavigate();

    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [userRole, setUserRole] = useState('STUDENT');

    // Create Course Modal State
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [newCourse, setNewCourse] = useState({
        title: '',
        description: '',
        thumbnailUrl: '',
        isPublished: true
    });
    const [creating, setCreating] = useState(false);
    const [createError, setCreateError] = useState(null);

    useEffect(() => {
        const stored = localStorage.getItem('user');
        if (stored) {
            try {
                const parsed = JSON.parse(stored);
                if (parsed.role) setUserRole(parsed.role);
            } catch (e) {
                console.error(e);
            }
        }
        fetchCourses();
    }, []);

    const fetchCourses = async () => {
        setLoading(true);
        setError(null);
        try {
            const res = await axiosInstance.get('/v1/courses');
            if (res.data && res.data.data) {
                setCourses(res.data.data);
            }
        } catch (err) {
            console.error('Error fetching courses:', err);
            setError('Không thể tải danh sách khóa học. Vui lòng kiểm tra lại kết nối!');
        } finally {
            setLoading(false);
        }
    };

    const handleCreateCourse = async (e) => {
        e.preventDefault();
        setCreating(true);
        setCreateError(null);
        try {
            await axiosInstance.post('/v1/courses', newCourse);
            setShowCreateModal(false);
            setNewCourse({
                title: '',
                description: '',
                thumbnailUrl: '',
                isPublished: true
            });
            fetchCourses();
        } catch (err) {
            console.error('Error creating course:', err);
            setCreateError(err.response?.data?.message || 'Có lỗi xảy ra khi tạo khóa học');
        } finally {
            setCreating(false);
        }
    };

    const filteredCourses = courses.filter((c) =>
        c.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        c.description?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        c.lecturerName?.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="course-container">
            {/* Header */}
            <div className="course-header">
                <div className="course-header-info">
                    <h1>
                        <span>🎓</span> Khóa Học & Bài Giảng Trực Tuyến
                    </h1>
                    <p>Kho học liệu, slide bài giảng và video hướng dẫn chuyên sâu từ nhà trường</p>
                </div>
                {(userRole === 'LECTURER' || userRole === 'ADMIN') && (
                    <button
                        className="btn-card-action"
                        style={{ width: 'auto', padding: '0.75rem 1.5rem' }}
                        onClick={() => setShowCreateModal(true)}
                    >
                        ➕ Tạo khóa học mới
                    </button>
                )}
            </div>

            {/* Filter Bar */}
            <div className="filter-bar">
                <div className="search-box">
                    <span className="search-icon">🔍</span>
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Tìm kiếm khóa học theo tên, mô tả hoặc giảng viên..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <div className="stat-card" style={{ padding: '0.6rem 1.25rem' }}>
                    <div className="stat-content">
                        <div className="stat-value" style={{ fontSize: '1.25rem' }}>{courses.length}</div>
                        <div className="stat-label">Tổng số khóa học</div>
                    </div>
                </div>
            </div>

            {error && <div className="error-message"><span>⚠️</span> {error}</div>}

            {loading ? (
                <div className="loader-container">
                    <div className="spinner"></div>
                    <p>Đang tải danh sách khóa học...</p>
                </div>
            ) : filteredCourses.length === 0 ? (
                <div className="lesson-card" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
                    <div style={{ fontSize: '3.5rem', marginBottom: '1rem' }}>📚</div>
                    <h3 style={{ fontSize: '1.4rem', color: '#1f2937', margin: '0 0 0.5rem' }}>
                        Chưa có khóa học nào
                    </h3>
                    <p style={{ color: '#6b7280', margin: 0 }}>
                        {searchTerm ? 'Không tìm thấy khóa học nào phù hợp với từ khóa.' : 'Hiện tại chưa có khóa học nào được đăng tải.'}
                    </p>
                </div>
            ) : (
                <div className="course-grid">
                    {filteredCourses.map((course) => (
                        <div key={course.id} className="course-card">
                            <div className="course-thumbnail-box">
                                {course.thumbnailUrl ? (
                                    <img
                                        src={course.thumbnailUrl}
                                        alt={course.title}
                                        className="course-thumbnail-img"
                                    />
                                ) : (
                                    <span className="course-thumbnail-placeholder">📖</span>
                                )}
                                <div className="course-lesson-badge">
                                    <span>🎬</span> {course.totalLessons || 0} bài giảng
                                </div>
                            </div>

                            <div className="course-card-body">
                                <h3 className="course-card-title">{course.title}</h3>
                                <p className="course-card-desc">
                                    {course.description || 'Chưa có mô tả chi tiết cho khóa học này.'}
                                </p>

                                <div className="course-lecturer-row">
                                    <div className="lecturer-avatar-small">
                                        {(course.lecturerName || 'G').charAt(0).toUpperCase()}
                                    </div>
                                    <span className="lecturer-name-text">
                                        {course.lecturerName || 'Giảng viên'}
                                    </span>
                                </div>

                                <button
                                    className="btn-card-action"
                                    onClick={() => navigate(`/student/courses/${course.id}`)}
                                >
                                    <span>Vào học ngay</span>
                                    <span>➔</span>
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Create Course Modal */}
            {showCreateModal && (
                <div className="modal-overlay" onClick={() => setShowCreateModal(false)}>
                    <div className="modal-glass" style={{ maxWidth: '520px' }} onClick={(e) => e.stopPropagation()}>
                        <h3>📘 Tạo Khóa Học Mới</h3>
                        <p>Nhập thông tin khóa học và lưu trữ bài giảng</p>

                        {createError && (
                            <div className="error-message" style={{ marginBottom: '1rem', padding: '0.75rem' }}>
                                <span>⚠️</span> {createError}
                            </div>
                        )}

                        <form onSubmit={handleCreateCourse}>
                            <div className="modal-form-group">
                                <label>Tên khóa học *</label>
                                <input
                                    type="text"
                                    required
                                    className="modal-form-control"
                                    placeholder="vd: Lập trình Java Enterprise & Spring Boot"
                                    value={newCourse.title}
                                    onChange={(e) => setNewCourse({ ...newCourse, title: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-group">
                                <label>Mô tả khóa học</label>
                                <textarea
                                    rows="3"
                                    className="modal-form-control"
                                    placeholder="Mô tả nội dung, lộ trình khóa học..."
                                    value={newCourse.description}
                                    onChange={(e) => setNewCourse({ ...newCourse, description: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-group">
                                <label>Link ảnh đại diện (Thumbnail URL)</label>
                                <input
                                    type="url"
                                    className="modal-form-control"
                                    placeholder="https://images.unsplash.com/..."
                                    value={newCourse.thumbnailUrl}
                                    onChange={(e) => setNewCourse({ ...newCourse, thumbnailUrl: e.target.value })}
                                />
                            </div>

                            <div className="modal-actions" style={{ marginTop: '1.5rem' }}>
                                <button
                                    type="button"
                                    className="btn-cancel"
                                    onClick={() => setShowCreateModal(false)}
                                    disabled={creating}
                                >
                                    Hủy bỏ
                                </button>
                                <button
                                    type="submit"
                                    className="btn-confirm"
                                    disabled={creating}
                                >
                                    {creating ? 'Đang tạo...' : 'Tạo khóa học'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CourseList;
