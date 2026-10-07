import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import '../../student/Course/CourseStyles.css';

const StudentClassList = () => {
    const navigate = useNavigate();
    const [classes, setClasses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchMyClasses();
    }, []);

    const fetchMyClasses = async () => {
        try {
            const res = await axiosInstance.get('/v1/student/classes');
            if (res.data && res.data.success) {
                setClasses(res.data.data || []);
            }
        } catch (err) {
            console.error('Error fetching classes:', err);
            setError('Could not load your classes.');
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="loader-container">
                <div className="spinner"></div>
                <p>Loading your classes...</p>
            </div>
        );
    }

    return (
        <div className="course-list-container">
            <div className="course-list-header" style={{ marginBottom: '2rem' }}>
                <h1 style={{ fontSize: '2.5rem', fontWeight: 800, color: '#1f2937' }}>
                    📚 My Enrolled Classes
                </h1>
                <p style={{ color: '#6b7280', fontSize: '1.1rem' }}>View the classes you are currently attending</p>
                
                <div style={{ marginTop: '1rem' }}>
                    <button 
                        onClick={() => navigate('/student/attendance-history')}
                        style={{ padding: '10px 20px', background: 'linear-gradient(135deg, #6366f1, #4f46e5)', color: 'white', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer', boxShadow: '0 4px 6px rgba(99, 102, 241, 0.3)' }}
                    >
                        🕒 View My Full Attendance History
                    </button>
                </div>
            </div>

            {error && <div className="error-message">⚠️ {error}</div>}

            {classes.length === 0 && !error ? (
                <div style={{ textAlign: 'center', padding: '4rem', background: 'white', borderRadius: '12px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)' }}>
                    <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>📭</div>
                    <h3 style={{ color: '#374151', marginBottom: '0.5rem' }}>No Classes Found</h3>
                    <p style={{ color: '#6b7280' }}>You are not enrolled in any classes at the moment.</p>
                </div>
            ) : (
                <div className="course-grid">
                    {classes.map((cls) => (
                        <div key={cls.id} className="course-card">
                            <div className="course-card-content" style={{ padding: '1.5rem' }}>
                                <div className="course-card-meta" style={{ marginBottom: '1rem' }}>
                                    <span className="major-badge" style={{ background: '#e0e7ff', color: '#4338ca', padding: '4px 8px', borderRadius: '4px', fontSize: '0.8rem', fontWeight: 600 }}>
                                        {cls.majorName || 'General'}
                                    </span>
                                </div>
                                
                                <h3 className="course-card-title" style={{ fontSize: '1.25rem', fontWeight: 700, color: '#111827', marginBottom: '0.5rem' }}>
                                    {cls.className}
                                </h3>
                                
                                <p style={{ color: '#4b5563', fontSize: '0.9rem', marginBottom: '0.25rem' }}>
                                    📖 Course: <strong style={{ color: '#1f2937' }}>{cls.courseName || 'N/A'}</strong>
                                </p>
                                <p style={{ color: '#4b5563', fontSize: '0.9rem', marginBottom: '1rem' }}>
                                    👨‍🏫 Lecturer: <strong style={{ color: '#1f2937' }}>{cls.lecturerName || 'Unassigned'}</strong>
                                </p>
                                
                                <button 
                                    className="btn-card-action"
                                    onClick={() => navigate(`/student/courses/${cls.courseId}`)}
                                    style={{ width: '100%', padding: '0.75rem', background: '#f3f4f6', color: '#374151', border: 'none', borderRadius: '6px', fontWeight: 600, cursor: 'pointer', transition: 'all 0.2s' }}
                                    onMouseOver={(e) => { e.target.style.background = '#e5e7eb'; }}
                                    onMouseOut={(e) => { e.target.style.background = '#f3f4f6'; }}
                                >
                                    Go to Course Content ➔
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default StudentClassList;
