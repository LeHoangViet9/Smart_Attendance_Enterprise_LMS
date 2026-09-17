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
                // For student, we get all grades, but if we are in a specific class context, 
                // we should filter it. If classId is present, we can filter it.
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

    const handleSaveScore = async (gradebookId, grade) => {
        try {
            await axiosInstance.put(`/v1/gradebooks/${gradebookId}`, {
                attendanceScore: grade.attendanceScore,
                assignmentScore: grade.assignmentScore,
                midtermScore: grade.midtermScore,
                finalScore: grade.finalScore,
                teacherComment: grade.teacherComment
            });
            showStatus("Lưu điểm thành công!", "success");
            setEditingRow(null);
            fetchGrades();
        } catch (error) {
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
                </div>
            </div>

            <div className="gradebook-table-wrapper">
                <table className="gradebook-table">
                    <thead>
                        <tr>
                            <th>Tên Học Sinh</th>
                            <th>Email</th>
                            <th>Chuyên Cần (x1)</th>
                            <th>Bài Tập (x1)</th>
                            <th>Giữa Kỳ (x2)</th>
                            <th>Cuối Kỳ (x3)</th>
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
                                    <td>{grade.studentName}</td>
                                    <td>{grade.studentEmail}</td>

                                    <td>
                                        {isEditing && userRole === 'LECTURER' ? (
                                            <input type="number" defaultValue={grade.attendanceScore} 
                                                onChange={(e) => grade.attendanceScore = e.target.value} 
                                                className="gradebook-input" min="0" max="10" />
                                        ) : (grade.attendanceScore || '-')}
                                    </td>
                                    
                                    <td>
                                        {isEditing && userRole === 'LECTURER' ? (
                                            <input type="number" defaultValue={grade.assignmentScore} 
                                                onChange={(e) => grade.assignmentScore = e.target.value} 
                                                className="gradebook-input" min="0" max="10" />
                                        ) : (grade.assignmentScore || '-')}
                                    </td>

                                    <td>
                                        {isEditing && userRole === 'LECTURER' ? (
                                            <input type="number" defaultValue={grade.midtermScore} 
                                                onChange={(e) => grade.midtermScore = e.target.value} 
                                                className="gradebook-input" min="0" max="10" />
                                        ) : (grade.midtermScore || '-')}
                                    </td>

                                    <td>
                                        {isEditing && userRole === 'LECTURER' ? (
                                            <input type="number" defaultValue={grade.finalScore} 
                                                onChange={(e) => grade.finalScore = e.target.value} 
                                                className="gradebook-input" min="0" max="10" />
                                        ) : (grade.finalScore || '-')}
                                    </td>

                                    <td className="gradebook-gpa">{grade.averageScore || '-'}</td>

                                    <td>
                                        {isEditing && userRole === 'LECTURER' ? (
                                            <input type="text" defaultValue={grade.teacherComment} 
                                                onChange={(e) => grade.teacherComment = e.target.value} 
                                                className="gradebook-input-text"/>
                                        ) : (grade.teacherComment || '-')}
                                    </td>

                                    {userRole === 'LECTURER' && (
                                        <td>
                                            {isEditing ? (
                                                <>
                                                    <button 
                                                        onClick={() => handleSaveScore(grade.id, grade)}
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
                                                    onClick={() => setEditingRow(grade.id)}
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
