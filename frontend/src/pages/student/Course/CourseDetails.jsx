import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './CourseStyles.css';
import '../Assignment/AssignmentStyles.css';

const CourseDetails = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const [course, setCourse] = useState(null);
    const [activeLesson, setActiveLesson] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [userRole, setUserRole] = useState('STUDENT');

    // Lesson Modal State (Add & Edit)
    const [showLessonModal, setShowLessonModal] = useState(false);
    const [editingLessonId, setEditingLessonId] = useState(null);
    const [lessonForm, setLessonForm] = useState({
        title: '',
        content: '',
        videoUrl: '',
        documentUrl: '',
        orderIndex: 1
    });
    const [savingLesson, setSavingLesson] = useState(false);
    const [lessonError, setLessonError] = useState(null);

    // Edit Course Modal State
    const [showEditCourseModal, setShowEditCourseModal] = useState(false);
    const [courseForm, setCourseForm] = useState({
        title: '',
        description: '',
        thumbnailUrl: ''
    });
    const [savingCourse, setSavingCourse] = useState(false);

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
        loadCourse();
    }, [id]);

    const loadCourse = async () => {
        setLoading(true);
        setError(null);
        try {
            const res = await axiosInstance.get(`/v1/courses/${id}`);
            if (res.data && res.data.data) {
                const data = res.data.data;
                setCourse(data);
                setCourseForm({
                    title: data.title || '',
                    description: data.description || '',
                    thumbnailUrl: data.thumbnailUrl || ''
                });

                if (data.lessions && data.lessions.length > 0) {
                    // Set active lesson to first lesson by default if not set
                    setActiveLesson((prev) => {
                        if (prev) {
                            const updated = data.lessions.find(l => l.id === prev.id);
                            return updated || data.lessions[0];
                        }
                        return data.lessions[0];
                    });
                }
            }
        } catch (err) {
            console.error('Error loading course:', err);
            setError('Không thể tải thông tin khóa học.');
        } finally {
            setLoading(false);
        }
    };

    // Open Lesson Modal (Create or Edit)
    const handleOpenAddLesson = () => {
        setEditingLessonId(null);
        setLessonForm({
            title: '',
            content: '',
            videoUrl: '',
            documentUrl: '',
            orderIndex: (course?.lessions?.length || 0) + 1
        });
        setLessonError(null);
        setShowLessonModal(true);
    };

    const handleOpenEditLesson = (lesson) => {
        setEditingLessonId(lesson.id);
        setLessonForm({
            title: lesson.title || '',
            content: lesson.content || '',
            videoUrl: lesson.videoUrl || '',
            documentUrl: lesson.documentUrl || '',
            orderIndex: lesson.orderIndex || 1
        });
        setLessonError(null);
        setShowLessonModal(true);
    };

    const handleSaveLesson = async (e) => {
        e.preventDefault();
        setSavingLesson(true);
        setLessonError(null);

        try {
            if (editingLessonId) {
                // Update lesson
                await axiosInstance.put(`/v1/courses/lessions/${editingLessonId}`, lessonForm);
            } else {
                // Add new lesson
                await axiosInstance.post(`/v1/courses/${id}/lessions`, lessonForm);
            }
            setShowLessonModal(false);
            loadCourse();
        } catch (err) {
            console.error('Error saving lesson:', err);
            setLessonError(err.response?.data?.message || 'Có lỗi xảy ra khi lưu bài giảng');
        } finally {
            setSavingLesson(false);
        }
    };

    const handleDeleteLesson = async (lessonId) => {
        if (!window.confirm('Bạn có chắc chắn muốn xóa bài giảng này không?')) return;
        try {
            await axiosInstance.delete(`/v1/courses/lessions/${lessonId}`);
            loadCourse();
        } catch (err) {
            console.error('Error deleting lesson:', err);
            alert('Không thể xóa bài giảng. Vui lòng thử lại!');
        }
    };

    const handleSaveCourse = async (e) => {
        e.preventDefault();
        setSavingCourse(true);
        try {
            await axiosInstance.put(`/v1/courses/${id}`, courseForm);
            setShowEditCourseModal(false);
            loadCourse();
        } catch (err) {
            console.error('Error updating course:', err);
            alert('Không thể cập nhật khóa học.');
        } finally {
            setSavingCourse(false);
        }
    };

    const handleDeleteCourse = async () => {
        if (!window.confirm('Bạn có chắc chắn muốn xóa toàn bộ khóa học này? Hành động này không thể hoàn tác.')) return;
        try {
            await axiosInstance.delete(`/v1/courses/${id}`);
            navigate('/student/courses');
        } catch (err) {
            console.error('Error deleting course:', err);
            alert('Không thể xóa khóa học.');
        }
    };

    // Helper to format YouTube or video embed URL
    const getEmbedUrl = (url) => {
        if (!url) return null;
        if (url.includes('youtube.com/watch?v=')) {
            return url.replace('watch?v=', 'embed/');
        }
        if (url.includes('youtu.be/')) {
            const videoId = url.split('youtu.be/')[1]?.split('?')[0];
            return `https://www.youtube.com/embed/${videoId}`;
        }
        return url;
    };

    if (loading) {
        return (
            <div className="loader-container">
                <div className="spinner"></div>
                <p>Đang chuẩn bị nội dung bài giảng...</p>
            </div>
        );
    }

    if (!course && !loading) {
        return (
            <div className="course-container">
                <div className="error-message">⚠️ Không tìm thấy khóa học hoặc khóa học đã bị xóa.</div>
                <button className="btn-card-action" style={{ width: '200px' }} onClick={() => navigate('/student/courses')}>
                    Quay lại danh sách
                </button>
            </div>
        );
    }

    const lessions = course.lessions || [];
    const currentIndex = lessions.findIndex(l => l.id === activeLesson?.id);
    const prevLesson = currentIndex > 0 ? lessions[currentIndex - 1] : null;
    const nextLesson = currentIndex < lessions.length - 1 ? lessions[currentIndex + 1] : null;
    const isLecturerOrAdmin = userRole === 'LECTURER' || userRole === 'ADMIN';

    return (
        <div className="course-container">
            {/* Breadcrumbs */}
            <div className="details-breadcrumb">
                <Link to="/student/student-home" className="breadcrumb-link">Trang chủ</Link>
                <span>/</span>
                <Link to="/student/courses" className="breadcrumb-link">Khóa học</Link>
                <span>/</span>
                <span>{course.title}</span>
            </div>

            {error && <div className="error-message"><span>⚠️</span> {error}</div>}

            {/* Course Header Banner */}
            <div className="course-header" style={{ marginBottom: '1.25rem' }}>
                <div className="course-header-info">
                    <h1 style={{ fontSize: '1.75rem' }}>
                        <span>📚</span> {course.title}
                    </h1>
                    <p>Giảng viên: <strong>{course.lecturerName || 'Bộ môn LMS'}</strong> • Tổng số: <strong>{lessions.length} bài giảng</strong></p>
                </div>
                {isLecturerOrAdmin && (
                    <div style={{ display: 'flex', gap: '0.75rem' }}>
                        <button
                            className="btn-card-action secondary"
                            style={{ width: 'auto', padding: '0.6rem 1.25rem' }}
                            onClick={() => setShowEditCourseModal(true)}
                        >
                            ✏️ Sửa khóa học
                        </button>
                        <button
                            className="btn-card-action secondary"
                            style={{ width: 'auto', padding: '0.6rem 1rem', color: '#ef4444' }}
                            onClick={handleDeleteCourse}
                        >
                            🗑️ Xóa
                        </button>
                    </div>
                )}
            </div>

            {/* Learning Player & Curriculum Layout */}
            <div className="learning-layout">
                {/* Left Side: Video Player & Lesson Notes */}
                <div className="learning-main">
                    {activeLesson ? (
                        <>
                            {/* Video / Slide Player Area */}
                            <div className="video-player-container">
                                {activeLesson.videoUrl ? (
                                    activeLesson.videoUrl.includes('embed') || activeLesson.videoUrl.includes('youtube') || activeLesson.videoUrl.includes('youtu.be') ? (
                                        <iframe
                                            src={getEmbedUrl(activeLesson.videoUrl)}
                                            title={activeLesson.title}
                                            className="video-iframe"
                                            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                                            allowFullScreen
                                        />
                                    ) : (
                                        <video controls className="video-iframe" src={activeLesson.videoUrl}>
                                            Trình duyệt của bạn không hỗ trợ phát video HTML5.
                                        </video>
                                    )
                                ) : (
                                    <div className="video-placeholder">
                                        <div className="video-placeholder-icon">📖</div>
                                        <h3 style={{ color: 'white', margin: '0 0 0.5rem' }}>Tài Liệu Đọc & Thực Hành</h3>
                                        <p style={{ margin: 0 }}>Bài giảng này không có video hướng dẫn. Vui lòng đọc tài liệu bên dưới.</p>
                                    </div>
                                )}
                            </div>

                            {/* Lesson Content Box */}
                            <div className="lesson-card">
                                <div className="lesson-header">
                                    <div>
                                        <span className="class-tag" style={{ marginBottom: '0.4rem', display: 'inline-block' }}>
                                            Bài {activeLesson.orderIndex || (currentIndex + 1)}
                                        </span>
                                        <h2 className="lesson-title">{activeLesson.title}</h2>
                                    </div>
                                    {isLecturerOrAdmin && (
                                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                                            <button
                                                className="btn-action-small primary"
                                                onClick={() => handleOpenEditLesson(activeLesson)}
                                            >
                                                ✏️ Sửa bài này
                                            </button>
                                            <button
                                                className="btn-action-small"
                                                style={{ background: '#fee2e2', color: '#ef4444' }}
                                                onClick={() => handleDeleteLesson(activeLesson.id)}
                                            >
                                                🗑️ Xóa
                                            </button>
                                        </div>
                                    )}
                                </div>

                                {/* Attached Document Slide */}
                                {activeLesson.documentUrl && (
                                    <div className="attachment-card" style={{ marginBottom: '1.5rem' }}>
                                        <div className="attachment-left">
                                            <div className="file-icon">📑</div>
                                            <div className="attachment-info">
                                                <h4>Slide & Tài liệu bài giảng</h4>
                                                <p>Tệp đính kèm học phần</p>
                                            </div>
                                        </div>
                                        <a
                                            href={activeLesson.documentUrl}
                                            target="_blank"
                                            rel="noreferrer"
                                            className="btn-download"
                                        >
                                            <span>⬇️</span> Tải tài liệu
                                        </a>
                                    </div>
                                )}

                                {/* Lesson Text Body */}
                                <h4 className="section-subheading">
                                    <span>📝</span> Nội dung chi tiết bài học
                                </h4>
                                <div className="lesson-body">
                                    {activeLesson.content || 'Bài giảng này chưa có ghi chú văn bản chi tiết.'}
                                </div>

                                {/* Previous / Next Lesson Navigation */}
                                <div className="lesson-navigation-footer">
                                    <button
                                        className="btn-card-action secondary"
                                        style={{ width: 'auto', opacity: prevLesson ? 1 : 0.5 }}
                                        disabled={!prevLesson}
                                        onClick={() => prevLesson && setActiveLesson(prevLesson)}
                                    >
                                        ⬅ Bài trước
                                    </button>
                                    <span style={{ fontSize: '0.9rem', color: '#6b7280', fontWeight: 600 }}>
                                        Bài {currentIndex + 1} / {lessions.length}
                                    </span>
                                    <button
                                        className="btn-card-action"
                                        style={{ width: 'auto', opacity: nextLesson ? 1 : 0.5 }}
                                        disabled={!nextLesson}
                                        onClick={() => nextLesson && setActiveLesson(nextLesson)}
                                    >
                                        Bài tiếp theo ➔
                                    </button>
                                </div>
                            </div>
                        </>
                    ) : (
                        <div className="lesson-card" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
                            <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📂</div>
                            <h3 style={{ fontSize: '1.3rem', color: '#1f2937' }}>Khóa học này chưa có bài giảng nào</h3>
                            <p style={{ color: '#6b7280' }}>
                                {isLecturerOrAdmin ? 'Hãy nhấn nút "Thêm bài giảng" bên phải để tải lên bài học đầu tiên.' : 'Vui lòng quay lại sau khi giảng viên cập nhật nội dung.'}
                            </p>
                        </div>
                    )}
                </div>

                {/* Right Side: Curriculum Sidebar */}
                <div className="curriculum-sidebar">
                    <div className="curriculum-header">
                        <h3 className="curriculum-title">📑 Danh Sách Bài Học</h3>
                        <span style={{ fontSize: '0.85rem', color: '#6366f1', fontWeight: 700 }}>
                            {lessions.length} bài
                        </span>
                    </div>

                    <div className="curriculum-list">
                        {lessions.map((lesson, idx) => {
                            const isActive = activeLesson?.id === lesson.id;
                            return (
                                <div
                                    key={lesson.id}
                                    className={`curriculum-item ${isActive ? 'active' : ''}`}
                                    onClick={() => setActiveLesson(lesson)}
                                >
                                    <div className="lesson-order-badge">
                                        {lesson.orderIndex || (idx + 1)}
                                    </div>
                                    <div className="curriculum-item-info">
                                        <h5 className="curriculum-item-title">{lesson.title}</h5>
                                        <span style={{ fontSize: '0.75rem', color: '#64748b' }}>
                                            {lesson.videoUrl ? '🎬 Video' : '📄 Tài liệu'}
                                        </span>
                                    </div>
                                </div>
                            );
                        })}
                    </div>

                    {isLecturerOrAdmin && (
                        <button className="btn-add-lesson" onClick={handleOpenAddLesson}>
                            ➕ Thêm bài giảng mới
                        </button>
                    )}
                </div>
            </div>

            {/* Modal Add/Edit Lesson */}
            {showLessonModal && (
                <div className="modal-overlay" onClick={() => setShowLessonModal(false)}>
                    <div className="modal-glass" style={{ maxWidth: '560px' }} onClick={(e) => e.stopPropagation()}>
                        <h3>{editingLessonId ? '✏️ Sửa Bài Giảng' : '➕ Thêm Bài Giảng Mới'}</h3>
                        <p>Nhập tiêu đề, đường dẫn video hoặc slide tài liệu</p>

                        {lessonError && (
                            <div className="error-message" style={{ marginBottom: '1rem', padding: '0.75rem' }}>
                                <span>⚠️</span> {lessonError}
                            </div>
                        )}

                        <form onSubmit={handleSaveLesson}>
                            <div className="modal-form-group">
                                <label>Tiêu đề bài giảng *</label>
                                <input
                                    type="text"
                                    required
                                    className="modal-form-control"
                                    placeholder="vd: Bài 1: Cấu trúc ứng dụng Spring Boot"
                                    value={lessonForm.title}
                                    onChange={(e) => setLessonForm({ ...lessonForm, title: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-group">
                                <label>Link Video bài giảng (YouTube URL / MinIO URL)</label>
                                <input
                                    type="url"
                                    className="modal-form-control"
                                    placeholder="https://www.youtube.com/watch?v=..."
                                    value={lessonForm.videoUrl}
                                    onChange={(e) => setLessonForm({ ...lessonForm, videoUrl: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-group">
                                <label>Link Slide / Tài liệu đính kèm (PDF, PPTX, Drive...)</label>
                                <input
                                    type="url"
                                    className="modal-form-control"
                                    placeholder="https://drive.google.com/..."
                                    value={lessonForm.documentUrl}
                                    onChange={(e) => setLessonForm({ ...lessonForm, documentUrl: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-group">
                                <label>Nội dung / Ghi chú bài học</label>
                                <textarea
                                    rows="4"
                                    className="modal-form-control"
                                    placeholder="Nội dung tóm tắt kiến thức cốt lõi của bài học..."
                                    value={lessonForm.content}
                                    onChange={(e) => setLessonForm({ ...lessonForm, content: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-group">
                                <label>Thứ tự hiển thị (Số nguyên)</label>
                                <input
                                    type="number"
                                    min="1"
                                    className="modal-form-control"
                                    value={lessonForm.orderIndex}
                                    onChange={(e) => setLessonForm({ ...lessonForm, orderIndex: parseInt(e.target.value) || 1 })}
                                />
                            </div>

                            <div className="modal-actions" style={{ marginTop: '1.5rem' }}>
                                <button
                                    type="button"
                                    className="btn-cancel"
                                    onClick={() => setShowLessonModal(false)}
                                    disabled={savingLesson}
                                >
                                    Hủy bỏ
                                </button>
                                <button
                                    type="submit"
                                    className="btn-confirm"
                                    disabled={savingLesson}
                                >
                                    {savingLesson ? 'Đang lưu...' : (editingLessonId ? 'Cập nhật bài giảng' : 'Thêm bài giảng')}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Modal Edit Course */}
            {showEditCourseModal && (
                <div className="modal-overlay" onClick={() => setShowEditCourseModal(false)}>
                    <div className="modal-glass" style={{ maxWidth: '500px' }} onClick={(e) => e.stopPropagation()}>
                        <h3>✏️ Chỉnh Sửa Khóa Học</h3>
                        <p>Cập nhật tiêu đề và mô tả khóa học</p>

                        <form onSubmit={handleSaveCourse}>
                            <div className="modal-form-group">
                                <label>Tên khóa học *</label>
                                <input
                                    type="text"
                                    required
                                    className="modal-form-control"
                                    value={courseForm.title}
                                    onChange={(e) => setCourseForm({ ...courseForm, title: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-group">
                                <label>Mô tả khóa học</label>
                                <textarea
                                    rows="3"
                                    className="modal-form-control"
                                    value={courseForm.description}
                                    onChange={(e) => setCourseForm({ ...courseForm, description: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-group">
                                <label>Thumbnail URL</label>
                                <input
                                    type="url"
                                    className="modal-form-control"
                                    value={courseForm.thumbnailUrl}
                                    onChange={(e) => setCourseForm({ ...courseForm, thumbnailUrl: e.target.value })}
                                />
                            </div>

                            <div className="modal-actions" style={{ marginTop: '1.5rem' }}>
                                <button
                                    type="button"
                                    className="btn-cancel"
                                    onClick={() => setShowEditCourseModal(false)}
                                    disabled={savingCourse}
                                >
                                    Hủy bỏ
                                </button>
                                <button
                                    type="submit"
                                    className="btn-confirm"
                                    disabled={savingCourse}
                                >
                                    {savingCourse ? 'Đang lưu...' : 'Lưu thay đổi'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CourseDetails;
