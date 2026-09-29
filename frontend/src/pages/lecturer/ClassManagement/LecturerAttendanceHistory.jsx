import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './LecturerClassManagement.css'; // Reuse same styles

const LecturerAttendanceHistory = () => {
    const { classId } = useParams();
    const navigate = useNavigate();
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [filterDate, setFilterDate] = useState('');

    useEffect(() => {
        if (classId) {
            fetchHistory(filterDate);
        }
    }, [classId, filterDate]);

    const fetchHistory = async (dateStr) => {
        setLoading(true);
        try {
            let url = `/v1/attendance/class/${classId}/history`;
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
        <div className="class-management-container">
            <div className="admin-header-title" style={{ marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '15px' }}>
                <button 
                    onClick={() => navigate(-1)} 
                    style={{ background: 'none', border: 'none', color: '#6b7280', fontSize: '1.5rem', cursor: 'pointer' }}
                >
                    ⬅️
                </button>
                <div>
                    <h1>📅 Lịch sử điểm danh Lớp học</h1>
                    <p>Class ID: {classId}</p>
                </div>
            </div>

            <div className="filter-bar" style={{ display: 'flex', gap: '1rem', alignItems: 'center', marginBottom: '20px' }}>
                <label style={{ fontWeight: 600, color: '#374151' }}>Filter by Date (Lọc theo ngày):</label>
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

            {error && <div className="error-message">⚠️ {error}</div>}

            {loading ? (
                <div className="loader-container">
                    <div className="spinner"></div>
                    <p>Loading records...</p>
                </div>
            ) : history.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '4rem', background: 'white', borderRadius: '12px' }}>
                    <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>📅</div>
                    <h3 style={{ color: '#374151' }}>Không có dữ liệu điểm danh</h3>
                    <p style={{ color: '#6b7280' }}>
                        {filterDate ? `Không tìm thấy bản ghi nào vào ngày ${filterDate}.` : 'Lớp này chưa có bản ghi điểm danh nào.'}
                    </p>
                </div>
            ) : (
                <div className="management-table-card">
                    <table className="lms-table">
                        <thead>
                            <tr>
                                <th>Thời gian</th>
                                <th>Mã Sinh Viên (ID)</th>
                                <th>Họ Tên</th>
                                <th>Trạng thái</th>
                            </tr>
                        </thead>
                        <tbody>
                            {history.map((record) => (
                                <tr key={record.id}>
                                    <td style={{ fontWeight: 500 }}>
                                        {new Date(record.checkInTime).toLocaleString()}
                                    </td>
                                    <td style={{ fontFamily: 'monospace', color: '#6366f1' }}>
                                        {record.student?.id || 'N/A'}
                                    </td>
                                    <td style={{ fontWeight: 600, color: '#1f2937' }}>
                                        {record.student?.fullName || 'N/A'}
                                    </td>
                                    <td>
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

export default LecturerAttendanceHistory;
