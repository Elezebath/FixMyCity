import { Routes, Route } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import AdminRoute from './components/AdminRoute.jsx';
import ManagerOrAdminRoute from './components/ManagerOrAdminRoute.jsx';
import MainLayout from './layouts/MainLayout.jsx';
import Login from './pages/auth/Login.jsx';
import Dashboard from './pages/dashboard/Dashboard.jsx';
import Incidents from './pages/incidents/Incidents.jsx';
import IncidentDetail from './pages/incidents/IncidentDetail.jsx';
import ReportIssue from './pages/incidents/ReportIssue.jsx';
import AdminUsers from './pages/admin/AdminUsers.jsx';
import NotFound from './pages/NotFound.jsx';
import ForgotPassword from './pages/auth/ForgotPassword.jsx';
import ResetPassword from './pages/auth/ResetPassword.jsx';
import './App.css';
import Assignment from "./pages/assignment/Assignment.jsx";
import AuditLogs from './pages/admin/AuditLogs.jsx';
import Home from './pages/home/Home.jsx';


function App() {
    return (
        <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />

            <Route element={<ProtectedRoute />}>
                <Route path="/app" element={<MainLayout />}>
                    <Route
                        path="admin/users"
                        element={
                            <AdminRoute>
                                <AdminUsers />
                            </AdminRoute>
                        }
                    />
                    <Route path="dashboard" element={<Dashboard />} />
                    <Route path="incidents" element={<Incidents />} />
                    <Route path="incidents/new" element={<ReportIssue />} />
                    <Route path="incidents/:id" element={<IncidentDetail />} />
                    <Route path="report" element={<ReportIssue />} />
                    <Route path="assignment" element={
                        <ManagerOrAdminRoute>
                            <Assignment />
                        </ManagerOrAdminRoute>
                    } />
                    <Route path="admin/logs" element={
                        <AdminRoute>
                            <AuditLogs />
                        </AdminRoute>
                    } />
                </Route>
            </Route>

            <Route path="*" element={<NotFound />} />
        </Routes>
    );
}

export default App;