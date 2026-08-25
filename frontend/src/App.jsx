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
import AssignmentList from './pages/student/Assignment/AssignmentList';
import AssignmentDetails from './pages/student/Assignment/AssignmentDetails';
import AssignmentManagement from './pages/student/Assignment/AssignmentManagement';
import CourseList from './pages/student/Course/CourseList';
import CourseDetails from './pages/student/Course/CourseDetails';
import Dashboard from './pages/Dashboard/Dashboard';
import AdminLayout from './pages/admin/Layout/AdminLayout';
import UserManagement from './pages/admin/UserManagement/UserManagement';

const App = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />

        {/* Student Routes */}
        <Route path="/student" element={<Layout />}>
          <Route path="student-home" element={<Dashboard />} />
          <Route path="courses" element={<CourseList />} />
          <Route path="courses/:id" element={<CourseDetails />} />
          <Route path="quizzes" element={<QuizList />} />
          <Route path="quizzes/history" element={<QuizHistory />} />
          <Route path="quizzes/:id" element={<QuizDetails />} />
          <Route path="quizzes/attempts/:attemptId" element={<QuizAttempt />} />
          <Route path="quizzes/attempts/:attemptId/review" element={<QuizReview />} />
          <Route path="assignments" element={<AssignmentList />} />
          <Route path="assignments/:id" element={<AssignmentDetails />} />
          <Route path="assignments/manage" element={<AssignmentManagement />} />
        </Route>

        <Route path="/face-onboarding" element={<FaceOnboarding />} />

        {/* Admin Routes */}
        <Route path="/admin" element={<AdminLayout />}>
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="users" element={<UserManagement />} />
          {/* Default fallback */}
          <Route index element={<Navigate to="dashboard" replace />} />
        </Route>

        <Route path="/lecturer-home" element={<Dashboard />} />
      </Routes>
    </BrowserRouter>
  );
};

export default App;
