import React, { useState, useEffect } from 'react';
import axiosInstance from '../../../api/axios';
import './ClassManagement.css';

const LecturerClassManagement = () => {
    const [activeTab, setActiveTab] = useState('courses'); // 'courses' or 'homeroom'

    // Courses state
    const [courses, setCourses] = useState([]);
    const [selectedCourseId, setSelectedCourseId] = useState('');
    const [courseStudents, setCourseStudents] = useState([]);
    const [loadingCourses, setLoadingCourses] = useState(true);

    // Homeroom state
    const [homerooms, setHomerooms] = useState([]);
    const [selectedHomeroomId, setSelectedHomeroomId] = useState('');
    const [homeroomStudents, setHomeroomStudents] = useState([]);
    const [loadingHomerooms, setLoadingHomerooms] = useState(true);

    const [loadingStudents, setLoadingStudents] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchCourses();
        fetchHomerooms();
    }, []);

    const fetchCourses = async () => {
        setLoadingCourses(true);
        try {
            const res = await axiosInstance.get('/v1/lecturer/classes');
            if (res.data && res.data.data) {
                setCourses(res.data.data);
                if (res.data.data.length > 0) {
                    const initialId = res.data.data[0].id;
                    setSelectedCourseId(initialId);
                    fetchStudents(initialId, 'courses');
                }
            }
        } catch (err) {
            console.error('Error fetching lecturer courses:', err);
            setError('Unable to load assigned courses.');
        } finally {
            setLoadingCourses(false);
        }
    };

    const fetchHomerooms = async () => {
        setLoadingHomerooms(true);
        try {
            const res = await axiosInstance.get('/v1/lecturer/classes/homeroom');
            if (res.data && res.data.data) {
                setHomerooms(res.data.data);
                if (res.data.data.length > 0) {
                    const initialId = res.data.data[0].id;
                    setSelectedHomeroomId(initialId);
                    fetchStudents(initialId, 'homeroom');
                }
            }
        } catch (err) {
            console.error('Error fetching homeroom classes:', err);
        } finally {
            setLoadingHomerooms(false);
        }
    };

    const fetchStudents = async (id, type) => {
        if (!id) return;
        setLoadingStudents(true);
        try {
            const url = type === 'courses'
                ? `/v1/lecturer/classes/${id}/students`
                : `/v1/lecturer/classes/homeroom/${id}/students`;

            const res = await axiosInstance.get(url);
            if (res.data && res.data.data) {
                if (type === 'courses') setCourseStudents(res.data.data);
                else setHomeroomStudents(res.data.data);
            }
        } catch (err) {
            console.error('Error fetching students:', err);
            setError('Unable to load students.');
        } finally {
            setLoadingStudents(false);
        }
    };

    const handleSelectChange = (e) => {
        const id = e.target.value;
        if (activeTab === 'courses') {
            setSelectedCourseId(id);
            fetchStudents(id, 'courses');
        } else {
            setSelectedHomeroomId(id);
            fetchStudents(id, 'homeroom');
        }
    };

    const currentData = activeTab === 'courses' ? courses : homerooms;
    const currentStudents = activeTab === 'courses' ? courseStudents : homeroomStudents;
    const currentSelection = activeTab === 'courses' ? selectedCourseId : selectedHomeroomId;
    const isLoadingData = activeTab === 'courses' ? loadingCourses : loadingHomerooms;

    // Find active metadata
    const activeItemMeta = currentData.find(item => item.id === currentSelection);

    return (
        <div className="class-management-container">
            <div className="admin-header-title" style={{ marginBottom: '20px' }}>
                <h1>👨‍🏫 Class & Student Management</h1>
                <p>Manage enrolled students across your teaching courses and homeroom classes</p>
            </div>

            {/* Custom Tabs */}
            <div style={{ display: 'flex', gap: '10px', marginBottom: '25px', borderBottom: '2px solid #e2e8f0', paddingBottom: '0' }}>
                <button
                    onClick={() => { setActiveTab('courses'); if (selectedCourseId) fetchStudents(selectedCourseId, 'courses'); }}
                    style={{
                        padding: '12px 20px',
                        background: 'none',
                        border: 'none',
                        borderBottom: activeTab === 'courses' ? '3px solid #6366f1' : '3px solid transparent',
                        color: activeTab === 'courses' ? '#4f46e5' : '#64748b',
                        fontWeight: activeTab === 'courses' ? '700' : '500',
                        fontSize: '1.05rem',
                        cursor: 'pointer',
                        transition: 'all 0.3s'
                    }}
                >
                    📚 Teaching Courses
                </button>
                <button
                    onClick={() => { setActiveTab('homeroom'); if (selectedHomeroomId) fetchStudents(selectedHomeroomId, 'homeroom'); }}
                    style={{
                        padding: '12px 20px',
                        background: 'none',
                        border: 'none',
                        borderBottom: activeTab === 'homeroom' ? '3px solid #6366f1' : '3px solid transparent',
                        color: activeTab === 'homeroom' ? '#4f46e5' : '#64748b',
                        fontWeight: activeTab === 'homeroom' ? '700' : '500',
                        fontSize: '1.05rem',
                        cursor: 'pointer',
                        transition: 'all 0.3s'
                    }}
                >
                    🏫 My Homeroom Classes
                </button>
            </div>

            {error && <div className="error-message" style={{ marginBottom: '20px' }}>⚠️ {error}</div>}

            <div className="filter-bar" style={{ display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap', marginBottom: '20px' }}>
                <div style={{ flex: 1, minWidth: '300px' }}>
                    <label style={{ display: 'block', fontSize: '0.9rem', fontWeight: 600, color: '#374151', marginBottom: '0.5rem' }}>
                        Select {activeTab === 'courses' ? 'Assigned Course' : 'Homeroom Class'}:
                    </label>
                    <select
                        className="modal-form-control"
                        style={{ background: 'white', padding: '0.75rem', width: '100%', borderRadius: '8px', border: '1px solid #d1d5db' }}
                        value={currentSelection}
                        onChange={handleSelectChange}
                        disabled={isLoadingData}
                    >
                        {currentData.length === 0 && <option>No {activeTab} available</option>}
                        {currentData.map(c => (
                            <option key={c.id} value={c.id}>
                                {activeTab === 'courses' ? c.title : `${c.className} - ${c.majorName} (Khóa ${c.entryYear})`}
                            </option>
                        ))}
                    </select>
                </div>

                {activeItemMeta && (
                    <div className="stat-card" style={{ padding: '0.75rem 1.5rem', background: '#e0e7ff', borderRadius: '12px', border: '1px solid #c7d2fe' }}>
                        <div style={{ fontSize: '0.85rem', color: '#4338ca', fontWeight: 600 }}>Total Students</div>
                        <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#3730a3' }}>{currentStudents.length}</div>
                    </div>
                )}
            </div>

            {loadingStudents ? (
                <div className="loader-container">
                    <div className="spinner"></div>
                    <p>Loading {activeTab} students...</p>
                </div>
            ) : currentData.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '3rem', background: 'white', borderRadius: '12px', color: '#6b7280' }}>
                    <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📭</div>
                    <h3>No {activeTab} assigned to you!</h3>
                    <p>You have not been assigned to any {activeTab === 'courses' ? 'teaching courses' : 'homeroom classes'} yet.</p>
                </div>
            ) : currentStudents.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '3rem', background: 'white', borderRadius: '12px', color: '#6b7280' }}>
                    <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📭</div>
                    <h3>No students found!</h3>
                    <p>This {activeTab === 'courses' ? 'course' : 'class'} currently has no students.</p>
                </div>
            ) : (
                <div className="management-table-card">
                    <table className="lms-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>ID</th>
                                <th>Full Name</th>
                                <th>Email</th>
                                <th>Phone</th>
                                <th>Homeroom Class</th>
                                {activeTab === 'courses' && (
                                    <>
                                        <th style={{ textAlign: 'center' }}>Avg Score</th>
                                        <th style={{ textAlign: 'center' }}>Attendance</th>
                                    </>
                                )}
                            </tr>
                        </thead>
                        <tbody>
                            {currentStudents.map((student, idx) => (
                                <tr key={student.id}>
                                    <td><strong>{idx + 1}</strong></td>
                                    <td><span style={{ fontFamily: 'monospace', color: '#6366f1' }}>{student.id}</span></td>
                                    <td>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                            {student.avatarUrl ? (
                                                <img src={student.avatarUrl} alt="Avatar" style={{ width: '30px', height: '30px', borderRadius: '50%', objectFit: 'cover' }} />
                                            ) : (
                                                <div style={{ width: '30px', height: '30px', borderRadius: '50%', background: '#6366f1', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', fontSize: '12px' }}>
                                                    {student.fullName ? student.fullName.charAt(0).toUpperCase() : 'S'}
                                                </div>
                                            )}
                                            <span style={{ fontWeight: 600, color: '#1f2937' }}>{student.fullName || 'Not updated'}</span>
                                        </div>
                                    </td>
                                    <td>{student.email}</td>
                                    <td>{student.phone || '--'}</td>
                                    <td>
                                        <span style={{ background: '#f3f4f6', padding: '4px 8px', borderRadius: '4px', fontSize: '0.85rem', fontWeight: 500 }}>
                                            {student.className !== 'N/A' && student.className != null ? student.className : (
                                                <i style={{ color: '#9ca3af' }}>N/A</i>
                                            )}
                                        </span>
                                    </td>
                                    {activeTab === 'courses' && (
                                        <>
                                            <td style={{ textAlign: 'center' }}><span style={{ fontWeight: 700, color: '#10b981' }}>{student.averageScore}</span></td>
                                            <td style={{ textAlign: 'center' }}><span style={{ fontWeight: 700, color: '#3b82f6' }}>{student.attendanceRate}%</span></td>
                                        </>
                                    )}
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default LecturerClassManagement;
