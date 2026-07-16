import { Navigate } from 'react-router-dom';

function AdminRoute({ children }) {
    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : null;

    if (!user || user.role !== 'ADMIN') {
        return <Navigate to="/app/dashboard" replace />;
    }

    return children;
}

export default AdminRoute;