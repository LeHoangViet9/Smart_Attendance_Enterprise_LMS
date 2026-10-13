import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import axiosInstance from '../api/axios';
import './Login.css';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [isResetting, setIsResetting] = useState(false);
    const [resetToken, setResetToken] = useState('');
    const [resetPassword, setResetPassword] = useState('');

    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);
        setIsLoading(true);

        try {
            const response = await axiosInstance.post('/auth/login', {
                email,
                password
            });

            const data = response.data.data;

            if (!data?.accessToken) {
                setError('Invalid server response, please try again');
                setIsLoading(false);
                return;
            }

            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);
            localStorage.setItem('user', JSON.stringify({
                userId: data.userId || data.id,
                email: data.email,
                role: data.role,
                avatarUrl: data.avatarUrl,
                fullName: data.fullName
            }));

            // Luồng điều hướng thông minh
            if (data.role === 'STUDENT') {
                navigate('/student/student-home');
            } else if (data.role === 'ADMIN') {
                navigate('/admin/dashboard');
            } else if (data.role === 'LECTURER') {
                navigate('/lecturer/dashboard');
            } else {
                navigate('/student/quizzes');
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Cannot connect to server. Please try again!');
        } finally {
            setIsLoading(false);
        }
    };

    const handleForgotPassword = async () => {
        if (!email) {
            setError('Enter your email first, then reset password.');
            return;
        }
        setError(null);
        setSuccess(null);
        setIsResetting(true);
        try {
            const response = await axiosInstance.post('/auth/forgot-password', { email });
            setResetToken(response.data?.data || '');
            setSuccess(response.data?.message || 'Password reset token generated.');
        } catch (err) {
            setError(err.response?.data?.message || 'Could not reset password.');
        } finally {
            setIsResetting(false);
        }
    };

    const handleResetPassword = async () => {
        if (!resetToken || !resetPassword) {
            setError('Enter reset token and new password.');
            return;
        }
        setError(null);
        setSuccess(null);
        setIsResetting(true);
        try {
            const response = await axiosInstance.post('/auth/reset-password', {
                token: resetToken,
                newPassword: resetPassword
            });
            setSuccess(response.data?.message || 'Password reset successfully.');
            setResetToken('');
            setResetPassword('');
        } catch (err) {
            setError(err.response?.data?.message || 'Could not reset password.');
        } finally {
            setIsResetting(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-glass-panel">
                <div className="login-header">
                    <div className="login-icon">🏫</div>
                    <h2>LMS</h2>
                    <p>AI Facial Recognition System</p>
                </div>

                {error && <div className="error-alert">{error}</div>}
                {success && <div className="success-alert">{success}</div>}

                <form onSubmit={handleLogin}>
                    <div className="form-group">
                        <label>Personal Email</label>
                        <input
                            type="email"
                            placeholder="e.g. admin@edu.vn"
                            required
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                        />
                    </div>
                    <div className="form-group">
                        <label>Password</label>
                        <input
                            type="password"
                            placeholder="Enter your password"
                            required
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                    </div>
                    <button type="submit" className="login-btn" disabled={isLoading}>
                        {isLoading ? 'Authenticating...' : 'Login'}
                    </button>
                    <button type="button" className="forgot-btn" onClick={handleForgotPassword} disabled={isResetting}>
                        {isResetting ? 'Processing...' : 'Forgot password?'}
                    </button>
                </form>

                {resetToken && (
                    <div className="reset-panel">
                        <label>Reset token</label>
                        <input value={resetToken} onChange={(e) => setResetToken(e.target.value)} />
                        <label>New password</label>
                        <input
                            type="password"
                            placeholder="New strong password"
                            value={resetPassword}
                            onChange={(e) => setResetPassword(e.target.value)}
                        />
                        <button type="button" className="login-btn" onClick={handleResetPassword} disabled={isResetting}>
                            Reset Password
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Login;
