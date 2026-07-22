function ManagerOrAdminRoute({ children }) {
    const user = JSON.parse(localStorage.getItem('user') || 'null');
    return ['MANAGER', 'ADMIN'].includes(user?.role) ? children : <NotFound />;
}

export default ManagerOrAdminRoute;