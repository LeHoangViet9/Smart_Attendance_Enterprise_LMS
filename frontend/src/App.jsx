import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Home from './pages/Home';
import Layout from './pages/student/Layout/Layout';
import QuizList from './pages/student/Quiz/QuizList';
import QuizDetails from './pages/student/Quiz/QuizDetails';
import QuizAttempt from './pages/student/Quiz/QuizAttempt';
import QuizHistory from './pages/student/Quiz/QuizHistory';
import QuizReview from './pages/student/Quiz/QuizReview';
import FaceOnboarding from './pages/student/FaceOnboarding';

const App = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />

        {/* Student Routes */}
        <Route path="/student" element={<Layout />}>
          <Route path="student-home" element={<div style={{ padding: '50px' }}>Chào mừng học viên LMS</div>} />
          <Route path="quizzes" element={<QuizList />} />
          <Route path="quizzes/history" element={<QuizHistory />} />
          <Route path="quizzes/:id" element={<QuizDetails />} />
          <Route path="quizzes/attempts/:attemptId" element={<QuizAttempt />} />
          <Route path="quizzes/attempts/:attemptId/review" element={<QuizReview />} />
        </Route>

        <Route path="/face-onboarding" element={<FaceOnboarding />} />
        <Route path="/admin-home" element={<div style={{ padding: '50px' }}>Chào Admin Quản trị LMS</div>} />
        <Route path="/lecturer-home" element={<div style={{ padding: '50px' }}>Chào Giảng viên LMS</div>} />
      </Routes>
    </BrowserRouter>
  );
};

export default App;
