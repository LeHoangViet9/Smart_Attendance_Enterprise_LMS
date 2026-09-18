import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './QuizStyles.css';

const QuizManagement = () => {
    const { quizId } = useParams();
    const navigate = useNavigate();

    const [quiz, setQuiz] = useState(null);
    const [questions, setQuestions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [showModal, setShowModal] = useState(false);
    const [saving, setSaving] = useState(false);
    const [editingQuestionId, setEditingQuestionId] = useState(null);
    
    const [showEditQuizModal, setShowEditQuizModal] = useState(false);
    const [savingQuiz, setSavingQuiz] = useState(false);
    const [editQuizData, setEditQuizData] = useState({});
    
    const [userRole, setUserRole] = useState('STUDENT');
    const [majors, setMajors] = useState([]);
    
    const [deleteConfirmModal, setDeleteConfirmModal] = useState({ show: false, questionId: null });

    const [toast, setToast] = useState({ show: false, message: '', type: 'success' });
    const showToast = (message, type = 'success') => {
        setToast({ show: true, message, type });
        setTimeout(() => setToast({ show: false, message: '', type: 'success' }), 3000);
    };

    const defaultMultipleChoiceOptions = () => [
        { content: '', isCorrect: false }, { content: '', isCorrect: false },
        { content: '', isCorrect: false }, { content: '', isCorrect: false }
    ];

    const generateInitialState = (type = 'MULTIPLE_CHOICE') => {
        let options = [];
        if (type === 'MULTIPLE_CHOICE') {
            options = defaultMultipleChoiceOptions();
        } else if (type === 'TRUE_FALSE') {
            options = [
                { content: 'Đúng', isCorrect: false },
                { content: 'Sai', isCorrect: false }
            ];
        } else if (type === 'SHORT_ANSWER') {
            options = [{ content: '', isCorrect: true }]; // Only 1 required correct answer
        } else if (type === 'ESSAY') {
            options = []; // No predefined options for essay
        }

        return {
            content: '',
            points: 1.0, // Default always 1.0, hidden from UI
            questionType: type,
            options: options
        };
    };

    const [newQuestion, setNewQuestion] = useState(generateInitialState());

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            try {
                const parsed = JSON.parse(storedUser);
                if (parsed.role) setUserRole(parsed.role);
            } catch (e) {
                console.error(e);
            }
        }
        fetchMajors();
        fetchData();
    }, [quizId]);

    const fetchMajors = async () => {
        try {
            const response = await axiosInstance.get('/v1/majors');
            if (response.data && response.data.data) {
                setMajors(response.data.data);
            }
        } catch (err) {
            console.error('Error fetching majors:', err);
        }
    };

    const fetchData = async () => {
        setLoading(true);
        try {
            const [quizRes, qsRes] = await Promise.all([
                axiosInstance.get(`/v1/quizzes/${quizId}`),
                axiosInstance.get(`/v1/quizzes/${quizId}/questions`)
            ]);
            if (quizRes.data?.data) setQuiz(quizRes.data.data);
            if (qsRes.data?.data?.content) setQuestions(qsRes.data.data.content);
        } catch (err) {
            console.error(err);
            setError('Cannot load exam data.');
        } finally {
            setLoading(false);
        }
    };

    const handleQuestionTypeChange = (e) => {
        const type = e.target.value;
        setNewQuestion(generateInitialState(type));
    };

    const handleOptionChange = (idx, value) => {
        const updatedOpts = [...newQuestion.options];
        updatedOpts[idx].content = value;
        setNewQuestion({ ...newQuestion, options: updatedOpts });
    };

    const handleCorrectOptionChange = (idx) => {
        const updatedOpts = newQuestion.options.map((opt, i) => ({
            ...opt,
            isCorrect: i === idx
        }));
        setNewQuestion({ ...newQuestion, options: updatedOpts });
    };

    const handleSaveQuestion = async (e) => {
        e.preventDefault();

        // Validation
        if (newQuestion.questionType === 'MULTIPLE_CHOICE' || newQuestion.questionType === 'TRUE_FALSE') {
            if (!newQuestion.options.some(o => o.isCorrect)) {
                showToast("Please select 1 correct answer!", 'error');
                return;
            }
        }
        if (newQuestion.questionType === 'SHORT_ANSWER') {
            if (!newQuestion.options[0].content.trim()) {
                showToast("Please provide the correct answer key!", 'error');
                return;
            }
        }

        setSaving(true);
        try {
            if (editingQuestionId) {
                await axiosInstance.put(`/v1/quizzes/${quizId}/questions/${editingQuestionId}`, newQuestion);
                showToast('Question updated successfully!', 'success');
            } else {
                await axiosInstance.post(`/v1/quizzes/${quizId}/questions`, newQuestion);
                showToast('Question added successfully!', 'success');
            }
            setShowModal(false);
            setEditingQuestionId(null);
            setNewQuestion(generateInitialState('MULTIPLE_CHOICE'));
            fetchData();
        } catch (err) {
            console.error(err);
            showToast('Error adding question', 'error');
        } finally {
            setSaving(false);
        }
    };

    const handleEditQuestionClick = (q) => {
        setEditingQuestionId(q.id);
        setNewQuestion({
            content: q.content,
            points: q.points || 1.0,
            questionType: q.questionType,
            options: q.options ? q.options.map(o => ({ content: o.content, isCorrect: o.isCorrect })) : []
        });
        setShowModal(true);
    };

    const openEditQuizModal = () => {
        setEditQuizData({
            title: quiz?.title || '',
            description: quiz?.description || '',
            timeLimitMinutes: quiz?.timeLimitMinutes || 30,
            startTime: quiz?.startTime ? quiz.startTime.substring(0, 16) : '',
            endTime: quiz?.endTime ? quiz.endTime.substring(0, 16) : '',
            requiresProctoring: quiz?.requiresProctoring || false,
            majorId: quiz?.majorId || ''
        });
        setShowEditQuizModal(true);
    };

    const handleUpdateQuiz = async (e) => {
        e.preventDefault();
        setSavingQuiz(true);
        try {
            const payload = {
                ...editQuizData,
                timeLimitMinutes: Number(editQuizData.timeLimitMinutes) || 30
            };
            if (editQuizData.startTime) payload.startTime = new Date(editQuizData.startTime).toISOString();
            else delete payload.startTime;
            
            if (editQuizData.endTime) payload.endTime = new Date(editQuizData.endTime).toISOString();
            else delete payload.endTime;
            
            await axiosInstance.put(`/v1/quizzes/${quizId}`, payload);
            setShowEditQuizModal(false);
            showToast('Exam settings updated successfully!', 'success');
            fetchData();
        } catch (err) {
            console.error(err);
            showToast('Error updating exam settings', 'error');
        } finally {
            setSavingQuiz(false);
        }
    };

    const handleDeleteClick = (questionId) => {
        setDeleteConfirmModal({ show: true, questionId });
    };

    const confirmDelete = async () => {
        const questionId = deleteConfirmModal.questionId;
        if (!questionId) return;
        
        setDeleteConfirmModal({ show: false, questionId: null });
        try {
            await axiosInstance.delete(`/v1/quizzes/${quizId}/questions/${questionId}`);
            fetchData();
            showToast('Question deleted successfully!', 'success');
        } catch (err) {
            console.error(err);
            showToast('Error deleting question', 'error');
        }
    };

    const renderQuestionTypeBadge = (type) => {
        switch (type) {
            case 'MULTIPLE_CHOICE': return <span style={{ color: '#0284c7', backgroundColor: '#e0f2fe', padding: '2px 8px', borderRadius: '12px', fontSize: '0.8rem', fontWeight: 600 }}>Trắc nghiệm</span>;
            case 'TRUE_FALSE': return <span style={{ color: '#059669', backgroundColor: '#d1fae5', padding: '2px 8px', borderRadius: '12px', fontSize: '0.8rem', fontWeight: 600 }}>Đúng/Sai</span>;
            case 'SHORT_ANSWER': return <span style={{ color: '#d97706', backgroundColor: '#fef3c7', padding: '2px 8px', borderRadius: '12px', fontSize: '0.8rem', fontWeight: 600 }}>Điền khuyết</span>;
            case 'ESSAY': return <span style={{ color: '#7c3aed', backgroundColor: '#ede9fe', padding: '2px 8px', borderRadius: '12px', fontSize: '0.8rem', fontWeight: 600 }}>Tự luận</span>;
            default: return null;
        }
    };

    const fileInputRef = React.useRef(null);
    const [importing, setImporting] = useState(false);

    const handleImportExcel = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append('file', file);

        setImporting(true);
        try {
            await axiosInstance.post(`/v1/quizzes/${quizId}/questions/import`, formData);
            showToast('Nhập câu hỏi từ Excel thành công!', 'success');
            fetchData();
        } catch (err) {
            console.error(err);
            const backendMsg = err.response?.data?.message || err.message;
            showToast('Lỗi khi nhập câu hỏi từ Excel: ' + backendMsg, 'error');
        } finally {
            setImporting(false);
            if (fileInputRef.current) {
                fileInputRef.current.value = '';
            }
        }
    };

    if (loading) {
        return <div className="loader-container"><div className="spinner"></div><p>Loading data...</p></div>;
    }

    return (
        <div className="quiz-container">
            {/* Custom Toast Notification */}
            {toast.show && (
                <div style={{
                    position: 'fixed',
                    top: '24px',
                    right: '24px',
                    padding: '16px 24px',
                    borderRadius: '8px',
                    backgroundColor: toast.type === 'success' ? '#10b981' : '#ef4444',
                    color: 'white',
                    fontWeight: 500,
                    boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                    zIndex: 10000,
                    transition: 'all 0.3s ease-in-out',
                    animation: 'slideInRight 0.3s ease-out'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <span>{toast.type === 'success' ? '✅' : '⚠️'}</span>
                        {toast.message}
                    </div>
                </div>
            )}

            <div style={{ marginBottom: '25px', display: 'flex', flexDirection: 'column', gap: '15px' }}>
                <div>
                    <button
                        className="btn-card-action secondary"
                        style={{ width: 'fit-content', padding: '0.6rem 1.2rem', display: 'flex', alignItems: 'center', gap: '8px' }}
                        onClick={() => navigate('/student/quizzes')}
                    >
                        ⬅ Back to Quiz List
                    </button>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'white', padding: '1.5rem', borderRadius: '12px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
                    <div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <h1 className="page-title" style={{ margin: 0, fontSize: '1.75rem' }}>Question Bank Management</h1>
                            <button 
                                onClick={openEditQuizModal}
                                style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.25rem', color: '#64748b', transition: 'color 0.2s' }}
                                title="Edit Exam Settings"
                            >
                                ⚙️
                            </button>
                        </div>
                        <h3 style={{ color: '#4b5563', marginTop: '0.5rem', margin: 0, fontWeight: 500 }}>
                            {quiz?.title} (Time limit: {quiz?.timeLimitMinutes} mins)
                        </h3>
                    </div>
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <input 
                            type="file" 
                            accept=".xlsx, .xls" 
                            ref={fileInputRef} 
                            style={{ display: 'none' }} 
                            onChange={handleImportExcel} 
                        />
                        <button
                            className="btn-card-action primary"
                            style={{ width: 'auto', padding: '0.75rem 1.5rem', backgroundColor: '#10b981', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px' }}
                            onClick={() => fileInputRef.current?.click()}
                            disabled={importing}
                        >
                            {importing ? '⏳ Đang nhập...' : '📄 Nhập từ Excel'}
                        </button>
                        <button
                            className="btn-primary"
                            style={{ width: 'auto', padding: '0.75rem 1.5rem' }}
                            onClick={() => {
                                setEditingQuestionId(null);
                                setNewQuestion(generateInitialState('MULTIPLE_CHOICE'));
                                setShowModal(true);
                            }}
                        >
                            ➕ Add New Question
                        </button>
                    </div>
                </div>
            </div>

            {error && <div className="error-message">⚠️ {error}</div>}

            <div className="quiz-grid">
                {questions.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '3rem', color: '#6b7280', gridColumn: '1 / -1' }}>
                        <h3>No questions yet.</h3>
                        <p>Click "Add New Question" to start designing the exam.</p>
                    </div>
                ) : (
                    questions.map((q, qIndex) => (
                        <div key={q.id} className="quiz-card" style={{ gridColumn: '1 / -1', maxWidth: '100%' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                <div>
                                    <h3 className="quiz-title" style={{ color: '#1e293b', marginBottom: '0.4rem' }}>Question {qIndex + 1}: {q.content}</h3>
                                    {renderQuestionTypeBadge(q.questionType)}
                                </div>
                                <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                                    <button
                                        style={{ background: 'none', border: 'none', color: '#3b82f6', cursor: 'pointer', fontSize: '1.2rem', padding: '4px' }}
                                        onClick={() => handleEditQuestionClick(q)}
                                        title="Edit question"
                                    >
                                        ✏️
                                    </button>
                                    <button
                                        style={{ background: 'none', border: 'none', color: '#ef4444', cursor: 'pointer', fontSize: '1.2rem', padding: '4px' }}
                                        onClick={() => handleDeleteClick(q.id)}
                                        title="Delete question"
                                    >
                                        🗑️
                                    </button>
                                </div>
                            </div>

                            <div style={{ marginTop: '1rem', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem' }}>
                                {q.questionType === 'ESSAY' && (
                                    <div style={{ padding: '0.75rem', backgroundColor: '#f8fafc', border: '1px dashed #cbd5e1', borderRadius: '6px', color: '#64748b', fontStyle: 'italic' }}>
                                        (Essay response area provided to student natively.)
                                    </div>
                                )}

                                {q.options && q.options.map((opt, oIdx) => (
                                    <div key={opt.id} style={{
                                        padding: '0.75rem',
                                        borderRadius: '6px',
                                        border: opt.isCorrect ? '2px solid #10b981' : '1px solid #e2e8f0',
                                        backgroundColor: opt.isCorrect ? '#f0fdf4' : '#f8fafc',
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '0.5rem'
                                    }}>
                                        {(q.questionType === 'MULTIPLE_CHOICE' || q.questionType === 'TRUE_FALSE') && (
                                            <div style={{
                                                width: '24px', height: '24px', borderRadius: '50%',
                                                backgroundColor: opt.isCorrect ? '#10b981' : '#cbd5e1',
                                                color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center',
                                                fontWeight: 'bold', fontSize: '12px', flexShrink: 0
                                            }}>
                                                {String.fromCharCode(65 + oIdx)}
                                            </div>
                                        )}
                                        {q.questionType === 'SHORT_ANSWER' && (
                                            <div style={{ color: '#10b981', fontWeight: 'bold' }}>✓</div>
                                        )}
                                        <span style={{ fontWeight: opt.isCorrect ? 600 : 400, color: opt.isCorrect ? '#065f46' : '#334155', wordBreak: 'break-all' }}>
                                            {opt.content}
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))
                )}
            </div>

            {/* Modal */}
            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 9999 }}>
                    <div className="modal-glass" style={{ maxWidth: '600px', width: '100%', backgroundColor: 'white', padding: '2rem', borderRadius: '12px', maxHeight: '90vh', overflowY: 'auto' }} onClick={(e) => e.stopPropagation()}>
                        <h3 style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>
                            {editingQuestionId ? '✏️ Edit Question' : '➕ Add New Question'}
                        </h3>

                        <form onSubmit={handleSaveQuestion}>

                            <div className="modal-form-group" style={{ marginBottom: '1rem' }}>
                                <label style={{ fontWeight: 600, display: 'block', marginBottom: '0.5rem' }}>Question Format *</label>
                                <select
                                    className="modal-form-control"
                                    value={newQuestion.questionType}
                                    onChange={handleQuestionTypeChange}
                                    style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '6px', background: 'white' }}
                                >
                                    <option value="MULTIPLE_CHOICE">Trắc nghiệm nhiều lựa chọn (Multiple Choice)</option>
                                    <option value="TRUE_FALSE">Đúng / Sai (True / False)</option>
                                    <option value="SHORT_ANSWER">Điền khuyết (Short Answer)</option>
                                    <option value="ESSAY">Tự luận (Essay)</option>
                                </select>
                            </div>

                            <div className="modal-form-group">
                                <label style={{ fontWeight: 600, display: 'block', marginBottom: '0.5rem' }}>Question content *</label>
                                <textarea
                                    required
                                    rows="3"
                                    style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '6px' }}
                                    value={newQuestion.content}
                                    onChange={(e) => setNewQuestion({ ...newQuestion, content: e.target.value })}
                                    placeholder="Enter your question here..."
                                />
                            </div>

                            {newQuestion.questionType !== 'ESSAY' && (
                                <div style={{ marginTop: '1.5rem', background: '#f8fafc', padding: '1rem', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                                    <label style={{ fontWeight: 600, display: 'block', marginBottom: '1rem', color: '#1e293b' }}>
                                        {newQuestion.questionType === 'SHORT_ANSWER' ? 'Correct Answer Key *' : 'Options (Select correct option) *'}
                                    </label>

                                    {newQuestion.options.map((opt, idx) => (
                                        <div key={idx} style={{ display: 'flex', gap: '1rem', alignItems: 'center', marginBottom: '0.75rem' }}>

                                            {newQuestion.questionType === 'MULTIPLE_CHOICE' && (
                                                <>
                                                    <input
                                                        type="radio"
                                                        name="correctOption"
                                                        checked={opt.isCorrect}
                                                        onChange={() => handleCorrectOptionChange(idx)}
                                                        style={{ width: '20px', height: '20px', cursor: 'pointer' }}
                                                    />
                                                    <span style={{ fontWeight: 600, color: '#475569', minWidth: '15px' }}>{String.fromCharCode(65 + idx)}</span>
                                                    <input
                                                        type="text"
                                                        required
                                                        style={{ flex: 1, padding: '0.6rem', border: opt.isCorrect ? '1px solid #10b981' : '1px solid #cbd5e1', borderRadius: '6px', background: 'white' }}
                                                        placeholder={`Option content ${String.fromCharCode(65 + idx)}`}
                                                        value={opt.content}
                                                        onChange={(e) => handleOptionChange(idx, e.target.value)}
                                                    />
                                                </>
                                            )}

                                            {newQuestion.questionType === 'TRUE_FALSE' && (
                                                <>
                                                    <input
                                                        type="radio"
                                                        name="correctOption"
                                                        checked={opt.isCorrect}
                                                        onChange={() => handleCorrectOptionChange(idx)}
                                                        style={{ width: '20px', height: '20px', cursor: 'pointer' }}
                                                    />
                                                    <div style={{ flex: 1, padding: '0.6rem', border: opt.isCorrect ? '2px solid #10b981' : '1px solid #cbd5e1', borderRadius: '6px', background: opt.isCorrect ? '#f0fdf4' : 'white', fontWeight: 600, color: '#334155' }}>
                                                        {opt.content}
                                                    </div>
                                                </>
                                            )}

                                            {newQuestion.questionType === 'SHORT_ANSWER' && (
                                                <>
                                                    <div style={{ color: '#10b981', fontWeight: 'bold' }}>✓</div>
                                                    <input
                                                        type="text"
                                                        required
                                                        style={{ flex: 1, padding: '0.6rem', border: '2px solid #10b981', borderRadius: '6px', background: 'white' }}
                                                        placeholder={`Dung lượng bộ phận...`}
                                                        value={opt.content}
                                                        onChange={(e) => handleOptionChange(idx, e.target.value)}
                                                    />
                                                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Exact match required</div>
                                                </>
                                            )}
                                        </div>
                                    ))}
                                </div>
                            )}

                            <div className="modal-actions" style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '2rem' }}>
                                <button type="button" onClick={() => setShowModal(false)} disabled={saving} style={{ padding: '0.75rem 1.5rem', borderRadius: '6px', border: '1px solid #cbd5e1', cursor: 'pointer' }}>
                                    Cancel
                                </button>
                                <button type="submit" disabled={saving} style={{ padding: '0.75rem 1.5rem', borderRadius: '6px', backgroundColor: '#3b82f6', color: 'white', border: 'none', cursor: 'pointer' }}>
                                    {saving ? 'Saving...' : (editingQuestionId ? 'Update Question' : 'Save Question')}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Edit Quiz Details Modal */}
            {showEditQuizModal && (
                <div className="modal-overlay" onClick={() => setShowEditQuizModal(false)} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 9999 }}>
                    <div className="modal-glass" style={{ maxWidth: '540px', width: '100%', backgroundColor: 'white', padding: '2rem', borderRadius: '12px', boxShadow: '0 10px 25px rgba(0,0,0,0.1)', maxHeight: '90vh', overflowY: 'auto' }} onClick={(e) => e.stopPropagation()}>
                        <h3 style={{ fontSize: '1.5rem', color: '#1e293b', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>⚙️ Edit Exam Settings</h3>
                        <p style={{ color: '#64748b', marginBottom: '1.5rem', fontSize: '0.9rem' }}>Update information for the online test</p>

                        <form onSubmit={handleUpdateQuiz}>
                            <div className="modal-form-group" style={{ marginBottom: '1rem' }}>
                                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600, color: '#374151' }}>Quiz Title *</label>
                                <input
                                    type="text"
                                    required
                                    style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '6px' }}
                                    value={editQuizData.title}
                                    onChange={(e) => setEditQuizData({ ...editQuizData, title: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-group" style={{ marginBottom: '1rem' }}>
                                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600, color: '#374151' }}>Description</label>
                                <textarea
                                    rows="3"
                                    style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '6px' }}
                                    value={editQuizData.description}
                                    onChange={(e) => setEditQuizData({ ...editQuizData, description: e.target.value })}
                                />
                            </div>

                            <div className="modal-form-row" style={{ display: 'flex', gap: '1rem', marginBottom: '1rem' }}>
                                <div className="modal-form-group" style={{ flex: 1 }}>
                                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600, color: '#374151' }}>Time limit (Minutes) *</label>
                                    <input
                                        type="number"
                                        min="1"
                                        required
                                        style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '6px' }}
                                        value={editQuizData.timeLimitMinutes}
                                        onChange={(e) => setEditQuizData({ ...editQuizData, timeLimitMinutes: e.target.value })}
                                    />
                                </div>
                                {userRole === 'ADMIN' && (
                                    <div className="modal-form-group" style={{ flex: 1 }}>
                                        <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600, color: '#374151' }}>Major</label>
                                        <select
                                            style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '6px', backgroundColor: '#fff' }}
                                            value={editQuizData.majorId || ''}
                                            onChange={(e) => setEditQuizData({ ...editQuizData, majorId: e.target.value })}
                                        >
                                            <option value="">-- Select Major --</option>
                                            {majors.map(m => (
                                                <option key={m.id} value={m.id}>{m.name}</option>
                                            ))}
                                        </select>
                                    </div>
                                )}
                            </div>

                            <div className="modal-form-row" style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem' }}>
                                <div className="modal-form-group" style={{ flex: 1 }}>
                                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600, color: '#374151' }}>Start time (Optional)</label>
                                    <input
                                        type="datetime-local"
                                        style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '6px' }}
                                        value={editQuizData.startTime}
                                        onChange={(e) => setEditQuizData({ ...editQuizData, startTime: e.target.value })}
                                    />
                                </div>
                                <div className="modal-form-group" style={{ flex: 1 }}>
                                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600, color: '#374151' }}>End time (Optional)</label>
                                    <input
                                        type="datetime-local"
                                        style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '6px' }}
                                        value={editQuizData.endTime}
                                        onChange={(e) => setEditQuizData({ ...editQuizData, endTime: e.target.value })}
                                    />
                                </div>
                            </div>

                            <div className="modal-form-group" style={{ marginBottom: '1.5rem' }}>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer' }}>
                                    <input
                                        type="checkbox"
                                        checked={editQuizData.requiresProctoring}
                                        onChange={(e) => setEditQuizData({ ...editQuizData, requiresProctoring: e.target.checked })}
                                        style={{ width: '20px', height: '20px', cursor: 'pointer' }}
                                    />
                                    <span style={{ fontWeight: 600, color: '#ef4444' }}>📸 Enable AI Face Proctoring (Anti-cheat)</span>
                                </label>
                                <p style={{ margin: '5px 0 0 30px', fontSize: '0.85rem', color: '#64748b' }}>Students must verify their identity via webcam before taking this exam.</p>
                            </div>

                            <div className="modal-actions" style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem' }}>
                                <button
                                    type="button"
                                    style={{ padding: '0.75rem 1.5rem', borderRadius: '6px', backgroundColor: '#f1f5f9', color: '#475569', fontWeight: 600, border: 'none', cursor: 'pointer' }}
                                    onClick={() => setShowEditQuizModal(false)}
                                    disabled={savingQuiz}
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    style={{ padding: '0.75rem 1.5rem', borderRadius: '6px', backgroundColor: '#3b82f6', color: '#ffffff', fontWeight: 600, border: 'none', cursor: 'pointer' }}
                                    disabled={savingQuiz}
                                >
                                    {savingQuiz ? 'Saving...' : 'Save Settings'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Custom Delete Confirmation Modal */}
            {deleteConfirmModal.show && (
                <div className="modal-overlay" onClick={() => setDeleteConfirmModal({ show: false, questionId: null })} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 10000 }}>
                    <div className="modal-glass" style={{ maxWidth: '400px', width: '100%', backgroundColor: 'white', padding: '1.5rem', borderRadius: '12px', textAlign: 'center' }} onClick={(e) => e.stopPropagation()}>
                        <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🗑️</div>
                        <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem', color: '#1e293b' }}>Confirm Deletion</h3>
                        <p style={{ color: '#64748b', marginBottom: '1.5rem' }}>Are you sure you want to delete this question? This action cannot be undone.</p>
                        
                        <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem' }}>
                            <button 
                                onClick={() => setDeleteConfirmModal({ show: false, questionId: null })} 
                                style={{ padding: '0.6rem 1.2rem', borderRadius: '6px', border: '1px solid #cbd5e1', backgroundColor: 'white', cursor: 'pointer', fontWeight: 500 }}
                            >
                                Cancel
                            </button>
                            <button 
                                onClick={confirmDelete} 
                                style={{ padding: '0.6rem 1.2rem', borderRadius: '6px', border: 'none', backgroundColor: '#ef4444', color: 'white', cursor: 'pointer', fontWeight: 500 }}
                            >
                                Delete
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default QuizManagement;
