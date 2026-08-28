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
        fetchData();
    }, [quizId]);

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
                alert("Please select 1 correct answer!");
                return;
            }
        }
        if (newQuestion.questionType === 'SHORT_ANSWER') {
            if (!newQuestion.options[0].content.trim()) {
                alert("Please provide the correct answer key!");
                return;
            }
        }

        setSaving(true);
        try {
            await axiosInstance.post(`/v1/quizzes/${quizId}/questions`, newQuestion);
            setShowModal(false);
            setNewQuestion(generateInitialState('MULTIPLE_CHOICE'));
            fetchData();
        } catch (err) {
            console.error(err);
            alert('Error adding question');
        } finally {
            setSaving(false);
        }
    };

    const handleDeleteQuestion = async (questionId) => {
        if (!window.confirm("Are you sure you want to delete this question?")) return;
        try {
            await axiosInstance.delete(`/v1/quizzes/${quizId}/questions/${questionId}`);
            fetchData();
        } catch (err) {
            console.error(err);
            alert('Error deleting question');
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

    if (loading) {
        return <div className="loader-container"><div className="spinner"></div><p>Loading data...</p></div>;
    }

    return (
        <div className="quiz-container">
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
                        <h1 className="page-title" style={{ margin: 0, fontSize: '1.75rem' }}>Question Bank Management</h1>
                        <h3 style={{ color: '#4b5563', marginTop: '0.5rem', margin: 0, fontWeight: 500 }}>
                            {quiz?.title} (Time limit: {quiz?.timeLimitMinutes} mins)
                        </h3>
                    </div>
                    <button
                        className="btn-primary"
                        style={{ width: 'auto', padding: '0.75rem 1.5rem' }}
                        onClick={() => setShowModal(true)}
                    >
                        ➕ Add New Question
                    </button>
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
                                        style={{ background: 'none', border: 'none', color: '#ef4444', cursor: 'pointer', fontSize: '1.2rem', padding: '4px' }}
                                        onClick={() => handleDeleteQuestion(q.id)}
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
                        <h3 style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>➕ Add New Question</h3>

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
                                    {saving ? 'Saving...' : 'Save Question'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default QuizManagement;
