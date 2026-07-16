import { Routes, Route } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import MainLayout from './layouts/MainLayout.jsx';
import Login from './pages/Login.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Incidents from './pages/Incidents.jsx';
import IncidentDetail from './pages/IncidentDetail.jsx';
import ReportIssue from './pages/ReportIssue.jsx';
import NotFound from './pages/NotFound.jsx';
import ForgotPassword from './pages/ForgotPassword.jsx';
import ResetPassword from './pages/ResetPassword.jsx';
import './App.css';

function App() {
    return (
        <Routes>
            <Route path="/" element={<Login />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />

            <Route element={<ProtectedRoute />}>
                <Route path="/app" element={<MainLayout />}>
                    <Route path="dashboard" element={<Dashboard />} />
                    <Route path="incidents" element={<Incidents />} />
                    <Route path="incidents/:id" element={<IncidentDetail />} />
                    <Route path="report" element={<ReportIssue />} />
                </Route>
            </Route>

            <Route path="*" element={<NotFound />} />
        </Routes>
    );
}

export default App;