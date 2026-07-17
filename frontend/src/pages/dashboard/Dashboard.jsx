import { Link } from 'react-router-dom';
import './Dashboard.css';

// TODO: GET /api/incidents
const STATS = [
    { label: 'Total incidents', value: 3, icon: '📋', tone: 'neutral' },
    { label: 'Open', value: 1, icon: '⚠️', tone: 'open' },
    { label: 'In progress', value: 1, icon: '🔧', tone: 'progress' },
    { label: 'Resolved', value: 1, icon: '✅', tone: 'resolved' },
];

const RECENT = [
    {
        id: 1005,
        title: 'Damaged pedestrian crossing sign',
        location: 'School Rd near Lincoln Elementary',
        category: 'Public Safety',
        status: 'Open',
        reported: '8h ago',
    },
    {
        id: 1001,
        title: 'Large pothole on Main St',
        location: '221 Main Street, near 5th Ave crossing',
        category: 'Road & Potholes',
        status: 'Assigned',
        reported: '4d ago',
    },
    {
        id: 1003,
        title: 'Overflowing garbage bin',
        location: 'Corner of Elm & Oak, near bus stop',
        category: 'Waste & Sanitation',
        status: 'Resolved',
        reported: '10d ago',
    },
];

const STATUS_CLASS = {
    Open: 'badge badge--open',
    Assigned: 'badge badge--assigned',
    'In progress': 'badge badge--progress',
    Resolved: 'badge badge--resolved',
};

function StatusBadge({ status }) {
    return <span className={STATUS_CLASS[status] || 'badge'}>{status}</span>;
}

function Dashboard() {
    const storedUser = JSON.parse(localStorage.getItem('user') || 'null');
    const firstName = storedUser?.fullName?.split(/\s+/)[0] || 'there';

    return (
        <div className="dashboard">
            <div className="dashboard__header">
                <div>
                    <h1>Welcome back, {firstName}</h1>
                    <p className="dashboard__subtitle">
                        Your recent reports and their current status.
                    </p>
                </div>
                <Link to="/app/report" className="dashboard__report-btn">
                    + Report Issue
                </Link>
            </div>

            <div className="dashboard__stats">
                {STATS.map((s) => (
                    <div key={s.label} className="stat-card">
                        <span className={`stat-card__icon stat-card__icon--${s.tone}`}>
                            {s.icon}
                        </span>
                        <div className="stat-card__body">
                            <span className="stat-card__value">{s.value}</span>
                            <span className="stat-card__label">{s.label}</span>
                        </div>
                    </div>
                ))}
            </div>

            <div className="dashboard__panel">
                <div className="dashboard__panel-head">
                    <h2>Recent incidents</h2>
                    <Link to="/app/incidents" className="dashboard__view-all">
                        View all →
                    </Link>
                </div>

                <table className="incidents-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Title</th>
                        <th>Category</th>
                        <th>Status</th>
                        <th>Reported</th>
                    </tr>
                    </thead>
                    <tbody>
                    {RECENT.map((inc) => (
                        <tr key={inc.id}>
                            <td className="incidents-table__id">#{inc.id}</td>
                            <td>
                                <div className="incidents-table__title">{inc.title}</div>
                                <div className="incidents-table__location">
                                    {inc.location}
                                </div>
                            </td>
                            <td>{inc.category}</td>
                            <td>
                                <StatusBadge status={inc.status} />
                            </td>
                            <td className="incidents-table__reported">{inc.reported}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default Dashboard;