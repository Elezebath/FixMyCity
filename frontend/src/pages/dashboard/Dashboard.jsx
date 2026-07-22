import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMyIncidents } from '../../services/incidentService';
import { normalizeIncident, formatStatus, formatRelative, STATUS_CLASS } from '../../utils/incidentHelpers';
import './Dashboard.css';

function StatusBadge({ status }) {
    return <span className={STATUS_CLASS[status] || 'badge'}>{formatStatus(status)}</span>;
}

function computeStats(incidents) {
    const total = incidents.length;
    const open = incidents.filter((i) => i.status === 'NEW').length;
    const inProgress = incidents.filter(
        (i) => i.status === 'ASSIGNED' || i.status === 'IN_PROGRESS'
    ).length;
    const resolved = incidents.filter(
        (i) => i.status === 'RESOLVED' || i.status === 'CLOSED'
    ).length;

    return [
        { label: 'Total incidents', value: total, icon: '📋', tone: 'neutral' },
        { label: 'Open', value: open, icon: '⚠️', tone: 'open' },
        { label: 'In progress', value: inProgress, icon: '🔧', tone: 'progress' },
        { label: 'Resolved', value: resolved, icon: '✅', tone: 'resolved' },
    ];
}

function Dashboard() {
    const storedUser = JSON.parse(localStorage.getItem('user') || 'null');
    const firstName = storedUser?.fullName?.split(/\s+/)[0] || 'there';

    const [incidents, setIncidents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        (async () => {
            setLoading(true);
            setError('');
            try {
                const data = await getMyIncidents();
                const normalized = data.map(normalizeIncident);
                normalized.sort(
                    (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
                );
                setIncidents(normalized);
            } catch (err) {
                setError(err.message || 'Failed to load your incidents.');
            } finally {
                setLoading(false);
            }
        })();
    }, []);

    const stats = computeStats(incidents);
    const recent = incidents.slice(0, 5);

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

            {error && <p className="dashboard__error">{error}</p>}

            <div className="dashboard__stats">
                {stats.map((s) => (
                    <div key={s.label} className="stat-card">
                        <span className={`stat-card__icon stat-card__icon--${s.tone}`}>
                            {s.icon}
                        </span>
                        <div className="stat-card__body">
                            <span className="stat-card__value">{loading ? '—' : s.value}</span>
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

                {loading ? (
                    <p className="dashboard__state">Loading your incidents…</p>
                ) : recent.length === 0 ? (
                    <p className="dashboard__state">
                        You haven't reported any incidents yet.{' '}
                        <Link to="/app/report">Report one now</Link>.
                    </p>
                ) : (
                    <table className="incidents-table">
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Title</th>
                            <th>Category</th>
                            <th>Assigned to</th>
                            <th>Status</th>
                            <th>Reported</th>
                        </tr>
                        </thead>
                        <tbody>
                        {recent.map((inc) => (
                            <tr key={inc.id}>
                                <td className="incidents-table__id">#{inc.id}</td>
                                <td>
                                    <div className="incidents-table__title">{inc.title}</div>
                                    <div className="incidents-table__location">
                                        {inc.address}
                                    </div>
                                    {inc.latestComment && (
                                        <div className="incidents-table__comment">
                                            💬 {inc.latestCommentBy ? `${inc.latestCommentBy}: ` : ''}
                                            "{inc.latestComment}"
                                        </div>
                                    )}
                                </td>
                                <td>{inc.category}</td>
                                <td>{inc.assignedCompanyName || '—'}</td>
                                <td>
                                    <StatusBadge status={inc.status} />
                                    {inc.resolvedAt && (
                                        <div className="incidents-table__resolved-date">
                                            Completed {formatRelative(inc.resolvedAt)}
                                        </div>
                                    )}
                                </td>
                                <td className="incidents-table__reported">
                                    {formatRelative(inc.createdAt)}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
}

export default Dashboard;