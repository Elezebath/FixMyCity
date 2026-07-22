import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getIncidents } from '../../services/incidentService.js';
import {
    STATUS_CLASS,
    formatStatus,
    formatRelative,
    normalizeIncident,
} from '../../utils/incidentHelpers.js';
import { getCurrentUser } from '../../utils/auth.js';
import './Home.css';

function StatusBadge({ status }) {
    return (
        <span className={STATUS_CLASS[status] || 'badge'}>
            {formatStatus(status)}
        </span>
    );
}

function Home() {
    const navigate = useNavigate();
    const user = getCurrentUser();

    const [incidents, setIncidents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        let cancelled = false;

        async function loadIncidents() {
            setLoading(true);
            setError('');
            try {
                const data = await getIncidents();
                const list = Array.isArray(data) ? data : data.content || [];
                const sorted = list
                    .map(normalizeIncident)
                    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
                if (!cancelled) setIncidents(sorted);
            } catch (err) {
                if (!cancelled) setError(err.message || 'Failed to load incidents.');
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        loadIncidents();
        return () => {
            cancelled = true;
        };
    }, []);

    return (
        <div className="home-page">
            <header className="home-header">
                <div className="home-header__brand">
                    <span className="home-header__logo">🏙️</span>
                    <span>FixMyCity</span>
                </div>

                <div className="home-header__actions">
                    {user ? (
                        <button
                            className="home-header__btn home-header__btn--primary"
                            onClick={() => navigate('/app/dashboard')}
                        >
                            Go to dashboard
                        </button>
                    ) : (
                        <>
                            <button
                                className="home-header__btn"
                                onClick={() => navigate('/login')}
                            >
                                Log in
                            </button>
                            <button
                                className="home-header__btn home-header__btn--primary"
                                onClick={() => navigate('/login', { state: { mode: 'signup' } })}
                            >
                                Register
                            </button>
                        </>
                    )}
                </div>
            </header>

            <main className="home-content">
                <h1>Recent incidents</h1>
                <p className="home-content__subtitle">
                    Latest infrastructure issues reported around the city.
                </p>

                {loading && <p className="incidents-page__state">Loading incidents...</p>}

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
                            {incidents.map((inc) => (
                                <tr key={inc.id}>
                                    <td className="incidents-table__id">#{inc.id}</td>
                                    <td>
                                        <div className="incidents-table__title">{inc.title}</div>
                                        <div className="incidents-table__location">
                                            {inc.address || '-'}
                                        </div>
                                    </td>
                                    <td>{inc.category || '-'}</td>
                                    <td>
                                        <StatusBadge status={inc.status} />
                                    </td>
                                    <td className="incidents-table__reported">
                                        {formatRelative(inc.createdAt) || '-'}
                                    </td>
                                </tr>
                            ))}

                            {incidents.length === 0 && (
                                <tr>
                                    <td colSpan={5} className="incidents-table__empty">
                                        No incidents yet.
                                    </td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                )}
            </main>
        </div>
    );
}

export default Home;