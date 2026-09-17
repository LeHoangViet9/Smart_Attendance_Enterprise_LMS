import React, { useState, useEffect } from 'react';
import axiosInstance from '../../api/axios';
import './Profile.css';

const Profile = () => {
    const [user, setUser] = useState(null);
    const [pwdData, setPwdData] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
    const [alert, setAlert] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        // Fallback to local storage first for instant render
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            setUser(JSON.parse(storedUser));
        }

        // Fetch detailed profile from backend
        axiosInstance.get('/v1/users/me')
            .then(res => {
                if (res.data && res.data.success) {
                    setUser(res.data.data);
                }
            })
            .catch(err => {
                console.error("Failed to fetch detailed profile", err);
            });
    }, []);

    const handlePasswordChange = async (e) => {
        e.preventDefault();
        if (pwdData.newPassword !== pwdData.confirmPassword) {
            setAlert({ type: 'error', text: 'New passwords do not match!' });
            return;
        }

        setIsSubmitting(true);
        setAlert(null);
        try {
            const response = await axiosInstance.post('/auth/change-password', {
                oldPassword: pwdData.oldPassword,
                newPassword: pwdData.newPassword
            });
            if (response.data.success) {
                setAlert({ type: 'success', text: response.data.message || 'Password changed successfully!' });
                setPwdData({ oldPassword: '', newPassword: '', confirmPassword: '' });
            }
        } catch (error) {
            setAlert({ type: 'error', text: error.response?.data?.message || 'Error changing password!' });
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!user) return <div className="loading-state">Loading Profile...</div>;

    return (
        <div className="profile-container">
            <div className="profile-header">
                <div className="profile-avatar-large">
                    {user.avatarUrl ? (
                        <img src={user.avatarUrl} alt="Avatar" style={{ width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover' }} />
                    ) : (
                        user.fullName ? user.fullName.charAt(0).toUpperCase() : (user.email ? user.email.charAt(0).toUpperCase() : 'U')
                    )}
                </div>
                <div className="profile-title">
                    <h1>{user.fullName || 'User'}</h1>
                    <span className={`role-badge ${user.role?.toLowerCase()}`}>{user.role}</span>
                </div>
            </div>

            <div className="profile-grid">
                <div className="profile-card info-card">
                    <h2>Account Information</h2>
                    <div className="info-group">
                        <label>Full Name</label>
                        <p>{user.fullName || 'Not provided'}</p>
                    </div>
                    <div className="info-group">
                        <label>Email Address</label>
                        <p>{user.email}</p>
                    </div>
                    <div className="info-group">
                        <label>User Code (ID)</label>
                        <p>{user.code || 'N/A'}</p>
                    </div>
                    <div className="info-group">
                        <label>Phone Number</label>
                        <p>{user.phone || 'N/A'}</p>
                    </div>
                    <div className="info-group">
                        <label>Address</label>
                        <p>{user.address || 'N/A'}</p>
                    </div>
                    <div className="info-group">
                        <label>Role</label>
                        <p>{user.role}</p>
                    </div>

                    {user.role === 'LECTURER' && (
                        <>
                            <h3 style={{ marginTop: '20px', color: '#6366f1', borderBottom: '1px solid #e0e7ff', paddingBottom: '8px' }}>Lecturer Details</h3>
                            <div className="info-group" style={{ marginTop: '16px' }}>
                                <label>Degree (Học vị)</label>
                                <p>{user.degree || 'N/A'}</p>
                            </div>
                            <div className="info-group">
                                <label>Department (Khoa)</label>
                                <p>{user.department || 'N/A'}</p>
                            </div>
                            <div className="info-group">
                                <label>Major (Ngành)</label>
                                <p>{user.major || 'N/A'}</p>
                            </div>
                        </>
                    )}

                    {user.role === 'STUDENT' && (
                        <>
                            <h3 style={{ marginTop: '20px', color: '#10b981', borderBottom: '1px solid #dcfce7', paddingBottom: '8px' }}>Student Details</h3>
                            <div className="info-group" style={{ marginTop: '16px' }}>
                                <label>Major (Ngành)</label>
                                <p>{user.major || 'N/A'}</p>
                            </div>
                            <div className="info-group">
                                <label>Class (Lớp)</label>
                                <p>{user.className || 'N/A'}</p>
                            </div>
                            <div className="info-group">
                                <label>Enrollment Year (Khóa)</label>
                                <p>{user.enrollmentYear || 'N/A'}</p>
                            </div>
                            <div className="info-group">
                                <label>Parent Phone (SĐT Phụ huynh)</label>
                                <p>{user.parentPhone || 'N/A'}</p>
                            </div>
                        </>
                    )}
                </div>

                <div className="profile-card password-card">
                    <h2>Change Password</h2>
                    {alert && <div className={`alert-box ${alert.type}`}>{alert.text}</div>}
                    <form onSubmit={handlePasswordChange} className="password-form">
                        <div className="form-group">
                            <label>Current Password</label>
                            <input 
                                type="password" 
                                value={pwdData.oldPassword} 
                                onChange={e => setPwdData({ ...pwdData, oldPassword: e.target.value })} 
                                required 
                                className="profile-input" 
                            />
                        </div>
                        <div className="form-group">
                            <label>New Password</label>
                            <input 
                                type="password" 
                                value={pwdData.newPassword} 
                                onChange={e => setPwdData({ ...pwdData, newPassword: e.target.value })} 
                                required 
                                className="profile-input" 
                            />
                        </div>
                        <div className="form-group">
                            <label>Confirm New Password</label>
                            <input 
                                type="password" 
                                value={pwdData.confirmPassword} 
                                onChange={e => setPwdData({ ...pwdData, confirmPassword: e.target.value })} 
                                required 
                                className="profile-input" 
                            />
                        </div>
                        <button type="submit" className="btn-primary" disabled={isSubmitting}>
                            {isSubmitting ? 'Processing...' : 'Update Password'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default Profile;
