import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../api/axios';
import './FaceOnboarding.css';

const FaceOnboarding = () => {
    const navigate = useNavigate();
    const videoRef = useRef(null);
    const canvasRef = useRef(null);
    const [stream, setStream] = useState(null);

    // 0: Init, 2: Scanning Face, 3: Processing, 4: Done
    const [step, setStep] = useState(0);
    const [progress, setProgress] = useState(0);
    const [error, setError] = useState('');

    const startCamera = async () => {
        try {
            setStep(2);
            setError('');
            const mediaStream = await navigator.mediaDevices.getUserMedia({ video: true });
            setStream(mediaStream);
            if (videoRef.current) {
                videoRef.current.srcObject = mediaStream;
            }
        } catch (err) {
            setError('Cannot access camera. Please grant permission to continue.');
            setStep(0);
        }
    };

    const stopCamera = () => {
        if (stream) {
            const tracks = stream.getTracks();
            tracks.forEach(track => track.stop());
        }
    };

    useEffect(() => {
        return () => stopCamera();
    }, [stream]);

    const captureAndUpload = () => {
        if (!videoRef.current || !canvasRef.current) return;
        
        const video = videoRef.current;
        const canvas = canvasRef.current;
        
        // Set canvas to video dimensions
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        
        // Draw current video frame to canvas
        const ctx = canvas.getContext('2d');
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        
        // Convert to blob and upload
        canvas.toBlob((blob) => {
            if (blob) {
                uploadFaceImage(blob);
            } else {
                setError("Failed to capture image");
                setStep(0);
            }
        }, 'image/jpeg', 0.9);
    };

    const uploadFaceImage = async (imageBlob) => {
        setStep(3);
        stopCamera();
        
        try {
            const formData = new FormData();
            formData.append('file', imageBlob, 'face.jpg');
            
            // Progress simulation
            let currentProgress = 0;
            const progressInterval = setInterval(() => {
                currentProgress += Math.floor(Math.random() * 10) + 2;
                if (currentProgress > 95) currentProgress = 95;
                setProgress(currentProgress);
            }, 200);

            await axiosInstance.post('/student/onboarding-face', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            
            clearInterval(progressInterval);
            setProgress(100);
            setTimeout(() => setStep(4), 500);

        } catch (err) {
            console.error('Error saving biometric data:', err);
            setError(err.response?.data?.message || 'Failed to securely save biometric data to server. Please try again.');
            setStep(0);
        }
    };

    const getInstruction = () => {
        switch (step) {
            case 0: return 'The anti-cheating system requires real facial recognition.\nPlease click Start Camera.';
            case 2: return 'Look straight into the camera and click Capture Face.';
            case 3: return 'Uploading and processing on AI Server...';
            case 4: return 'Facial verification completed successfully!';
            default: return '';
        }
    };

    return (
        <div className="onboarding-container">
            <div className="cyber-circle circle-1"></div>
            <div className="cyber-circle circle-2"></div>

            <div className="onboarding-glass">
                <div className="onboarding-header">
                    <h2>KYC Face Onboarding</h2>
                    <p className="subtitle">Backend AI Biometric System</p>
                </div>

                {error && <div className="error-alert" style={{color: '#ef4444', marginBottom: '10px'}}>{error}</div>}

                <div className="scanner-arena">
                    {step === 2 && (
                        <div className="video-container" style={{ position: 'relative' }}>
                            <video 
                                ref={videoRef} 
                                autoPlay 
                                playsInline 
                                muted
                            ></video>
                            <canvas ref={canvasRef} style={{ display: 'none' }}></canvas>
                            <div className="target-frame">
                                <div className="corner top-left"></div>
                                <div className="corner top-right"></div>
                                <div className="corner bottom-left"></div>
                                <div className="corner bottom-right"></div>
                            </div>
                        </div>
                    )}

                    {step === 3 && (
                        <div className="training-container">
                            <div className="ai-brain-icon">🧠</div>
                            <div className="progress-bar-container">
                                <div className="progress-bar" style={{ width: `${progress}%` }}></div>
                            </div>
                            <div className="progress-text">Processing Model... {progress}%</div>
                        </div>
                    )}

                    {step === 4 && (
                        <div className="success-container">
                            <div className="success-checkmark">✅</div>
                            <h3>Biometric profile saved!</h3>
                            <p>You can now participate in exams or get automatically marked as Present.</p>
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
                        <button 
                            className="cyber-btn btn-start" 
                            onClick={startCamera}
                        >
                            Start Camera
                        </button>
                    )}

                    {step === 2 && (
                         <button 
                         className="cyber-btn btn-start" 
                         onClick={captureAndUpload}
                         style={{ backgroundColor: '#10b981', borderColor: '#059669' }}
                     >
                         📸 Capture Face
                     </button>
                    )}

                    {step === 4 && (
                        <button className="cyber-btn btn-finish" onClick={() => navigate('/student/student-home')}>
                            Return to Dashboard 🚀
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default FaceOnboarding;
