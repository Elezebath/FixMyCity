import { useState, useMemo, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getIncidents } from '../services/incidentService';
import { isCitizen } from '../utils/auth';
import {
    INCIDENT_STATUSES,
    STATUS_CLASS,
    formatStatus,
    formatRelative,
    normalizeIncident,
} from '../utils/incidentHelpers';
import './Incidents.css';

function StatusBadge({ status }) {
    return (
        <span className={STATUS_CLASS[status] || 'badge'}>
            {formatStatus(status)}
        </span>
    );
}

function Incidents() {
    const navigate = useNavigate();
    const canReport = isCitizen() || isManagerOrAdmin();

    const [incidents, setIncidents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const [search, setSearch] = useState('');
    const [status, setStatus] = useState('');
    const [category, setCategory] = useState('');

    useEffect(() => {
        let cancelled = false;

        async function loadIncidents() {
            setLoading(true);
            setError('');
            try {
                const data = await getIncidents();
                const list = Array.isArray(data) ? data : data.content || [];
                if (!cancelled) {
                    setIncidents(list.map(normalizeIncident));
                }
            } catch (err) {
                if (!cancelled) {
                    setError(err.message || 'Failed to load incidents.');
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        loadIncidents();
        return () => {
            cancelled = true;
        };
    }, []);

    const categoryOptions = useMemo(() => {
        const names = new Set();
        incidents.forEach((inc) => {
            if (inc.category) names.add(inc.category);
        });
        return Array.from(names).sort();
    }, [incidents]);

    const filtered = useMemo(() => {
        const q = search.trim().toLowerCase();

        return incidents.filter((inc) => {
            const title = (inc.title || '').toLowerCase();
            const location = (inc.address || '').toLowerCase();
            const idStr = String(inc.id ?? '');

            const matchesSearch =
                !q ||
                title.includes(q) ||
                location.includes(q) ||
                idStr.includes(q);

            const matchesStatus = !status || inc.status === status;
            const matchesCategory = !category || inc.category === category;

            return matchesSearch && matchesStatus && matchesCategory;
        });
    }, [incidents, search, status, category]);

    return (
        <div className="incidents-page">
            <div className="incidents-page__header">
                <div>
                    <h1>Incidents</h1>
                    <p className="incidents-page__count">
                        {loading ? 'Loading…' : `${filtered.length} incidents`}
                    </p>
                </div>

                {canReport && (
                    <Link to="/app/incidents/new" className="incidents-page__report-btn">
                        + Report New Incident
                    </Link>
                )}

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
                    {INCIDENT_STATUSES.map((s) => (
                        <option key={s} value={s}>
                            {formatStatus(s)}
                        </option>
                    ))}
                </select>

                <select value={category} onChange={(e) => setCategory(e.target.value)}>
                    <option value="">All categories</option>
                    {categoryOptions.map((c) => (
                        <option key={c} value={c}>
                            {c}
                        </option>
                    ))}
                </select>
            </div>

            {loading && <p className="incidents-page__state">Loading incidents…</p>}

            {error && (
                <p className="incidents-page__error" role="alert">
                    {error}
                </p>
            )}

            {!loading && !error && (
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
                                            {inc.address || '—'}
                                        </div>
                                    </td>
                                    <td>{inc.category || '—'}</td>
                                    <td>
                                        <StatusBadge status={inc.status} />
                                    </td>
                                    <td className="incidents-table__reported">
                                        {formatRelative(inc.createdAt) || '—'}
                                    </td>

                                </tr>
                            ))}

                            {filtered.length === 0 && (
                                <tr>
                                    <td colSpan={5} className="incidents-table__empty">
                                        {incidents.length === 0
                                            ? 'No incidents yet.'
                                            : 'No incidents match your filters.'}
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

export default Incidents;
