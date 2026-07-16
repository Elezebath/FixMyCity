import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { getCurrentUser, hasRole, clearAuth } from '../utils/auth';
import './MainLayout.css';

function MainLayout() {
    const navigate = useNavigate();

    const user = getCurrentUser() || { fullName: '', role: '' };

    const navItems = [
        { to: '/app/dashboard', label: 'Dashboard', icon: '📊' },
        { to: '/app/incidents', label: 'Incidents', icon: '📋' },
        // Report is primarily for citizens (managers/admins can also create via API)
        ...(hasRole('CITIZEN', 'MANAGER', 'ADMIN')
            ? [{ to: '/app/incidents/new', label: 'Report Issue', icon: '➕' }]
            : []),

    ];


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
        clearAuth();
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
                    {navItems.map((item) => (

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