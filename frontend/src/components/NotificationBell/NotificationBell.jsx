import React, { useState, useEffect, useRef } from 'react';
import axiosInstance from '../../api/axios';
import './NotificationBell.css';

import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';

const NotificationBell = () => {
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef(null);

    const fetchData = async () => {
        try {
            // Fetch unread count
            const countResponse = await axiosInstance.get('/v1/notifications/unread-count');
            if (countResponse.data && countResponse.data.success) {
                setUnreadCount(countResponse.data.data || 0);
            }

            // Fetch notifications (page 0, size 20 by default)
            const response = await axiosInstance.get('/v1/notifications');
            if (response.data && response.data.success) {
                // Handle pagination structure
                const pageData = response.data.data;
                const content = pageData.content !== undefined ? pageData.content : (Array.isArray(pageData) ? pageData : []);
                setNotifications(content);
            }
        } catch (error) {
            console.error('Error fetching notifications:', error);
        }
    };

    useEffect(() => {
        fetchData();
        
        // STOMP WebSocket Connection
        const token = localStorage.getItem('accessToken');
        const stompClient = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            connectHeaders: {
                Authorization: `Bearer ${token}`
            },
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('Connected to WebSocket!');
                // Subscribe to personal queue
                stompClient.subscribe('/user/queue/notifications', (message) => {
                    if (message.body) {
                        const newNotif = JSON.parse(message.body);
                        setNotifications(prev => [newNotif, ...prev]);
                        setUnreadCount(prev => prev + 1);
                        alert(`Có thông báo mới: ${newNotif.title}`); // Simple popup for demo
                    }
                });
                
                // Subscribe to global topic
                stompClient.subscribe('/topic/notifications', (message) => {
                    if (message.body) {
                        const newNotif = JSON.parse(message.body);
                        setNotifications(prev => [newNotif, ...prev]);
                        setUnreadCount(prev => prev + 1);
                    }
                });
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
            }
        });

        stompClient.activate();

        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);

        return () => {
            stompClient.deactivate();
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    const handleMarkAsRead = async (id, isRead) => {
        if (isRead) return;
        try {
            await axiosInstance.put(`/v1/notifications/${id}/read`);
            setNotifications(prev => 
                prev.map(notif => notif.id === id ? { ...notif, isRead: true } : notif)
            );
            setUnreadCount(prev => Math.max(0, prev - 1));
        } catch (error) {
            console.error('Error marking as read:', error);
        }
    };

    const handleMarkAllAsRead = async () => {
        try {
            await axiosInstance.put('/v1/notifications/read-all');
            setNotifications(prev => prev.map(notif => ({ ...notif, isRead: true })));
            setUnreadCount(0);
        } catch (error) {
            console.error('Error marking all as read:', error);
        }
    };

    const formatTime = (isoString) => {
        if (!isoString) return '';
        const d = new Date(isoString);
        return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' + d.toLocaleDateString('vi-VN');
    };

    return (
        <div className="notif-bell-container" ref={dropdownRef}>
            <div className={`notif-bell-icon ${unreadCount > 0 ? 'has-unread' : ''}`} onClick={() => setIsOpen(!isOpen)}>
                <span style={{ fontSize: '1.25rem' }}>🔔</span>
                {unreadCount > 0 && <span className="notif-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>}
            </div>
            
            {isOpen && (
                <div className="notif-dropdown">
                    <div className="notif-header">
                        <div>
                            <h4 style={{ margin: 0 }}>Notifications</h4>
                            <span className="notif-count" style={{ fontSize: '0.8rem', color: '#64748b' }}>{unreadCount} unread</span>
                        </div>
                        {unreadCount > 0 && (
                            <button 
                                onClick={handleMarkAllAsRead}
                                style={{
                                    background: 'none', 
                                    border: 'none', 
                                    color: '#3b82f6', 
                                    cursor: 'pointer', 
                                    fontSize: '0.85rem',
                                    padding: '4px 8px',
                                    borderRadius: '4px'
                                }}
                            >
                                Mark all as read
                            </button>
                        )}
                    </div>
                    <div className="notif-list">
                        {notifications.length === 0 ? (
                            <div className="notif-empty">No notifications yet.</div>
                        ) : (
                            notifications.map(notif => (
                                <div 
                                    key={notif.id} 
                                    className={`notif-item ${!notif.isRead ? 'unread' : ''}`}
                                    onClick={() => handleMarkAsRead(notif.id, notif.isRead)}
                                >
                                    <div className="notif-item-icon">
                                        {notif.type === 'REPORT' ? '⚠️' : 'ℹ️'}
                                    </div>
                                    <div className="notif-item-content">
                                        <div className="notif-item-title">{notif.title}</div>
                                        <div className="notif-item-message">{notif.message}</div>
                                        <div className="notif-item-time">{formatTime(notif.createdAt)}</div>
                                    </div>
                                    {!notif.isRead && <div className="notif-item-dot"></div>}
                                </div>
                            ))
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default NotificationBell;
