import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './LecturerCourseManagement.css';

const LecturerCourseManagement = () => {
    const navigate = useNavigate();
    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState('');

    // Pagination state
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        fetchCourses(0);
    }, []);

    const fetchCourses = async (page = currentPage) => {
        setLoading(true);
        try {
            // Note: Ideally, this should fetch ONLY courses assigned to this lecturer.
            // Currently using the generic course endpoint until backend is updated.
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

    const getInitials = (title) => {
        return title ? title.substring(0, 2).toUpperCase() : 'CO';
    };

    return (
        <div className="course-management-container fade-in">
            <div className="cm-header">
                <div>
                    <h1 className="cm-title">📚 Course & Curriculum Management</h1>
                    <p className="cm-subtitle">View your assigned courses and manage lecture materials, videos, and documents.</p>
                </div>
                {/* Lecturers do not have Add Course button */}
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
                    <span className="stat-badge">Total Courses: <strong>{courses.length}</strong></span>
                </div>
            </div>

            {loading ? (
                <div className="cm-loading">
                    <div className="spinner"></div>
                    <p>Loading your courses...</p>
                </div>
            ) : filteredCourses.length === 0 ? (
                <div className="cm-empty glass-panel">
                    <div className="empty-icon">📂</div>
                    <h3>No courses found</h3>
                    <p>You have not been assigned to any courses yet or no courses match your search.</p>
                </div>
            ) : (
                <div className="cm-grid">
                    {filteredCourses.map(course => (
                        <div key={course.id} className="cm-card glass-panel">
                            <div className="cm-card-image" style={{ backgroundImage: course.thumbnailUrl ? `url(${course.thumbnailUrl})` : 'none' }}>
                                {!course.thumbnailUrl && <div className="cm-card-initials">{getInitials(course.title)}</div>}
                            </div>
                            <div className="cm-card-content">
                                <h3 className="cm-card-title">{course.title}</h3>
                                <p className="cm-card-desc">{course.description || 'No detailed description for this course yet.'}</p>
                            </div>
                            <div className="cm-card-actions" style={{ justifyContent: 'center' }}>
                                <button className="btn-icon view" style={{ width: '100%', borderRadius: '8px', padding: '10px', background: '#eef2ff', color: '#4f46e5', fontWeight: 600 }} onClick={() => {
                                    navigate(`/lecturer/courses/${course.id}`);
                                }}>
                                    📖 Manage Lessons & Materials
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
        </div>
    );
};

export default LecturerCourseManagement;
