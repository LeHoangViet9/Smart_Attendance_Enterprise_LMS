import React, { useState, useRef, useEffect } from 'react';
import ReactMarkdown from 'react-markdown';
import axiosInstance from '../../api/axios';
import './AITutor.css';

const AITutor = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState([
        { sender: 'bot', text: 'Chào bạn! Mình là AI Tutor. Mình có thể giúp gì cho quá trình học tập của bạn hôm nay?' }
    ]);
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        if (isOpen) {
            scrollToBottom();
        }
    }, [messages, isOpen]);

    const handleSend = async () => {
        if (!input.trim()) return;

        const userMsg = input.trim();
        setMessages(prev => [...prev, { sender: 'user', text: userMsg }]);
        setInput('');
        setLoading(true);

        try {
            const res = await axiosInstance.post('/v1/chatbot/ask', { message: userMsg });
            if (res.data && res.data.success) {
                setMessages(prev => [...prev, { sender: 'bot', text: res.data.data.reply }]);
            } else {
                setMessages(prev => [...prev, { sender: 'bot', text: 'Lỗi: ' + (res.data.message || 'Không thể lấy câu trả lời từ máy chủ.') }]);
            }
        } catch (error) {
            console.error("AI Tutor Error:", error);
            setMessages(prev => [...prev, { sender: 'bot', text: 'Xin lỗi, hệ thống đang bận. Vui lòng thử lại sau!' }]);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={`ai-tutor-container ${isOpen ? 'open' : ''}`}>
            {isOpen ? (
                <div className="ai-tutor-window">
                    <div className="ai-tutor-header">
                        <div className="ai-tutor-info">
                            <span className="bot-avatar">🤖</span>
                            <div>
                                <h4>AI Tutor</h4>
                                <span className="status">● Online</span>
                            </div>
                        </div>
                        <button className="close-btn" onClick={() => setIsOpen(false)}>×</button>
                    </div>
                    
                    <div className="ai-tutor-messages">
                        {messages.map((msg, idx) => (
                            <div key={idx} className={`message-wrapper ${msg.sender}`}>
                                <div className="message-bubble markdown-body">
                                    {msg.sender === 'bot' ? (
                                        <ReactMarkdown>{msg.text}</ReactMarkdown>
                                    ) : (
                                        msg.text
                                    )}
                                </div>
                            </div>
                        ))}
                        {loading && (
                            <div className="message-wrapper bot">
                                <div className="message-bubble typing">
                                    <span className="dot"></span>
                                    <span className="dot"></span>
                                    <span className="dot"></span>
                                </div>
                            </div>
                        )}
                        <div ref={messagesEndRef} />
                    </div>

                    <div className="ai-tutor-input">
                        <input
                            type="text"
                            placeholder="Nhập câu hỏi của bạn..."
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            onKeyDown={(e) => e.key === 'Enter' && handleSend()}
                            disabled={loading}
                        />
                        <button className="send-btn" onClick={handleSend} disabled={loading || !input.trim()}>
                            ➤
                        </button>
                    </div>
                </div>
            ) : (
                <button className="ai-tutor-fab" onClick={() => setIsOpen(true)}>
                    🤖 <span className="tooltip">Hỏi Gia Sư AI</span>
                </button>
            )}
        </div>
    );
};

export default AITutor;
