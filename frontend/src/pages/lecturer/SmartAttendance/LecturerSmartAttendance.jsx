import React, {useEffect, useRef, useState} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import axiosInstance from '../../../api/axios';
import './LecturerSmartAttendance.css';

const LecturerSmartAttendance = () => {
    const { id: classId } = useParams();
    const navigate = useNavigate();
    const videoRef = useRef(null);
    const canvasRef = useRef(null);
    const scanInFlight = useRef(false);
    const [isScanning, setIsScanning] = useState(false);
    const [scanError, setScanError] = useState('');
    const [isSaving, setIsSaving] = useState(false);
    const [saveNotice, setSaveNotice] = useState(null);
    const [filter, setFilter] = useState('all');
    const [searchTerm, setSearchTerm] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 8;

    const [students, setStudents] = useState([]);
    const [className, setClassName] = useState(classId); // Mặc định hiển thị ID trước khi load xong

    // Fetch class details để lấy tên lớp học
    useEffect(() => {
        const fetchClassDetails = async () => {
            try {
                const response = await axiosInstance.get(`/v1/lecturer/classes/${classId}`);
                if (response.data && response.data.data) {
                    setClassName(response.data.data.className);
                }
            } catch (err) {
                console.error("Error fetching class details", err);
            }
        };
        fetchClassDetails();
    }, [classId]);

    // Fetch initial student list
    useEffect(() => {
        const fetchStudents = async () => {
            try {
                // Fetch real students and their faces from backend
                const response = await axiosInstance.get(`/v1/lecturer/classes/${classId}/students/faces`);
                if (response.data && response.data.data) {
                    const studentList = response.data.data;
                    const initialStudents = studentList.map(student => ({
                        id: student.id,
                        name: student.fullName,
                        time: '-',
                        status: 'absent',
                        justMarked: false
                    }));
                    setStudents(initialStudents);
                }
            } catch (err) {
                console.error("Error fetching class face descriptors", err);
            }
        };
        fetchStudents();
    }, [classId]);

    // Start Camera
    useEffect(() => {
        const startCamera = async () => {
            try {
                const stream = await navigator.mediaDevices.getUserMedia({ video: true });
                if (videoRef.current) {
                    videoRef.current.srcObject = stream;
                }
            } catch (err) {
                console.error("Error accessing camera:", err);
            }
        };
        startCamera();
        
        return () => {
            if (videoRef.current && videoRef.current.srcObject) {
                const tracks = videoRef.current.srcObject.getTracks();
                tracks.forEach(track => track.stop());
            }
        };
    }, []);

    // Real-time AI Face Scanning (Backend Driven)
    useEffect(() => {
        if (!isScanning || !videoRef.current || !canvasRef.current) return;

        const video = videoRef.current;
        const canvas = canvasRef.current;

        const scanInterval = setInterval(async () => {
            if (video.paused || video.ended || !video.videoWidth || scanInFlight.current) return;
            scanInFlight.current = true;

            // Match canvas to video size
            canvas.width = video.videoWidth || 640;
            canvas.height = video.videoHeight || 480;

            const ctx = canvas.getContext('2d');
            ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

            canvas.toBlob(async (blob) => {
                if (blob) {
                    try {
                        const formData = new FormData();
                        formData.append('file', blob, 'frame.jpg');

                        const response = await axiosInstance.post(
                            `/v1/attendance/class/${classId}/verify-faces`,
                            formData,
                            { headers: { 'Content-Type': 'multipart/form-data' } }
                        );

                        if (response.data && response.data.success && response.data.data) {
                            setScanError('');
                            const presentIds = response.data.data;
                            presentIds.forEach(id => {
                                markStudentPresentById(id);
                            });
                        }
                    } catch (error) {
                        console.error('Error verifying frame:', error);
                        setScanError(error.response?.data?.message || 'Unable to reach face recognition service. Please try again.');
                    } finally {
                        scanInFlight.current = false;
                    }
                } else {
                    scanInFlight.current = false;
                }
            }, 'image/jpeg', 0.5);

        }, 5000); // Poll every 5 seconds to reduce backend load

        return () => {
            clearInterval(scanInterval);
        };
    }, [isScanning, classId]);

    const markStudentPresentById = (studentId) => {
        setStudents(prev => {
            const studentIdx = prev.findIndex(s => s.id === studentId);
            if (studentIdx === -1) return prev;
            if (prev[studentIdx].status === 'present') return prev; // Already present
            
            const now = new Date();
            const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

            const newStudents = [...prev];
            newStudents[studentIdx] = { ...newStudents[studentIdx], status: 'present', time: timeStr, justMarked: true };
            return newStudents;
        });
        
        // Remove 'justMarked' animation class after 2s
        setTimeout(() => {
            setStudents(prev => {
                const newStudents = [...prev];
                const idx = newStudents.findIndex(s => s.id === studentId);
                if (idx !== -1) {
                    newStudents[idx] = { ...newStudents[idx], justMarked: false };
                }
                return newStudents;
            });
        }, 2000);
    };

    const handleStartScan = () => {
        setIsScanning(!isScanning);
    };

    const handleSubmitAttendance = async () => {
        if (isSaving || students.length === 0) return;
        setIsSaving(true);
        setSaveNotice(null);
        try {
            const presentStudentIds = students.filter(s => s.status === 'present').map(s => s.id);
            const lateStudentIds = students.filter(s => s.status === 'late').map(s => s.id);
            const response = await axiosInstance.post('/v1/attendance/submit-attendance', {
                classId: classId,
                presentStudentIds: presentStudentIds,
                lateStudentIds: lateStudentIds
            });
            if (response.data && response.data.success) {
                setSaveNotice({
                    type: 'success',
                    title: 'Attendance saved',
                    message: `${presentStudentIds.length} present · ${lateStudentIds.length} late · ${students.length - presentStudentIds.length - lateStudentIds.length} absent`
                });
            } else {
                setSaveNotice({type: 'error', title: 'Could not save attendance', message: response.data?.message || 'Please try again.'});
            }
        } catch (error) {
            console.error('Error submitting attendance:', error);
            setSaveNotice({type: 'error', title: 'Could not save attendance', message: error.response?.data?.message || 'Check your connection and try again.'});
        } finally {
            setIsSaving(false);
        }
    };

    const toggleStudentStatus = (studentId) => {
        setStudents(prev => {
            const studentIdx = prev.findIndex(s => s.id === studentId);
            if (studentIdx === -1) return prev;
            
            const currentStatus = prev[studentIdx].status;
            const newStatus = currentStatus === 'absent' ? 'present' : currentStatus === 'present' ? 'late' : 'absent';
            const timeStr = newStatus !== 'absent' ? new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '-';
            
            const newStudents = [...prev];
            newStudents[studentIdx] = { ...newStudents[studentIdx], status: newStatus, time: timeStr, justMarked: newStatus === 'present' };
            
            if (newStatus === 'present') {
                setTimeout(() => {
                    setStudents(current => {
                        const idx = current.findIndex(s => s.id === studentId);
                        if (idx !== -1) {
                            const updated = [...current];
                            updated[idx] = { ...updated[idx], justMarked: false };
                            return updated;
                        }
                        return current;
                    });
                }, 2000);
            }
            
            return newStudents;
        });
    };

    const filteredStudents = students.filter(s => {
        const matchesFilter = filter === 'all' || s.status === filter;
        const matchesSearch = s.name.toLowerCase().includes(searchTerm.toLowerCase());
        return matchesFilter && matchesSearch;
    });

    const totalPages = Math.ceil(filteredStudents.length / itemsPerPage);
    const paginatedStudents = filteredStudents.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage);

    const presentCount = students.filter(s => s.status === 'present').length;

    return (
        <div className="smart-attendance-container">
            <header className="sa-header">
                <div className="sa-title">
                    <button 
                        onClick={() => navigate(-1)} 
                        style={{ background: 'none', border: 'none', color: '#94a3b8', fontSize: '1.2rem', cursor: 'pointer', marginRight: '15px' }}
                        title="Back to Class Management"
                    >
                        ⬅️
                    </button>
                    <h1>Smart Attendance Dashboard</h1>
                    <div className="sa-class-info">Lớp: {className}</div>
                </div>
            </header>

            <div className="sa-main-grid">
                {/* Left Side: Live Feed */}
                <div className="sa-panel">
                    <div className="sa-feed-header">
                        <h2>Live Classroom Feed (Backend AI)</h2>
                        <div className="sa-status-badges">
                            {isScanning ? (
                                <span className="sa-badge live">LIVE</span>
                            ) : (
                                <span className="sa-badge" style={{background: '#334155'}}>PAUSED</span>
                            )}
                            <span className="sa-badge count">{presentCount} / {students.length} Present</span>
                        </div>
                    </div>
                    
                    <div className="sa-video-container" style={{ position: 'relative' }}>
                        <video 
                            ref={videoRef} 
                            autoPlay 
                            playsInline 
                            muted 
                            className="sa-video-feed"
                        ></video>
                        <canvas ref={canvasRef} style={{ display: 'none' }}></canvas>
                        
                        {isScanning && <div className="sa-scan-line"></div>}
                    </div>

                    <div style={{ display: 'flex', gap: '10px', marginTop: '15px', width: '100%' }}>
                        <button 
                            className="sa-start-btn" 
                            onClick={handleStartScan}
                            style={{ flex: 1, margin: 0 }}
                        >
                            {isScanning ? "Stop Scanning" : "Start Smart Attendance"}
                        </button>
                        <button 
                            className="sa-start-btn" 
                            onClick={handleSubmitAttendance}
                            disabled={isSaving || students.length === 0}
                            aria-busy={isSaving}
                            style={{ flex: 1, margin: 0, backgroundColor: '#10b981' }}
                        >
                            {isSaving ? 'Saving…' : 'Save Attendance'}
                        </button>
                    </div>
                    {saveNotice && (
                        <div className={`sa-save-notice ${saveNotice.type}`} role={saveNotice.type === 'error' ? 'alert' : 'status'}>
                            <span className="sa-save-notice-icon" aria-hidden="true">{saveNotice.type === 'success' ? '✓' : '!'}</span>
                            <div className="sa-save-notice-copy">
                                <strong>{saveNotice.title}</strong>
                                <p>{saveNotice.message}</p>
                            </div>
                            <button type="button" className="sa-save-notice-close" aria-label="Dismiss notification" onClick={() => setSaveNotice(null)}>×</button>
                        </div>
                    )}
                    {scanError && <p role="alert" style={{ color: '#f87171' }}>{scanError}</p>}
                    {students.length === 0 && (
                        <p style={{ color: '#f59e0b', fontSize: '0.8rem', marginTop: '10px', textAlign: 'center' }}>
                            ⚠️ Warning: No students found in this class.
                        </p>
                    )}
                </div>

                {/* Right Side: Student List */}
                <div className="sa-panel">
                    <div className="sa-list-header">
                        <h2>Student Attendance</h2>
                        <div className="sa-search-bar">
                            <input 
                                type="text" 
                                placeholder="Search Students..." 
                                value={searchTerm}
                                onChange={e => { setSearchTerm(e.target.value); setCurrentPage(1); }}
                            />
                        </div>
                        <div className="sa-filters">
                            <button className={`sa-filter-btn ${filter === 'all' ? 'active' : ''}`} onClick={() => { setFilter('all'); setCurrentPage(1); }}>All Students</button>
                            <button className={`sa-filter-btn ${filter === 'present' ? 'active' : ''}`} onClick={() => { setFilter('present'); setCurrentPage(1); }}>Present</button>
                            <button className={`sa-filter-btn ${filter === 'late' ? 'active' : ''}`} onClick={() => { setFilter('late'); setCurrentPage(1); }}>Late</button>
                            <button className={`sa-filter-btn ${filter === 'absent' ? 'active' : ''}`} onClick={() => { setFilter('absent'); setCurrentPage(1); }}>Absent</button>
                        </div>
                    </div>

                    <div className="sa-student-list">
                        {paginatedStudents.map(student => (
                            <div 
                                key={student.id} 
                                className={`sa-student-item ${student.justMarked ? 'just-marked' : ''}`}
                                onClick={() => toggleStudentStatus(student.id)}
                                style={{ cursor: 'pointer' }}
                                title="Click to manually mark attendance"
                            >
                                <div className="sa-student-info">
                                    <div className="sa-avatar">
                                        {student.name.charAt(0)}
                                    </div>
                                    <div className="sa-student-details">
                                        <h4>{student.name}</h4>
                                        <span>{student.time}</span>
                                    </div>
                                </div>
                                <span className={`sa-status ${student.status}`}>
                                    {student.status}
                                </span>
                            </div>
                        ))}
                    </div>
                    {totalPages > 1 && (
                        <div className="sa-pagination">
                            <button 
                                disabled={currentPage === 1} 
                                onClick={() => setCurrentPage(prev => prev - 1)}
                            >
                                Prev
                            </button>
                            <span>Page {currentPage} of {totalPages}</span>
                            <button 
                                disabled={currentPage === totalPages} 
                                onClick={() => setCurrentPage(prev => prev + 1)}
                            >
                                Next
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default LecturerSmartAttendance;
