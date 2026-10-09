import React from 'react';
import {BrowserRouter, Navigate, Route, Routes} from 'react-router-dom';
import Login from './pages/Login';
import Home from './pages/Home';
import QuizList from './pages/student/Quiz/QuizList';
import QuizDetails from './pages/student/Quiz/QuizDetails';
import QuizAttempt from './pages/student/Quiz/QuizAttempt';
import QuizHistory from './pages/student/Quiz/QuizHistory';
import QuizReview from './pages/student/Quiz/QuizReview';
import QuizManagement from './pages/student/Quiz/QuizManagement';
import QuizGradingList from './pages/lecturer/QuizGrading/QuizGradingList';
import QuizGradingDetail from './pages/lecturer/QuizGrading/QuizGradingDetail';
import FaceOnboarding from './pages/student/FaceOnboarding';
import AssignmentList from './pages/student/Assignment/AssignmentList';
import AssignmentDetails from './pages/student/Assignment/AssignmentDetails';
import AssignmentManagement from './pages/student/Assignment/AssignmentManagement';
import CourseList from './pages/student/Course/CourseList';
import CourseDetails from './pages/student/Course/CourseDetails';
import Dashboard from './pages/Dashboard/Dashboard';
import AdminLayout from './pages/admin/Layout/AdminLayout';
import UserManagement from './pages/admin/UserManagement/UserManagement';
import CourseManagement from './pages/admin/CourseManagement/CourseManagement';
import LecturerClassManagement from './pages/lecturer/ClassManagement/LecturerClassManagement';
import LecturerAttendanceHistory from './pages/lecturer/ClassManagement/LecturerAttendanceHistory';
import AdminClassManagement from './pages/admin/ClassManagement/AdminClassManagement';
import LecturerSmartAttendance from './pages/lecturer/SmartAttendance/LecturerSmartAttendance';
import StudentClassList from './pages/student/Class/StudentClassList';
import StudentAttendanceHistory from './pages/student/Class/StudentAttendanceHistory';
import LecturerCourseManagement from './pages/lecturer/CourseManagement/LecturerCourseManagement';
import LecturerCourseDetails from './pages/lecturer/CourseManagement/LecturerCourseDetails';
import Profile from './pages/Profile/Profile';
import Gradebook from './pages/shared/Gradebook/Gradebook';

const RoleBasedLayout = () => {
  const userString = localStorage.getItem('user');
  const user = userString ? JSON.parse(userString) : null;

  if (user && user.role === 'ADMIN') {
    return <Navigate to="/admin/dashboard" replace />;
  }
  if (user && user.role === 'LECTURER') {
    return <Navigate to="/lecturer/dashboard" replace />;
  }
  return <AdminLayout />;
};

const App = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />

        {/* Shared Routes (Student/Lecturer defaults to Top Nav, Admin defaults to Side Nav) */}
        <Route path="/student" element={<RoleBasedLayout />}>
          <Route path="student-home" element={<Dashboard />} />
          <Route path="profile" element={<Profile />} />
          <Route path="gradebook" element={<Gradebook />} />
          <Route path="courses" element={<CourseList />} />
          <Route path="my-classes" element={<StudentClassList />} />
          <Route path="attendance-history" element={<StudentAttendanceHistory />} />
          <Route path="courses/:id" element={<CourseDetails />} />
          <Route path="quizzes" element={<QuizList />} />
          <Route path="quizzes/manage/:quizId" element={<QuizManagement />} />
          <Route path="quizzes/history" element={<QuizHistory />} />
          <Route path="quizzes/:id" element={<QuizDetails />} />
          <Route path="quizzes/attempts/:attemptId" element={<QuizAttempt />} />
          <Route path="quizzes/attempts/:attemptId/review" element={<QuizReview />} />
          <Route path="assignments" element={<AssignmentList />} />
          <Route path="assignments/manage" element={<AssignmentManagement />} />
          <Route path="assignments/:id" element={<AssignmentDetails />} />
        </Route>

        <Route path="/face-onboarding" element={<FaceOnboarding />} />

        <Route path="/admin" element={<AdminLayout />}>
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="profile" element={<Profile />} />
          <Route path="users" element={<UserManagement />} />
          <Route path="classes" element={<LecturerClassManagement />} />
          <Route path="classes/:id/smart-attendance" element={<LecturerSmartAttendance />} />
          <Route path="classes/:classId/gradebook" element={<Gradebook />} />
          <Route path="classes-admin" element={<AdminClassManagement />} />
          <Route path="courses" element={<CourseManagement />} />
          <Route path="courses/:id" element={<CourseDetails />} />
          <Route path="quizzes" element={<QuizList />} />
          <Route path="quizzes/manage/:quizId" element={<QuizManagement />} />
          <Route path="quizzes/:quizId/grading" element={<QuizGradingList />} />
          <Route path="quizzes/attempts/:attemptId/grading" element={<QuizGradingDetail />} />
          <Route path="assignments" element={<AssignmentList />} />
          <Route path="assignments/manage" element={<AssignmentManagement />} />
          {/* Default fallback */}
          <Route index element={<Navigate to="dashboard" replace />} />
        </Route>

        <Route path="/lecturer" element={<AdminLayout />}>
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="profile" element={<Profile />} />
          <Route path="classes" element={<LecturerClassManagement />} />
          <Route path="classes/:id/smart-attendance" element={<LecturerSmartAttendance />} />
          <Route path="classes/:classId/attendance-history" element={<LecturerAttendanceHistory />} />
          <Route path="classes/:classId/gradebook" element={<Gradebook />} />
          <Route path="courses" element={<LecturerCourseManagement />} />
          <Route path="courses/:id" element={<LecturerCourseDetails />} />
          <Route path="quizzes" element={<QuizList />} />
          <Route path="quizzes/manage/:quizId" element={<QuizManagement />} />
          <Route path="quizzes/:quizId/grading" element={<QuizGradingList />} />
          <Route path="quizzes/attempts/:attemptId/grading" element={<QuizGradingDetail />} />
          <Route path="assignments" element={<AssignmentList />} />
          <Route path="assignments/manage" element={<AssignmentManagement />} />
          <Route index element={<Navigate to="dashboard" replace />} />
        </Route>

        {/* Global Fallback for unknown routes */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
};

export default App;
