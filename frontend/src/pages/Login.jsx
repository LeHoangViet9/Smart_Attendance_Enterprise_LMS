import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axios';
import './Login.css';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(false);

    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setError(null);
        setIsLoading(true);

        try {
            const response = await axiosInstance.post('/auth/login', {
                email,
                password
            });

            const data = response.data.data;

            console.log('[LOGIN DEBUG] Full response:', response.data);
            console.log('[LOGIN DEBUG] data object:', data);
            console.log('[LOGIN DEBUG] role:', data?.role, '| accessToken:', data?.accessToken ? 'EXISTS' : 'MISSING');

            if (!data?.accessToken) {
                setError('Phản hồi từ server không hợp lệ, vui lòng thử lại');
                setIsLoading(false);
                return;
            }

            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);
            localStorage.setItem('user', JSON.stringify({
                email: data.email,
                role: data.role,
                avatarUrl: data.avatarUrl,
                fullName: data.fullName
            }));

            // Luồng điều hướng thông minh
            console.log('[LOGIN DEBUG] Navigating for role:', data.role);
            if (data.role === 'STUDENT') {
                navigate('/student/quizzes');
            } else if (data.role === 'ADMIN') {
                navigate('/admin-home');
            } else if (data.role === 'LECTURER') {
                navigate('/lecturer-home');
            } else {
                console.log('[LOGIN DEBUG] Unknown role, defaulting to /student/quizzes');
                navigate('/student/quizzes');
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Không thể kết nối đến máy chủ. Hãy thử lại!');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="glass-panel">
                <div className="login-header">
                    <div className="login-icon">🏫</div>
                    <h2>Educo LMS</h2>
                    <p>Hệ thống Điểm danh Khuôn mặt AI</p>
                </div>

                {error && <div className="error-alert">{error}</div>}

                <form onSubmit={handleLogin}>
                    <div className="form-group">
                        <label>Email Cá Nhân</label>
                        <input
                            type="email"
                            placeholder="vd: admin@edu.vn"
                            required
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                        />
                    </div>
                    <div className="form-group">
                        <label>Mật Khẩu</label>
                        <input
                            type="password"
                            placeholder="Nhập 123456 để thử nghiệm"
                            required
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                    </div>
                    <button type="submit" className="login-btn" disabled={isLoading}>
                        {isLoading ? 'Đang xác thực...' : 'Đăng Nhập'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default Login;
