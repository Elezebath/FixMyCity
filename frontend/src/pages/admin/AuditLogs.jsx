import { useEffect, useState } from 'react';
import './AuditLogs.css';

const ENTITY_TYPES = ['CATEGORY', 'USER'];

const ACTION_CLASS = {
    CREATE: 'action-badge action-badge--create',
    UPDATE: 'action-badge action-badge--update',
    DELETE: 'action-badge action-badge--delete',
};

function AuditLogs() {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const [entityType, setEntityType] = useState('');
    const [entityId, setEntityId] = useState('');

    const token = localStorage.getItem('token');
    const tokenType = localStorage.getItem('tokenType');
    const authHeader = { Authorization: `${tokenType} ${token}` };

    const fetchLogs = async () => {
        setLoading(true);
        setError('');
        try {
            const params = new URLSearchParams();
            if (entityType) params.set('entityType', entityType);
            if (entityId) params.set('entityId', entityId);

            const query = params.toString() ? `?${params.toString()}` : '';

            const res = await fetch(
                `${import.meta.env.VITE_API_BASE_URL}/api/admin/audit-logs${query}`,
                { headers: { ...authHeader } }
            );
            if (!res.ok) {
                const err = await res.json().catch(() => null);
                throw new Error(err?.message || 'Failed to load audit logs');
            }
            const data = await res.json();
            setLogs(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void fetchLogs();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleApplyFilters = (e) => {
        e.preventDefault();
        fetchLogs();
    };

    const handleClearFilters = () => {
        setEntityType('');
        setEntityId('');
        setTimeout(fetchLogs, 0);
    };

    return (
        <div className="audit-logs-page">
            <div className="audit-logs-header">
                <h1>Audit Logs</h1>
            </div>

            <form className="audit-logs-filters" onSubmit={handleApplyFilters}>
                <label>
                    Entity type
                    <select value={entityType} onChange={(e) => setEntityType(e.target.value)}>
                        <option value="">All</option>
                        {ENTITY_TYPES.map((t) => (
                            <option key={t} value={t}>{t}</option>
                        ))}
                    </select>
                </label>

                <label>
                    Entity ID
                    <input
                        type="number"
                        min={1}
                        value={entityId}
                        onChange={(e) => setEntityId(e.target.value)}
                        placeholder="e.g. 12"
                    />
                </label>

                <div className="audit-logs-filters__actions">
                    <button type="submit" className="admin-btn-primary">Apply</button>
                    <button type="button" className="filter-btn-clear" onClick={handleClearFilters}>
                        Clear
                    </button>
                </div>
            </form>

            {error && <p className="admin-error">{error}</p>}

            {loading ? (
                <p>Loading...</p>
            ) : logs.length === 0 ? (
                <p className="audit-logs-empty">No audit log entries found.</p>
            ) : (
                <table className="audit-logs-table">
                    <thead>
                    <tr>
                        <th>Timestamp</th>
                        <th>Entity</th>
                        <th>ID</th>
                        <th>Action</th>
                        <th>Performed by</th>
                        <th>Details</th>
                    </tr>
                    </thead>
                    <tbody>
                    {logs.map((log) => (
                        <tr key={log.auditLogId}>
                            <td className="audit-logs-table__timestamp">
                                {new Date(log.timestamp).toLocaleString(undefined, {
                                    year: 'numeric',
                                    month: 'short',
                                    day: 'numeric',
                                    hour: '2-digit',
                                    minute: '2-digit',
                                })}
                            </td>
                            <td className="audit-logs-table__entity">{log.entityType}</td>
                            <td className="audit-logs-table__id">{log.entityId}</td>
                            <td>
                                    <span className={ACTION_CLASS[log.action] || 'action-badge'}>
                                        {log.action}
                                    </span>
                            </td>
                            <td className="audit-logs-table__email">{log.performedByEmail}</td>
                            <td className="audit-logs-table__details">{log.details || '—'}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}

export default AuditLogs;