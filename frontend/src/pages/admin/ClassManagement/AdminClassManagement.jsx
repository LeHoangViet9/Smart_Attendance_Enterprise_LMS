import React, { useState, useEffect } from 'react';
import axiosInstance from '../../../api/axios';
import './AdminClassManagement.css';

const AdminClassManagement = () => {
    const [classes, setClasses] = useState([]);
    const [lecturers, setLecturers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [alertMsg, setAlertMsg] = useState(null);

    // Modal state for assigning lecturer
    const [showLecturerModal, setShowLecturerModal] = useState(false);
    const [selectedClass, setSelectedClass] = useState(null);
    const [selectedLecturerId, setSelectedLecturerId] = useState('');
    const [assignLoading, setAssignLoading] = useState(false);
    const [autoAssignLoading, setAutoAssignLoading] = useState(false);
    const [confirmModal, setConfirmModal] = useState({ isOpen: false, title: '', message: '', action: null });

    useEffect(() => {
        loadClasses();
        loadLecturers();
    }, []);

    const loadClasses = async () => {
        try {
            setLoading(true);
            const response = await axiosInstance.get('/v1/admin/classes');
            if (response.data.success) {
                setClasses(response.data.data);
            }
        } catch (error) {
            console.error('Error loading classes:', error);
            setAlertMsg({ type: 'error', text: 'Failed to load administrative classes.' });
        } finally {
            setLoading(false);
        }
    };

    const loadLecturers = async () => {
        try {
            const response = await axiosInstance.get('/v1/users', {
                params: { role: 'LECTURER', size: 100 }
            });
            if (response.data.success) {
                setLecturers(response.data.data.content);
            }
        } catch (error) {
            console.error('Error loading lecturers:', error);
        }
    };

    const openAssignModal = (cls) => {
        setSelectedClass(cls);
        setSelectedLecturerId(cls.homeroomLecturerId || '');
        setShowLecturerModal(true);
    };

    const handleAssignLecturer = async (e) => {
        e.preventDefault();
        if (!selectedLecturerId) return;

        setAssignLoading(true);
        try {
            const response = await axiosInstance.put(`/v1/admin/classes/${selectedClass.id}/lecturer/${selectedLecturerId}`);
            if (response.data.success) {
                setAlertMsg({ type: 'success', text: 'Homeroom lecturer assigned successfully.' });
                setShowLecturerModal(false);
                loadClasses();
            }
        } catch (error) {
            setAlertMsg({ type: 'error', text: error.response?.data?.message || 'Failed to assign lecturer.' });
        } finally {
            setAssignLoading(false);
            setTimeout(() => setAlertMsg(null), 3000);
        }
    };

    const handleAutoAssign = () => {
        setConfirmModal({
            isOpen: true,
            title: '🪄 Phân lớp tự động',
            message: 'Tự động quét tất cả học sinh chưa có lớp và sắp xếp vào các lớp học theo tỷ lệ tối đa 30 sinh viên/lớp dựa trên Chuyên Ngành. Xác nhận thực hiện?',
            action: async () => {
                setConfirmModal({ isOpen: false });
                setAutoAssignLoading(true);
                try {
                    const response = await axiosInstance.post('/v1/admin/classes/auto-assign');
                    if (response.data.success) {
                        setAlertMsg({ type: 'success', text: response.data.message });
                        loadClasses();
                    }
                } catch (error) {
                    setAlertMsg({ type: 'error', text: 'Failed to auto-assign students.' });
                } finally {
                    setAutoAssignLoading(false);
                    setTimeout(() => setAlertMsg(null), 8000);
                }
            }
        });
    };

    const handleAutoAssignLecturers = () => {
        setConfirmModal({
            isOpen: true,
            title: '👨‍🏫 Gán Giảng Viên Chủ Nhiệm Tự Động',
            message: 'Tự động gán Giảng Viên Chủ Nhiệm cho các lớp còn trống dựa theo Chuyên Ngành sao cho phù hợp nhất. Xác nhận thực hiện?',
            action: async () => {
                setConfirmModal({ isOpen: false });
                setAutoAssignLoading(true);
                try {
                    const response = await axiosInstance.post('/v1/admin/classes/auto-assign-lecturers');
                    if (response.data.success) {
                        setAlertMsg({ type: 'success', text: response.data.message });
                        loadClasses();
                    }
                } catch (error) {
                    setAlertMsg({ type: 'error', text: 'Thất bại khi gán giảng viên.' });
                } finally {
                    setAutoAssignLoading(false);
                    setTimeout(() => setAlertMsg(null), 8000);
                }
            }
        });
    };

    return (
        <div className="admin-class-management" style={{ padding: '30px', animation: 'fadeIn 0.5s ease-in' }}>
            <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
                <div>
                    <h2 style={{ color: '#0f172a', margin: '0 0 8px 0', fontSize: '1.8rem', fontWeight: '800' }}>Class Administration</h2>
                    <p style={{ color: '#64748b', margin: 0, fontSize: '0.95rem' }}>Manage homeroom lecturers and auto-distribute unassigned students.</p>
                </div>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button
                        className="btn-assign"
                        onClick={handleAutoAssignLecturers}
                        disabled={autoAssignLoading}
                        style={{ border: '2px solid #3b82f6', color: '#1d4ed8', background: '#eff6ff' }}
                    >
                        👨‍🏫 Auto-Assign Lecturers
                    </button>
                    <button
                        className="btn-primary-magic"
                        onClick={handleAutoAssign}
                        disabled={autoAssignLoading}
                    >
                        {autoAssignLoading ? '⏳ Processing distribution...' : '🪄 Auto-Assign Students (Max 30)'}
                    </button>
                </div>
            </div>

            {alertMsg && (
                <div style={{ padding: '16px 20px', borderRadius: '10px', marginBottom: '24px', background: alertMsg.type === 'error' ? '#fef2f2' : '#ecfdf5', color: alertMsg.type === 'error' ? '#991b1b' : '#065f46', border: `1px solid ${alertMsg.type === 'error' ? '#fecaca' : '#a7f3d0'}`, display: 'flex', alignItems: 'center', gap: '10px', fontWeight: '500' }}>
                    <span>{alertMsg.type === 'error' ? '⚠️' : '✅'}</span>
                    {alertMsg.text}
                </div>
            )}

            <div className="table-card" style={{ background: 'white', borderRadius: '16px', overflow: 'hidden' }}>
                {loading ? (
                    <div style={{ padding: '60px', textAlign: 'center', color: '#94a3b8', fontSize: '1.1rem' }}>
                        <div style={{ fontSize: '2rem', marginBottom: '10px' }}>📚</div>
                        Loading classroom data...
                    </div>
                ) : (
                    <table>
                        <thead>
                            <tr>
                                <th>Class Name</th>
                                <th>Major</th>
                                <th>Entry Year</th>
                                <th>Capacity / Sĩ số</th>
                                <th>Homeroom Lecturer</th>
                                <th style={{ textAlign: 'right' }}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {classes.map((cls) => (
                                <tr key={cls.id}>
                                    <td style={{ fontWeight: '700', color: '#1e293b', fontSize: '1.05rem' }}>{cls.className}</td>
                                    <td>
                                        <span className="major-badge">
                                            {cls.majorName}
                                        </span>
                                    </td>
                                    <td style={{ color: '#64748b', fontWeight: '500' }}>{cls.entryYear}</td>
                                    <td>
                                        <div className="capacity-container">
                                            <div className="capacity-bar-bg">
                                                <div className={`capacity-bar-fill ${cls.studentCount >= 30 ? 'full' : 'normal'}`} style={{ width: `${Math.min(100, (cls.studentCount / 30) * 100)}%` }}></div>
                                            </div>
                                            <span className="capacity-text">{cls.studentCount} / 30</span>
                                        </div>
                                    </td>
                                    <td>
                                        {cls.homeroomLecturerName ? (
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                                <div style={{ width: '32px', height: '32px', borderRadius: '50%', background: '#f1f5f9', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1rem' }}>👨‍🏫</div>
                                                <span style={{ fontWeight: '600', color: '#334155' }}>
                                                    {cls.homeroomLecturerName}
                                                </span>
                                            </div>
                                        ) : (
                                            <span style={{ color: '#ef4444', fontStyle: 'italic', fontWeight: '500', background: '#fef2f2', padding: '4px 8px', borderRadius: '6px' }}>Unassigned</span>
                                        )}
                                    </td>
                                    <td style={{ textAlign: 'right' }}>
                                        <button
                                            className="btn-assign"
                                            onClick={() => openAssignModal(cls)}
                                        >
                                            ✏️ Assign Lecturer
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            {classes.length === 0 && (
                                <tr>
                                    <td colSpan="6" style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>
                                        <div style={{ fontSize: '2.5rem', opacity: 0.5, marginBottom: '10px' }}>🏫</div>
                                        No classes configured. Classes will be auto-generated when you auto-assign students.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                )}
            </div>

            {/* Modal */}
            {showLecturerModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <h3 style={{ marginTop: 0, color: '#0f172a', marginBottom: '8px', fontSize: '1.4rem' }}>Assign Homeroom</h3>
                        <p style={{ color: '#64748b', fontSize: '0.95rem', marginBottom: '20px', lineHeight: '1.5' }}>
                            Select the advising lecturer to lead and manage class <strong style={{ color: '#0f172a' }}>{selectedClass?.className}</strong>.
                        </p>

                        <form onSubmit={handleAssignLecturer}>
                            <select
                                className="form-select"
                                value={selectedLecturerId}
                                onChange={(e) => setSelectedLecturerId(e.target.value)}
                                required
                            >
                                <option value="">-- Choose Lecturer --</option>
                                {lecturers.map(l => (
                                    <option key={l.userId} value={l.userId}>
                                        {l.fullName} (Code: {l.code}) - {l.major || 'Chưa định danh ngành'} ({l.degree || 'Không có bằng cấp'})
                                    </option>
                                ))}
                            </select>

                            <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end', marginTop: '10px' }}>
                                <button type="button" onClick={() => setShowLecturerModal(false)} style={{ padding: '0.75rem 1.25rem', border: '1px solid #cbd5e1', background: 'white', color: '#475569', borderRadius: '8px', cursor: 'pointer', fontWeight: '600' }}>Cancel</button>
                                <button type="submit" disabled={assignLoading} style={{ padding: '0.75rem 1.5rem', border: 'none', background: '#3b82f6', color: 'white', borderRadius: '8px', cursor: 'pointer', fontWeight: '600', boxShadow: '0 4px 6px -1px rgba(59, 130, 246, 0.3)' }}>
                                    {assignLoading ? 'Saving...' : 'Confirm Assignment'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Custom Confirm Modal */}
            {confirmModal.isOpen && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '450px', textAlign: 'center', padding: '30px' }}>
                        <div style={{ fontSize: '3rem', marginBottom: '10px' }}>⚠️</div>
                        <h3 style={{ marginTop: 0, color: '#0f172a', marginBottom: '15px', fontSize: '1.4rem' }}>{confirmModal.title}</h3>
                        <p style={{ color: '#475569', fontSize: '1.05rem', marginBottom: '25px', lineHeight: '1.6' }}>
                            {confirmModal.message}
                        </p>

                        <div style={{ display: 'flex', gap: '15px', justifyContent: 'center' }}>
                            <button
                                type="button"
                                onClick={() => setConfirmModal({ ...confirmModal, isOpen: false })}
                                style={{ padding: '0.75rem 1.5rem', border: '1px solid #cbd5e1', background: 'white', color: '#475569', borderRadius: '8px', cursor: 'pointer', fontWeight: '600', flex: 1 }}
                            >
                                Hủy Bỏ
                            </button>
                            <button
                                onClick={confirmModal.action}
                                style={{ padding: '0.75rem 1.5rem', border: 'none', background: '#3b82f6', color: 'white', borderRadius: '8px', cursor: 'pointer', fontWeight: '600', boxShadow: '0 4px 6px -1px rgba(59, 130, 246, 0.3)', flex: 1 }}
                            >
                                Xác Nhận
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminClassManagement;
