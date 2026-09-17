import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './CourseManagement.css';

const CourseManagement = () => {
    const navigate = useNavigate();
    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [modalMode, setModalMode] = useState('add'); // 'add' or 'edit'
    const [currentCourse, setCurrentCourse] = useState({
        title: '',
        description: '',
        thumbnailUrl: '',
        isPublished: true
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isUploading, setIsUploading] = useState(false);
    const [deleteModal, setDeleteModal] = useState({ show: false, courseId: null, courseName: '' });

    // Pagination state
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        fetchCourses(0);
    }, []);

    const fetchCourses = async (page = currentPage) => {
        setLoading(true);
        try {
            const response = await axiosInstance.get('/v1/courses', {
                params: {
                    page: page,
                    size: 8
                }
            });
            if (response.data && response.data.success) {
                const pageData = response.data.data;
                setCourses(pageData.content !== undefined ? pageData.content : pageData);
                if (pageData.page) {
                    setTotalPages(pageData.page.totalPages || 0);
                    setCurrentPage(pageData.page.number || 0);
                } else if (pageData.pageable) {
                    setTotalPages(pageData.totalPages || 0);
                    setCurrentPage(pageData.pageable.pageNumber || 0);
                } else {
                    setTotalPages(pageData.totalPages || 0);
                    setCurrentPage(pageData.number || 0);
                }
            }
        } catch (error) {
            console.error('Error fetching courses:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = (e) => {
        setSearchQuery(e.target.value.toLowerCase());
    };

    const filteredCourses = courses.filter(course =>
        course.title.toLowerCase().includes(searchQuery) ||
        (course.description && course.description.toLowerCase().includes(searchQuery))
    );

    const openAddModal = () => {
        setModalMode('add');
        setCurrentCourse({ title: '', description: '', thumbnailUrl: '', isPublished: true });
        setShowModal(true);
    };

    const openEditModal = (course) => {
        setModalMode('edit');
        setCurrentCourse({
            id: course.id,
            title: course.title,
            description: course.description || '',
            thumbnailUrl: course.thumbnailUrl || '',
            isPublished: course.isPublished
        });
        setShowModal(true);
    };

    const openDeleteModal = (course) => {
        setDeleteModal({ show: true, courseId: course.id, courseName: course.title });
    };

    const handleInput = (e) => {
        const { name, value, type, checked } = e.target;
        setCurrentCourse({
            ...currentCourse,
            [name]: type === 'checkbox' ? checked : value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        try {
            if (modalMode === 'add') {
                await axiosInstance.post('/v1/courses', currentCourse);
            } else {
                await axiosInstance.put(`/v1/courses/${currentCourse.id}`, currentCourse);
            }
            setShowModal(false);
            fetchCourses();
        } catch (error) {
            console.error('Error submitting course:', error);
            alert('Error saving course!');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleDelete = async () => {
        try {
            await axiosInstance.delete(`/v1/courses/${deleteModal.courseId}`);
            setDeleteModal({ show: false, courseId: null, courseName: '' });
            fetchCourses();
        } catch (error) {
            console.error('Error deleting course:', error);
            alert('Error deleting course!');
        }
    };

    // Premium UI design helper
    const getInitials = (title) => {
        return title ? title.substring(0, 2).toUpperCase() : 'CO';
    };

    return (
        <div className="course-management-container fade-in">
            <div className="cm-header">
                <div>
                    <h1 className="cm-title">Course Management</h1>
                    <p className="cm-subtitle">Create, edit, and manage courses.</p>
                </div>
                <button className="cm-btn-primary" onClick={openAddModal}>
                    <span className="icon">➕</span> Add New Course
                </button>
            </div>

            <div className="cm-controls glass-panel">
                <div className="cm-searchbox">
                    <span className="search-icon">🔍</span>
                    <input
                        type="text"
                        placeholder="Search courses by title or description..."
                        value={searchQuery}
                        onChange={handleSearch}
                    />
                </div>
                <div className="cm-stats">
                    <span className="stat-badge">Total: <strong>{courses.length}</strong></span>
                    <span className="stat-badge active">Published: <strong>{courses.filter(c => c.isPublished).length}</strong></span>
                </div>
            </div>

            {loading ? (
                <div className="cm-loading">
                    <div className="spinner"></div>
                    <p>Loading course data...</p>
                </div>
            ) : filteredCourses.length === 0 ? (
                <div className="cm-empty glass-panel">
                    <div className="empty-icon">📂</div>
                    <h3>No courses found</h3>
                    <p>Try refining your search or add a new course.</p>
                </div>
            ) : (
                <div className="cm-grid">
                    {filteredCourses.map(course => (
                        <div key={course.id} className="cm-card glass-panel">
                            <div className="cm-card-image" style={{ backgroundImage: course.thumbnailUrl ? `url(${course.thumbnailUrl})` : 'none' }}>
                                {!course.thumbnailUrl && <div className="cm-card-initials">{getInitials(course.title)}</div>}
                                <div className={`cm-status ${course.isPublished ? 'published' : 'draft'}`}>
                                    {course.isPublished ? 'Published' : 'Draft'}
                                </div>
                            </div>
                            <div className="cm-card-content">
                                <h3 className="cm-card-title">{course.title}</h3>
                                <p className="cm-card-desc">{course.description || 'No detailed description for this course yet.'}</p>
                                <div className="cm-card-meta">
                                    <span>Created: {new Date(course.createdAt).toLocaleDateString('en-US')}</span>
                                </div>
                            </div>
                            <div className="cm-card-actions">
                                <button className="btn-icon view" onClick={() => {
                                    const user = JSON.parse(localStorage.getItem('user'));
                                    const prefix = user?.role === 'ADMIN' ? '/admin' : user?.role === 'LECTURER' ? '/lecturer' : '/student';
                                    navigate(`${prefix}/courses/${course.id}`);
                                }} title="Quản lý Bài Học">
                                    📖 Lessons
                                </button>
                                <button className="btn-icon edit" onClick={() => openEditModal(course)} title="Edit">
                                    ✏️ Edit
                                </button>
                                <button className="btn-icon delete" onClick={() => openDeleteModal(course)} title="Delete">
                                    🗑️ Delete
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Pagination Controls */}
            {totalPages > 1 && !loading && filteredCourses.length > 0 && (
                <div className="pagination-container" style={{ display: 'flex', justifyContent: 'center', marginTop: '2rem', gap: '1rem', alignItems: 'center' }}>
                    <button
                        className="cm-btn-secondary"
                        disabled={currentPage === 0}
                        onClick={() => fetchCourses(currentPage - 1)}
                    >
                        Previous
                    </button>
                    <span style={{ fontWeight: 600, color: '#374151' }}>
                        Page {currentPage + 1} of {totalPages}
                    </span>
                    <button
                        className="cm-btn-secondary"
                        disabled={currentPage === totalPages - 1}
                        onClick={() => fetchCourses(currentPage + 1)}
                    >
                        Next
                    </button>
                </div>
            )}

            {/* Course Form Modal */}
            {showModal && (
                <div className="cm-modal-overlay fade-in">
                    <div className="cm-modal slide-up">
                        <div className="cm-modal-header">
                            <h2>{modalMode === 'add' ? 'Add New Course' : 'Edit Course'}</h2>
                            <button className="cm-btn-close" onClick={() => setShowModal(false)}>✕</button>
                        </div>
                        <form onSubmit={handleSubmit} className="cm-modal-form">
                            <div className="form-group">
                                <label>Course Title <span className="required">*</span></label>
                                <input
                                    type="text"
                                    name="title"
                                    value={currentCourse.title}
                                    onChange={handleInput}
                                    placeholder="Enter course name..."
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label>Description</label>
                                <textarea
                                    name="description"
                                    value={currentCourse.description}
                                    onChange={handleInput}
                                    placeholder="Write a brief description about this course..."
                                    rows="4"
                                ></textarea>
                            </div>
                            <div className="form-group">
                                <label>Thumbnail URL</label>
                                <div style={{ display: 'flex', gap: '10px' }}>
                                    <input
                                        type="url"
                                        name="thumbnailUrl"
                                        value={currentCourse.thumbnailUrl}
                                        onChange={handleInput}
                                        placeholder="https://example.com/image.jpg"
                                        style={{ flex: 1 }}
                                    />
                                    <input 
                                        type="file" 
                                        accept="image/*"
                                        id="thumbnailUploadAdmin"
                                        style={{ display: 'none' }}
                                        onChange={async (e) => {
                                            const file = e.target.files[0];
                                            if (!file) return;
                                            setIsUploading(true);
                                            try {
                                                const presignedRes = await axiosInstance.post('/v1/courses/upload-url', {
                                                    fileName: file.name,
                                                    contentType: file.type || 'image/jpeg'
                                                });
                                                if (presignedRes.data && presignedRes.data.data) {
                                                    const { uploadUrl, fileUrl } = presignedRes.data.data;
                                                    await fetch(uploadUrl, {
                                                        method: 'PUT',
                                                        headers: { 'Content-Type': file.type || 'image/jpeg' },
                                                        body: file
                                                    });
                                                    setCurrentCourse({ ...currentCourse, thumbnailUrl: fileUrl });
                                                }
                                            } catch (err) {
                                                console.error('Error uploading thumbnail:', err);
                                                alert('Failed to upload thumbnail.');
                                            } finally {
                                                setIsUploading(false);
                                                e.target.value = null;
                                            }
                                        }}
                                    />
                                    <button 
                                        type="button" 
                                        className="btn-action-small" 
                                        onClick={() => document.getElementById('thumbnailUploadAdmin').click()}
                                        disabled={isUploading}
                                        style={{ flexShrink: 0, padding: '0 1rem', background: '#e0e7ff', color: '#4338ca', borderRadius: '4px', border: 'none', cursor: 'pointer', fontWeight: 600 }}
                                    >
                                        {isUploading ? '...' : '📁 Upload'}
                                    </button>
                                </div>
                            </div>
                            <div className="form-group checkbox-group">
                                <label className="switch">
                                    <input
                                        type="checkbox"
                                        name="isPublished"
                                        checked={currentCourse.isPublished}
                                        onChange={handleInput}
                                    />
                                    <span className="slider round"></span>
                                </label>
                                <span className="checkbox-label">Publish course (Visible to students)</span>
                            </div>

                            <div className="cm-modal-footer">
                                <button type="button" className="cm-btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                                <button type="submit" className="cm-btn-primary" disabled={isSubmitting || isUploading}>
                                    {isSubmitting || isUploading ? 'Processing...' : (modalMode === 'add' ? 'Create Course' : 'Save Changes')}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Premium Confirm Delete Modal */}
            {deleteModal.show && (
                <div className="custom-modal-overlay">
                    <div className="custom-modal error-modal">
                        <div className="cm-icon-box">
                            <div className="cm-warning-icon">!</div>
                        </div>
                        <h3>Are you sure you want to delete?</h3>
                        <p>This will permanently delete the course <strong>{deleteModal.courseName}</strong> and cannot be undone.</p>
                        <div className="custom-modal-buttons">
                            <button className="btn-cancel" onClick={() => setDeleteModal({ show: false, courseId: null, courseName: '' })}>Cancel</button>
                            <button className="btn-confirm error" onClick={handleDelete}>Delete Course</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CourseManagement;
