import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import '../../student/Course/CourseStyles.css';

const StudentAttendanceHistory = () => {
    const navigate = useNavigate();
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [filterDate, setFilterDate] = useState('');

    useEffect(() => {
        fetchHistory(filterDate);
    }, [filterDate]);

    const fetchHistory = async (dateStr) => {
        setLoading(true);
        try {
            let url = '/v1/attendance/my-history';
            if (dateStr) {
                url += `?date=${dateStr}`;
            }
            const res = await axiosInstance.get(url);
            if (res.data && res.data.success) {
                setHistory(res.data.data || []);
            }
        } catch (err) {
            console.error('Error fetching attendance history:', err);
            setError('Could not load attendance history.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="course-list-container">
            <div className="course-list-header" style={{ marginBottom: '2rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <button 
                        onClick={() => navigate(-1)} 
                        style={{ background: 'none', border: 'none', color: '#6b7280', fontSize: '1.5rem', cursor: 'pointer' }}
                    >
                        ⬅️
                    </button>
                    <div>
                        <h1 style={{ fontSize: '2.5rem', fontWeight: 800, color: '#1f2937', margin: 0 }}>
                            🕒 My Attendance History
                        </h1>
                        <p style={{ color: '#6b7280', fontSize: '1.1rem', margin: 0, marginTop: '0.5rem' }}>
                            View your attendance records across all classes
                        </p>
                    </div>
                </div>
                
                <div style={{ marginTop: '1.5rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <label style={{ fontWeight: 600, color: '#374151' }}>Filter by Date:</label>
                    <input 
                        type="date" 
                        value={filterDate}
                        onChange={(e) => setFilterDate(e.target.value)}
                        style={{ padding: '8px 12px', border: '1px solid #d1d5db', borderRadius: '6px', fontSize: '1rem' }}
                    />
                    {filterDate && (
                        <button 
                            onClick={() => setFilterDate('')}
                            style={{ padding: '8px 12px', background: '#fef2f2', color: '#dc2626', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 600 }}
                        >
                            Clear Filter
                        </button>
                    )}
                </div>
            </div>

            {error && <div className="error-message">⚠️ {error}</div>}

            {loading ? (
                <div className="loader-container" style={{ minHeight: '300px' }}>
                    <div className="spinner"></div>
                    <p>Loading records...</p>
                </div>
            ) : history.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '4rem', background: 'white', borderRadius: '12px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)' }}>
                    <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>📅</div>
                    <h3 style={{ color: '#374151', marginBottom: '0.5rem' }}>No Attendance Records</h3>
                    <p style={{ color: '#6b7280' }}>
                        {filterDate ? `No records found for ${filterDate}.` : 'You have no attendance records yet.'}
                    </p>
                </div>
            ) : (
                <div style={{ background: 'white', borderRadius: '12px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', overflow: 'hidden' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                        <thead style={{ background: '#f9fafb', borderBottom: '2px solid #e5e7eb' }}>
                            <tr>
                                <th style={{ padding: '1rem', fontWeight: 600, color: '#4b5563' }}>Date & Time</th>
                                <th style={{ padding: '1rem', fontWeight: 600, color: '#4b5563' }}>Class</th>
                                <th style={{ padding: '1rem', fontWeight: 600, color: '#4b5563' }}>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {history.map((record) => (
                                <tr key={record.id} style={{ borderBottom: '1px solid #e5e7eb' }}>
                                    <td style={{ padding: '1rem', color: '#111827', fontWeight: 500 }}>
                                        {new Date(record.checkInTime).toLocaleString()}
                                    </td>
                                    <td style={{ padding: '1rem', color: '#4b5563' }}>
                                        {record.schoolClass?.className || 'N/A'}
                                    </td>
                                    <td style={{ padding: '1rem' }}>
                                        <span style={{ 
                                            padding: '4px 8px', 
                                            borderRadius: '9999px', 
                                            fontSize: '0.85rem', 
                                            fontWeight: 600,
                                            background: record.status === 'PRESENT' ? '#d1fae5' : '#fee2e2',
                                            color: record.status === 'PRESENT' ? '#065f46' : '#991b1b'
                                        }}>
                                            {record.status}
                                        </span>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default StudentAttendanceHistory;
