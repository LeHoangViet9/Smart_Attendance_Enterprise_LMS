import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './LecturerClassManagement.css';

const LecturerClassManagement = () => {
    const navigate = useNavigate();

    // Classes (Courses) state
    const [courses, setCourses] = useState([]);
    const [selectedCourseId, setSelectedCourseId] = useState('');
    const [courseStudents, setCourseStudents] = useState([]);
    const [loadingCourses, setLoadingCourses] = useState(true);
    const [loadingStudents, setLoadingStudents] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchCourses();
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
                    fetchStudents(initialId);
                }
            }
        } catch (err) {
            console.error('Error fetching lecturer classes:', err);
            setError('Unable to load assigned classes.');
        } finally {
            setLoadingCourses(false);
        }
    };

    const fetchStudents = async (id) => {
        if (!id) return;
        setLoadingStudents(true);
        try {
            const url = `/v1/lecturer/classes/${id}/students`;
            const res = await axiosInstance.get(url);
            if (res.data && res.data.data) {
                setCourseStudents(res.data.data);
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
        setSelectedCourseId(id);
        fetchStudents(id);
    };

    // Find active metadata
    const activeItemMeta = courses.find(item => item.id === selectedCourseId);

    return (
        <div className="class-management-container">
            <div className="admin-header-title" style={{ marginBottom: '20px' }}>
                <h1>👨‍🏫 Class & Student Management</h1>
                <p>Manage enrolled students across your teaching classes</p>
            </div>

            {error && <div className="error-message" style={{ marginBottom: '20px' }}>⚠️ {error}</div>}

            <div className="filter-bar" style={{ display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap', marginBottom: '20px' }}>
                <div style={{ flex: 1, minWidth: '300px' }}>
                    <label style={{ display: 'block', fontSize: '0.9rem', fontWeight: 600, color: '#374151', marginBottom: '0.5rem' }}>
                        Select Assigned Class:
                    </label>
                    <select
                        className="modal-form-control"
                        style={{ background: 'white', padding: '0.75rem', width: '100%', borderRadius: '8px', border: '1px solid #d1d5db' }}
                        value={selectedCourseId}
                        onChange={handleSelectChange}
                        disabled={loadingCourses}
                    >
                        {courses.length === 0 && <option>No classes available</option>}
                        {courses.map(c => (
                            <option key={c.id} value={c.id}>
                                {c.className} {c.courseName ? `- ${c.courseName}` : ''}
                            </option>
                        ))}
                    </select>
                </div>

                {activeItemMeta && (
                    <>
                        <div className="stat-card" style={{ padding: '0.75rem 1.5rem', background: '#e0e7ff', borderRadius: '12px', border: '1px solid #c7d2fe' }}>
                            <div style={{ fontSize: '0.85rem', color: '#4338ca', fontWeight: 600 }}>Total Students</div>
                            <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#3730a3' }}>{courseStudents.length}</div>
                        </div>
                        <div style={{ marginLeft: 'auto', display: 'flex', gap: '1rem' }}>
                            <button 
                                onClick={() => navigate(`/lecturer/classes/${selectedCourseId}/gradebook`)}
                                style={{ padding: '12px 24px', background: 'linear-gradient(135deg, #10b981, #059669)', color: 'white', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer', boxShadow: '0 4px 6px rgba(16, 185, 129, 0.3)' }}>
                                📊 Bảng điểm
                            </button>
                            <button 
                                onClick={() => navigate(`/lecturer/classes/${selectedCourseId}/smart-attendance`)}
                                style={{ padding: '12px 24px', background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)', color: 'white', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer', boxShadow: '0 4px 6px rgba(59, 130, 246, 0.3)' }}>
                                📸 Start Smart Attendance
                            </button>
                        </div>
                    </>
                )}
            </div>

            {loadingStudents ? (
                <div className="loader-container">
                    <div className="spinner"></div>
                    <p>Loading students...</p>
                </div>
            ) : courses.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '3rem', background: 'white', borderRadius: '12px', color: '#6b7280' }}>
                    <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📭</div>
                    <h3>No classes assigned to you!</h3>
                    <p>You have not been assigned to any teaching classes yet.</p>
                </div>
            ) : courseStudents.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '3rem', background: 'white', borderRadius: '12px', color: '#6b7280' }}>
                    <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📭</div>
                    <h3>No students found!</h3>
                    <p>This class currently has no students enrolled.</p>
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
                                <th style={{ textAlign: 'center' }}>Avg Score</th>
                                <th style={{ textAlign: 'center' }}>Attendance</th>
                            </tr>
                        </thead>
                        <tbody>
                            {courseStudents.map((student, idx) => (
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
                                    <td style={{ textAlign: 'center' }}><span style={{ fontWeight: 700, color: '#10b981' }}>{student.averageScore}</span></td>
                                    <td style={{ textAlign: 'center' }}><span style={{ fontWeight: 700, color: '#3b82f6' }}>{student.attendanceRate}%</span></td>
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
