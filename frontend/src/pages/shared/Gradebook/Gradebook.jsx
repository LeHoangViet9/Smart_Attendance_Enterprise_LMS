import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './Gradebook.css';

const Gradebook = () => {
    const { classId } = useParams();
    const navigate = useNavigate();
    const [grades, setGrades] = useState([]);
    const [loading, setLoading] = useState(true);
    const [editingRow, setEditingRow] = useState(null);
    const [editValues, setEditValues] = useState({});
    const [statusMessage, setStatusMessage] = useState({ text: '', type: '' });

    const userString = localStorage.getItem('user');
    const userRole = userString ? JSON.parse(userString).role : 'STUDENT';

    useEffect(() => {
        fetchGrades();
    }, [classId]);

    const fetchGrades = async () => {
        setLoading(true);
        try {
            let res;
            if (userRole === 'STUDENT') {
                res = await axiosInstance.get(`/v1/gradebooks/my-grades`);
                if (classId) {
                     setGrades(res.data.data.filter(g => g.classId === classId));
                } else {
                     setGrades(res.data.data);
                }
            } else {
                res = await axiosInstance.get(`/v1/gradebooks/classes/${classId}`);
                setGrades(res.data.data);
            }
        } catch (error) {
            console.error("Lỗi khi tải bảng điểm:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleExportExcel = async () => {
        try {
            const res = await axiosInstance.get(`/v1/gradebooks/classes/${classId}/export`, {
                responseType: 'blob' 
            });
            const url = window.URL.createObjectURL(new Blob([res.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `BangDiem_Lop_${classId}.xlsx`);
            document.body.appendChild(link);
            link.click();
        } catch (error) {
            showStatus("Lỗi xuất file Excel!", "error");
        }
    };

    const showStatus = (text, type) => {
        setStatusMessage({ text, type });
        setTimeout(() => setStatusMessage({ text: '', type: '' }), 3000);
    };

    const startEditing = (grade) => {
        setEditingRow(grade.id);
        setEditValues({
            attendanceScore: grade.attendanceScore || '',
            assignmentScore: grade.assignmentScore || '',
            midtermScore: grade.midtermScore || '',
            finalScore: grade.finalScore || '',
            teacherComment: grade.teacherComment || ''
        });
    };

    const handleSaveScore = async (gradebookId) => {
        try {
            const payload = {
                attendanceScore: editValues.attendanceScore !== '' ? parseFloat(editValues.attendanceScore) : null,
                assignmentScore: editValues.assignmentScore !== '' ? parseFloat(editValues.assignmentScore) : null,
                midtermScore: editValues.midtermScore !== '' ? parseFloat(editValues.midtermScore) : null,
                finalScore: editValues.finalScore !== '' ? parseFloat(editValues.finalScore) : null,
                teacherComment: editValues.teacherComment
            };
            console.log("Sending payload:", payload);
            await axiosInstance.put(`/v1/gradebooks/${gradebookId}`, payload);
            showStatus("Lưu điểm thành công!", "success");
            setEditingRow(null);
            fetchGrades();
        } catch (error) {
            console.error(error);
            showStatus("Lỗi khi lưu điểm!", "error");
        }
    };

    if (loading) return <div style={{ padding: '2rem' }}>Đang tải bảng điểm...</div>;

    return (
        <div className="gradebook-container">
            {statusMessage.text && (
                <div style={{
                    padding: '1rem',
                    marginBottom: '1rem',
                    borderRadius: '0.375rem',
                    color: 'white',
                    fontWeight: 'bold',
                    backgroundColor: statusMessage.type === 'success' ? '#10b981' : '#ef4444',
                    transition: 'all 0.3s'
                }}>
                    {statusMessage.type === 'success' ? '✅' : '❌'} {statusMessage.text}
                </div>
            )}

            <div className="gradebook-header">
                <div>
                    <h2 className="gradebook-title">📚 Bảng Điểm</h2>
                    <p style={{ color: '#64748b' }}>Quản lý điểm số và tiến độ học tập</p>
                </div>
                
                <div style={{ display: 'flex', gap: '1rem' }}>
                    <button 
                        onClick={() => navigate(-1)}
                        style={{ padding: '0.5rem 1rem', borderRadius: '0.375rem', border: '1px solid #cbd5e1', cursor: 'pointer' }}
                    >
                        ⬅ Quay lại
                    </button>
                    {userRole === 'ADMIN' && classId && (
                        <button 
                            onClick={handleExportExcel}
                            className="gradebook-btn-export"
                        >
                            ⬇ Xuất Excel
                        </button>
                    )}
                    {userRole === 'LECTURER' && classId && (
                        <button 
                            onClick={async () => {
                                try {
                                    setLoading(true);
                                    const res = await axiosInstance.post(`/v1/gradebooks/classes/${classId}/sync`);
                                    setGrades(res.data.data);
                                    showStatus("Đồng bộ điểm thành công!", "success");
                                } catch(e) {
                                    showStatus("Lỗi đồng bộ điểm!", "error");
                                } finally { setLoading(false); }
                            }}
                            className="gradebook-btn-save"
                            style={{ background: '#3b82f6', color: 'white', border: 'none', padding: '0.5rem 1rem', borderRadius: '0.375rem', cursor: 'pointer' }}
                        >
                            🔄 Đồng bộ điểm
                        </button>
                    )}
                </div>
            </div>

            <div className="gradebook-table-wrapper">
                <table className="gradebook-table">
                    <thead>
                        <tr>
                            {userRole === 'STUDENT' ? (
                                <>
                                    <th>Môn Học</th>
                                    <th>Lớp Học</th>
                                </>
                            ) : (
                                <>
                                    <th>Tên Học Sinh</th>
                                    <th>Email</th>
                                </>
                            )}
                            <th>Chuyên Cần (x1)</th>
                            <th>Bài Tập (x1)</th>
                            <th>Giữa Kỳ (x3)</th>
                            <th>Cuối Kỳ (x5)</th>
                            <th style={{ color: '#2563eb' }}>GPA</th>
                            <th>Nhận Xét</th>
                            {userRole === 'LECTURER' && <th>Thao tác</th>}
                        </tr>
                    </thead>
                    <tbody>
                        {grades.length === 0 ? (
                            <tr>
                                <td colSpan={userRole === 'LECTURER' ? 9 : 8} style={{ textAlign: 'center', padding: '2rem' }}>
                                    Chưa có dữ liệu điểm cho lớp học này.
                                </td>
                            </tr>
                        ) : grades.map((grade) => {
                            const isEditing = editingRow === grade.id;

                            return (
                                <tr key={grade.id}>
                                    {userRole === 'STUDENT' ? (
                                        <>
                                            <td>{grade.courseName || '-'}</td>
                                            <td>{grade.className || '-'}</td>
                                        </>
                                    ) : (
                                        <>
                                            <td>{grade.studentName}</td>
                                            <td>{grade.studentEmail}</td>
                                        </>
                                    )}

                                    <td>
                                        {isEditing && userRole === 'LECTURER' ? (
                                            <input type="number" value={editValues.attendanceScore} 
                                                onChange={(e) => setEditValues({...editValues, attendanceScore: e.target.value})} 
                                                className="gradebook-input" min="0" max="10" />
                                        ) : (grade.attendanceScore ?? '-')}
                                    </td>
                                    
                                    <td>
                                        {isEditing && userRole === 'LECTURER' ? (
                                            <input type="number" value={editValues.assignmentScore} 
                                                onChange={(e) => setEditValues({...editValues, assignmentScore: e.target.value})} 
                                                className="gradebook-input" min="0" max="10" />
                                        ) : (grade.assignmentScore ?? '-')}
                                    </td>

                                    <td>
                                        {isEditing && userRole === 'LECTURER' ? (
                                            <input type="number" value={editValues.midtermScore} 
                                                onChange={(e) => setEditValues({...editValues, midtermScore: e.target.value})} 
                                                className="gradebook-input" min="0" max="10" />
                                        ) : (grade.midtermScore ?? '-')}
                                    </td>

                                    <td>
                                        {isEditing && userRole === 'LECTURER' ? (
                                            <input type="number" value={editValues.finalScore} 
                                                onChange={(e) => setEditValues({...editValues, finalScore: e.target.value})} 
                                                className="gradebook-input" min="0" max="10" />
                                        ) : (grade.finalScore ?? '-')}
                                    </td>

                                    <td className="gradebook-gpa">{grade.averageScore ?? '-'}</td>

                                    <td>
                                        {isEditing && userRole === 'LECTURER' ? (
                                            <input type="text" value={editValues.teacherComment} 
                                                onChange={(e) => setEditValues({...editValues, teacherComment: e.target.value})} 
                                                className="gradebook-input-text"/>
                                        ) : (grade.teacherComment ?? '-')}
                                    </td>

                                    {userRole === 'LECTURER' && (
                                        <td>
                                            {isEditing ? (
                                                <>
                                                    <button 
                                                        onClick={() => handleSaveScore(grade.id)}
                                                        className="gradebook-btn-save"
                                                    >
                                                        Lưu
                                                    </button>
                                                    <button 
                                                        onClick={() => setEditingRow(null)}
                                                        style={{ background: '#cbd5e1', color: 'black', padding: '0.25rem 0.5rem', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                                                    >
                                                        Hủy
                                                    </button>
                                                </>
                                            ) : (
                                                <button 
                                                    onClick={() => startEditing(grade)}
                                                    className="gradebook-btn-edit"
                                                >
                                                    Sửa
                                                </button>
                                            )}
                                        </td>
                                    )}
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default Gradebook;
