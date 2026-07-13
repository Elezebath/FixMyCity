import { Link, Outlet } from 'react-router-dom';

function MainLayout() {
    return (
        <div className="layout">
            <header className="navbar">
                <h2>FixMyCity</h2>
                <nav>
                    <Link to="/app/dashboard">Dashboard</Link>
                </nav>
            </header>

            <main className="content">
                <Outlet />
            </main>
        </div>
    );
}

export default MainLayout;