import { useState, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Incidents.css';

// TODO GET /api/incidents
const INCIDENTS = [
    {
        id: 1005,
        title: 'Damaged pedestrian crossing sign',
        location: 'School Rd near Lincoln Elementary',
        category: 'Public Safety',
        status: 'Open',
        reported: '4h ago',
    },
    {
        id: 1001,
        title: 'Large pothole on Main St',
        location: '221 Main Street, near 5th Ave crossing',
        category: 'Road & Potholes',
        status: 'Assigned',
        reported: '3d ago',
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

const STATUSES = ['Open', 'Assigned', 'In progress', 'Resolved'];
const CATEGORIES = ['Public Safety', 'Road & Potholes', 'Waste & Sanitation'];

const STATUS_CLASS = {
    Open: 'badge badge--open',
    Assigned: 'badge badge--assigned',
    'In progress': 'badge badge--progress',
    Resolved: 'badge badge--resolved',
};

function StatusBadge({ status }) {
    return <span className={STATUS_CLASS[status] || 'badge'}>{status}</span>;
}

function Incidents() {
    const navigate = useNavigate();
    const [search, setSearch] = useState('');
    const [status, setStatus] = useState('');
    const [category, setCategory] = useState('');

    const filtered = useMemo(() => {
        const q = search.trim().toLowerCase();
        return INCIDENTS.filter((inc) => {
            const matchesSearch =
                !q ||
                inc.title.toLowerCase().includes(q) ||
                inc.location.toLowerCase().includes(q) ||
                String(inc.id).includes(q);
            const matchesStatus = !status || inc.status === status;
            const matchesCategory = !category || inc.category === category;
            return matchesSearch && matchesStatus && matchesCategory;
        });
    }, [search, status, category]);

    return (
        <div className="incidents-page">
            <div className="incidents-page__header">
                <div>
                    <h1>Incidents</h1>
                    <p className="incidents-page__count">{filtered.length} incidents</p>
                </div>
                <Link to="/app/report" className="incidents-page__report-btn">
                    + Report Issue
                </Link>
            </div>

            <div className="incidents-page__filters">
                <input
                    type="text"
                    className="incidents-page__search"
                    placeholder="Search by title, address or ID..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />
                <select value={status} onChange={(e) => setStatus(e.target.value)}>
                    <option value="">All statuses</option>
                    {STATUSES.map((s) => (
                        <option key={s} value={s}>
                            {s}
                        </option>
                    ))}
                </select>
                <select value={category} onChange={(e) => setCategory(e.target.value)}>
                    <option value="">All categories</option>
                    {CATEGORIES.map((c) => (
                        <option key={c} value={c}>
                            {c}
                        </option>
                    ))}
                </select>
            </div>

            <div className="incidents-page__panel">
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
                    {filtered.map((inc) => (
                        <tr
                            key={inc.id}
                            className="incidents-table__row--clickable"
                            onClick={() => navigate(`/app/incidents/${inc.id}`)}
                        >
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
                    {filtered.length === 0 && (
                        <tr>
                            <td colSpan={5} className="incidents-table__empty">
                                No incidents match your filters.
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default Incidents;