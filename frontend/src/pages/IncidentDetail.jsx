import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
    getIncident,
    assignCompany,
    resolveIncident,
} from '../services/incidentService';
import { isManagerOrAdmin, isCompany } from '../utils/auth';
import {
    STATUS_CLASS,
    formatStatus,
    formatDateTime,
    formatRelative,
    normalizeIncident,
} from '../utils/incidentHelpers';
import './IncidentDetail.css';

function StatusBadge({ status }) {
    return (
        <span className={STATUS_CLASS[status] || 'badge'}>{formatStatus(status)}</span>
    );
}

function Modal({ title, onClose, children }) {
    return (
        <div className="modal-overlay" role="dialog" aria-modal="true" aria-label={title}>
            <div className="modal">
                <div className="modal__header">
                    <h2 className="modal__title">{title}</h2>
                    <button
                        type="button"
                        className="modal__close"
                        onClick={onClose}
                        aria-label="Close"
                    >
                        ×
                    </button>
                </div>
                <div className="modal__body">{children}</div>
            </div>
        </div>
    );
}

function IncidentDetail() {
    const { id } = useParams();
    const [incident, setIncident] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [notFound, setNotFound] = useState(false);
    const [toast, setToast] = useState('');

    const [assignOpen, setAssignOpen] = useState(false);
    const [companyId, setCompanyId] = useState('');
    const [assignError, setAssignError] = useState('');
    const [assigning, setAssigning] = useState(false);

    const [resolveOpen, setResolveOpen] = useState(false);
    const [comment, setComment] = useState('');
    const [resolveError, setResolveError] = useState('');
    const [resolving, setResolving] = useState(false);

    const load = useCallback(async () => {
        const numericId = Number(id);
        if (!id || Number.isNaN(numericId) || numericId <= 0) {
            setNotFound(true);
            setLoading(false);
            return;
        }

        setLoading(true);
        setError('');
        setNotFound(false);
        try {
            const data = await getIncident(numericId);
            setIncident(normalizeIncident(data));
        } catch (err) {
            if (err.status === 404) {
                setNotFound(true);
            } else {
                setError(err.message || 'Failed to load incident.');
            }
        } finally {
            setLoading(false);
        }
    }, [id]);

    useEffect(() => {
        load();
    }, [load]);

    useEffect(() => {
        if (!toast) return undefined;
        const t = setTimeout(() => setToast(''), 3500);
        return () => clearTimeout(t);
    }, [toast]);

    const status = incident?.status;
    const isResolved =
        status === 'RESOLVED' || status === 'CLOSED' || status === 'REJECTED';

    const showAssign =
        isManagerOrAdmin() && incident && !isResolved;

    // COMPANY role only; hide when already resolved.
    // Backend also checks assigned company match — we cannot know companyId from
    // IncidentResponse (not included), so we show the button and surface API errors.
    const showResolve = isCompany() && incident && !isResolved;

    const handleAssign = async (e) => {
        e.preventDefault();
        setAssignError('');
        if (!companyId || Number(companyId) <= 0) {
            setAssignError('Please enter a valid company ID.');
            return;
        }
        setAssigning(true);
        try {
            const updated = await assignCompany(incident.id, companyId);
            setIncident(normalizeIncident(updated));
            setAssignOpen(false);
            setCompanyId('');
            setToast('Incident assigned successfully.');
        } catch (err) {
            setAssignError(err.message || 'Failed to assign company.');
        } finally {
            setAssigning(false);
        }
    };

    const handleResolve = async (e) => {
        e.preventDefault();
        setResolveError('');
        const trimmed = comment.trim();
        if (!trimmed) {
            setResolveError('Comment is required when resolving an incident.');
            return;
        }
        if (trimmed.length < 5) {
            setResolveError('Comment must be at least 5 characters.');
            return;
        }
        if (trimmed.length > 1000) {
            setResolveError('Comment must be at most 1000 characters.');
            return;
        }
        setResolving(true);
        try {
            const updated = await resolveIncident(incident.id, trimmed);
            setIncident(normalizeIncident(updated));
            setResolveOpen(false);
            setComment('');
            setToast('Incident resolved successfully.');
        } catch (err) {
            setResolveError(err.message || 'Failed to resolve incident.');
        } finally {
            setResolving(false);
        }
    };

    if (loading) {
        return (
            <div className="incident-detail">
                <Link to="/app/incidents" className="incident-detail__back">
                    ← Back to incidents
                </Link>
                <p className="incident-detail__state">Loading incident…</p>
            </div>
        );
    }

    if (notFound) {
        return (
            <div className="incident-detail">
                <Link to="/app/incidents" className="incident-detail__back">
                    ← Back to incidents
                </Link>
                <p className="incident-detail__notfound">Incident not found.</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="incident-detail">
                <Link to="/app/incidents" className="incident-detail__back">
                    ← Back to incidents
                </Link>
                <p className="incident-detail__error" role="alert">
                    {error}
                </p>
                <button type="button" className="incident-detail__btn" onClick={load}>
                    Retry
                </button>
            </div>
        );
    }

    if (!incident) return null;

    const isImage =
        incident.attachment?.fileType?.startsWith('image/') ||
        (incident.photoUrl &&
            !incident.attachment?.fileType?.includes('pdf'));

    return (
        <div className="incident-detail">
            <Link to="/app/incidents" className="incident-detail__back">
                ← Back to incidents
            </Link>

            {toast && (
                <div className="incident-detail__toast" role="status">
                    {toast}
                </div>
            )}

            <div className="incident-detail__title-row">
                <div>
                    <h1 className="incident-detail__title">{incident.title}</h1>
                    <div className="incident-detail__meta">
                        <StatusBadge status={incident.status} />
                        <span>#{incident.id}</span>
                        {incident.createdAt && (
                            <span>· Reported {formatRelative(incident.createdAt)}</span>
                        )}
                    </div>
                </div>

                <div className="incident-detail__actions">
                    {showAssign && (
                        <button
                            type="button"
                            className="incident-detail__btn incident-detail__btn--primary"
                            onClick={() => {
                                setAssignError('');
                                setAssignOpen(true);
                            }}
                        >
                            Assign to Company
                        </button>
                    )}
                    {showResolve && (
                        <button
                            type="button"
                            className="incident-detail__btn incident-detail__btn--success"
                            onClick={() => {
                                setResolveError('');
                                setResolveOpen(true);
                            }}
                        >
                            Resolve Incident
                        </button>
                    )}
                </div>
            </div>

            <div className="incident-detail__grid">
                <div className="incident-detail__main">
                    <div className="incident-detail__card">
                        <div className="incident-detail__photo">
                            {incident.photoUrl && isImage ? (
                                <img src={incident.photoUrl} alt={incident.title} />
                            ) : incident.photoUrl ? (
                                <a
                                    href={incident.photoUrl}
                                    target="_blank"
                                    rel="noreferrer"
                                    className="incident-detail__attachment-link"
                                >
                                    📎 {incident.attachmentName || 'View attachment'}
                                </a>
                            ) : (
                                <span className="incident-detail__photo-empty">
                                    No attachment
                                </span>
                            )}
                        </div>

                        <h3>Description</h3>
                        <p className="incident-detail__description">
                            {incident.description}
                        </p>
                    </div>

                    {/* Comments/history are not returned by IncidentResponse today.
                        Resolve still creates a Comment server-side; UI notes that. */}
                    <div className="incident-detail__card">
                        <h3>Activity & Comments</h3>
                        <p className="incident-detail__muted">
                            Comment history is not included in the incident API response
                            yet. Closing comments are stored when an incident is resolved.
                        </p>
                    </div>
                </div>

                <aside className="incident-detail__card incident-detail__side">
                    <h3>Details</h3>
                    <dl className="incident-detail__props">
                        <div>
                            <dt>Category</dt>
                            <dd>{incident.category || '—'}</dd>
                        </div>
                        <div>
                            <dt>Address</dt>
                            <dd>{incident.address || '—'}</dd>
                        </div>
                        <div>
                            <dt>Reported by</dt>
                            <dd>{incident.reportedBy || '—'}</dd>
                        </div>
                        <div>
                            <dt>Priority</dt>
                            <dd>{incident.priority || '—'}</dd>
                        </div>
                        <div>
                            <dt>Status</dt>
                            <dd>
                                <StatusBadge status={incident.status} />
                            </dd>
                        </div>
                        <div>
                            <dt>Created</dt>
                            <dd>{formatDateTime(incident.createdAt)}</dd>
                        </div>
                    </dl>
                </aside>
            </div>

            {assignOpen && (
                <Modal
                    title="Assign to Company"
                    onClose={() => !assigning && setAssignOpen(false)}
                >
                    <form onSubmit={handleAssign} className="modal-form">
                        <p className="modal-form__hint">
                            {/* No public company list endpoint exists yet — managers enter company ID. */}
                            Enter the company ID to assign this incident. There is no
                            company list API yet.
                        </p>
                        <label className="modal-form__field">
                            Company ID
                            <input
                                type="number"
                                min={1}
                                step={1}
                                value={companyId}
                                onChange={(e) => setCompanyId(e.target.value)}
                                placeholder="e.g. 1"
                                disabled={assigning}
                                autoFocus
                            />
                        </label>
                        {assignError && (
                            <p className="modal-form__error" role="alert">
                                {assignError}
                            </p>
                        )}
                        <div className="modal-form__actions">
                            <button
                                type="button"
                                className="incident-detail__btn"
                                onClick={() => setAssignOpen(false)}
                                disabled={assigning}
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                className="incident-detail__btn incident-detail__btn--primary"
                                disabled={assigning || !companyId}
                            >
                                {assigning ? 'Assigning…' : 'Confirm assign'}
                            </button>
                        </div>
                    </form>
                </Modal>
            )}

            {resolveOpen && (
                <Modal
                    title="Resolve Incident"
                    onClose={() => !resolving && setResolveOpen(false)}
                >
                    <form onSubmit={handleResolve} className="modal-form">
                        <label className="modal-form__field">
                            Closing comment
                            <textarea
                                rows={4}
                                value={comment}
                                onChange={(e) => setComment(e.target.value)}
                                placeholder="Describe how the issue was resolved…"
                                maxLength={1000}
                                disabled={resolving}
                                autoFocus
                            />
                        </label>
                        {resolveError && (
                            <p className="modal-form__error" role="alert">
                                {resolveError}
                            </p>
                        )}
                        <div className="modal-form__actions">
                            <button
                                type="button"
                                className="incident-detail__btn"
                                onClick={() => setResolveOpen(false)}
                                disabled={resolving}
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                className="incident-detail__btn incident-detail__btn--success"
                                disabled={resolving || comment.trim().length < 5}
                            >
                                {resolving ? 'Resolving…' : 'Confirm resolve'}
                            </button>
                        </div>
                    </form>
                </Modal>
            )}
        </div>
    );
}

export default IncidentDetail;
