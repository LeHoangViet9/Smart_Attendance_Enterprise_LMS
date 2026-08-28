import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import './FaceOnboarding.css';

const FaceOnboarding = () => {
    const navigate = useNavigate();
    const videoRef = useRef(null);
    const [stream, setStream] = useState(null);

    // State quản lý tiến trình chụp ảnh
    const [step, setStep] = useState(0);
    // 0: Init, 1: Front Face, 2: Left Face, 3: Right Face, 4: Training, 5: Done

    const [progress, setProgress] = useState(0);
    const [error, setError] = useState('');

    const startCamera = async () => {
        try {
            const mediaStream = await navigator.mediaDevices.getUserMedia({ video: true });
            setStream(mediaStream);
            if (videoRef.current) {
                videoRef.current.srcObject = mediaStream;
            }
            setStep(1); // Bắt đầu chụp mặt chính diện
        } catch (err) {
            setError('Cannot access camera. Please grant permission to continue.');
        }
    };

    const stopCamera = () => {
        if (stream) {
            const tracks = stream.getTracks();
            tracks.forEach(track => track.stop());
        }
    };

    // Dừng camera khi component unmount
    useEffect(() => {
        return () => stopCamera();
    }, [stream]);

    const handleCapture = () => {
        if (step < 3) {
            // Hiệu ứng flash giả lập
            const flash = document.createElement('div');
            flash.className = 'camera-flash';
            document.body.appendChild(flash);
            setTimeout(() => flash.remove(), 300);

            setStep(prev => prev + 1);
        }

        if (step === 3) {
            setStep(4);
            simulateTraining();
        }
    };

    const simulateTraining = () => {
        stopCamera();
        let currentProgress = 0;
        const interval = setInterval(() => {
            currentProgress += Math.floor(Math.random() * 15) + 5;
            if (currentProgress >= 100) {
                currentProgress = 100;
                clearInterval(interval);
                setTimeout(() => setStep(5), 500);
            }
            setProgress(currentProgress);
        }, 300);
    };

    const getInstruction = () => {
        switch (step) {
            case 0: return 'The anti-cheating system requires facial recognition.\nPlease start the camera.';
            case 1: return 'Look straight into the camera and keep your head still.';
            case 2: return 'Slowly turn your face to the LEFT by 45 degrees.';
            case 3: return 'Slowly turn your face to the RIGHT by 45 degrees.';
            case 4: return 'Configuring your Face ID data...';
            case 5: return 'Facial verification completed!';
            default: return '';
        }
    };

    return (
        <div className="onboarding-container">
            {/* Background elements */}
            <div className="cyber-circle circle-1"></div>
            <div className="cyber-circle circle-2"></div>

            <div className="onboarding-glass">
                <div className="onboarding-header">
                    <h2>KYC Face Onboarding</h2>
                    <p className="subtitle">AI Biometric Security System</p>
                </div>

                {error && <div className="error-alert">{error}</div>}

                <div className="scanner-arena">
                    {step >= 1 && step <= 3 && (
                        <div className="video-container">
                            <video ref={videoRef} autoPlay playsInline muted></video>

                            {/* Khung ngắm Sci-fi */}
                            <div className="target-frame">
                                <div className="corner top-left"></div>
                                <div className="corner top-right"></div>
                                <div className="corner bottom-left"></div>
                                <div className="corner bottom-right"></div>
                            </div>

                            {/* Tia quét Laser */}
                            <div className="laser-scanner"></div>
                        </div>
                    )}

                    {step === 4 && (
                        <div className="training-container">
                            <div className="ai-brain-icon">🧠</div>
                            <div className="progress-bar-container">
                                <div className="progress-bar" style={{ width: `${progress}%` }}></div>
                            </div>
                            <div className="progress-text">Training Model... {progress}%</div>
                        </div>
                    )}

                    {step === 5 && (
                        <div className="success-container">
                            <div className="success-checkmark">✅</div>
                            <h3>Biometric profile saved!</h3>
                            <p>You can now participate in exams.</p>
                        </div>
                    )}

                    {step === 0 && (
                        <div className="init-container">
                            <div className="face-icon-large">👤</div>
                        </div>
                    )}
                </div>

                <div className="instruction-box">
                    <p>{getInstruction()}</p>
                </div>

                <div className="onboarding-actions">
                    {step === 0 && (
                        <button className="cyber-btn btn-start" onClick={startCamera}>
                            Start Camera
                        </button>
                    )}

                    {step >= 1 && step <= 3 && (
                        <button className="cyber-btn btn-capture" onClick={handleCapture}>
                            📸 Capture ({step}/3)
                        </button>
                    )}

                    {step === 5 && (
                        <button className="cyber-btn btn-finish" onClick={() => navigate('/student/quizzes')}>
                            Enter Exam System 🚀
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default FaceOnboarding;
