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
            setError('Cannot load course list. Please check your connection!');
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
            setCreateError(err.response?.data?.message || 'Error creating course');
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
                        <span>🎓</span> {userRole === 'STUDENT' ? 'Online Courses & Lessons' : 'Course Management'}
                    </h1>
                    <p>{userRole === 'STUDENT' ? 'Learning materials, slides, and video tutorials' : 'Design syllabus, upload lectures, and manage digital content'}</p>
                </div>
                {(userRole === 'LECTURER' || userRole === 'ADMIN') && (
                    <button
                        className="btn-card-action"
                        style={{ width: 'auto', padding: '0.75rem 1.5rem' }}
                        onClick={() => setShowCreateModal(true)}
                    >
                        ➕ Create New Course
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
                        placeholder="Search courses by name, description, or lecturer..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <div className="stat-card" style={{ padding: '0.6rem 1.25rem' }}>
                    <div className="stat-content">
                        <div className="stat-value" style={{ fontSize: '1.25rem' }}>{courses.length}</div>
                        <div className="stat-label">Total Courses</div>
                    </div>
                </div>
            </div>

            {error && <div className="error-message"><span>⚠️</span> {error}</div>}

            {loading ? (
                <div className="loader-container">
                    <div className="spinner"></div>
                    <p>Loading course list...</p>
                </div>
            ) : filteredCourses.length === 0 ? (
                <div className="lesson-card" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
                    <div style={{ fontSize: '3.5rem', marginBottom: '1rem' }}>📚</div>
                    <h3 style={{ fontSize: '1.4rem', color: '#1f2937', margin: '0 0 0.5rem' }}>
                        No Courses Yet
                    </h3>
                    <p style={{ color: '#6b7280', margin: 0 }}>
                        {searchTerm ? 'No courses found matching your search.' : 'No courses have been published yet.'}
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
                                    <span>🎬</span> {course.totalLessons || 0} lessons
                                </div>
                            </div>

                            <div className="course-card-body">
                                <h3 className="course-card-title">{course.title}</h3>
                                <p className="course-card-desc">
                                    {course.description || 'No detailed description for this course yet.'}
                                </p>

                                <div className="course-lecturer-row">
                                    <div className="lecturer-avatar-small">
                                        {(course.lecturerName || 'G').charAt(0).toUpperCase()}
                                    </div>
                                    <span className="lecturer-name-text">
                                        {course.lecturerName || 'Lecturer'}
                                    </span>
                                </div>

                                <button
                                    className="btn-card-action"
                                    onClick={() => navigate(`/student/courses/${course.id}`)}
                                >
                                    <span>{userRole === 'STUDENT' ? 'Start Learning' : 'Manage Course'}</span>
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
                        <h3>📘 Create New Course</h3>
                        <p>Enter course details and organize lessons</p>

                        {createError && (
                            <div className="error-message" style={{ marginBottom: '1rem', padding: '0.75rem' }}>
                                <span>⚠️</span> {createError}
                            </div>
                        )}

                        <form onSubmit={handleCreateCourse}>
                            <div className="modal-form-group">
                                <label>Course Title *</label>
                                <input
                                    type="text"
                                    required
                                    className="modal-form-control"
                                    placeholder="e.g. Java Enterprise & Spring Boot"
                                    value={newCourse.title}
                                    onChange={(e) => setNewCourse({ ...newCourse, title: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-group">
                                <label>Course Description</label>
                                <textarea
                                    rows="3"
                                    className="modal-form-control"
                                    placeholder="Describe the course content and learning path..."
                                    value={newCourse.description}
                                    onChange={(e) => setNewCourse({ ...newCourse, description: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-group">
                                <label>Thumbnail URL</label>
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
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="btn-confirm"
                                    disabled={creating}
                                >
                                    {creating ? 'Creating...' : 'Create Course'}
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
