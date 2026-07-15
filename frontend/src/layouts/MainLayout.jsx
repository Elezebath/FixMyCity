import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import './MainLayout.css';

const NAV_ITEMS = [
    { to: '/app/dashboard', label: 'Dashboard', icon: '📊' },
    { to: '/app/incidents', label: 'Incidents', icon: '📋' },
    { to: '/app/report', label: 'Report Issue', icon: '➕' },
];

function MainLayout() {
    const navigate = useNavigate();

    const storedUser = JSON.parse(localStorage.getItem('user') || 'null');
    const user = storedUser || { fullName: '', role: '' };

    const initials = user.fullName
        ? user.fullName
            .trim()
            .split(/\s+/)
            .slice(0, 2)
            .map((part) => part[0])
            .join('')
            .toUpperCase()
        : '?';

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('tokenType');
        localStorage.removeItem('user');
        navigate('/');
    };

    return (
        <div className="layout">
            <aside className="sidebar">
                <div className="sidebar__brand">
                    <span className="sidebar__logo">F</span>
                    <span>FixMyCity</span>
                </div>

                <nav className="sidebar__nav">
                    {NAV_ITEMS.map((item) => (
                        <NavLink
                            key={item.to}
                            to={item.to}
                            className={({ isActive }) =>
                                isActive ? 'sidebar__link active' : 'sidebar__link'
                            }
                        >
                            <span className="sidebar__link-icon">{item.icon}</span>
                            {item.label}
                        </NavLink>
                    ))}
                </nav>

                <button className="sidebar__user" onClick={handleLogout} title="Log out">
                    <span className="sidebar__avatar">{initials}</span>
                    <span className="sidebar__user-info">
                        <span className="sidebar__user-name">{user.fullName || 'Unknown'}</span>
                        <span className="sidebar__user-role">{user.role}</span>
                    </span>
                    <span className="sidebar__logout-icon">⚙</span>
                </button>
            </aside>

            <main className="layout__content">
                <Outlet />
            </main>
        </div>
    );
}

export default MainLayout;