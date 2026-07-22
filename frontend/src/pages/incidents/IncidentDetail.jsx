import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
    getIncident,
    getIncidentStatusHistory,
    getIncidentComments,
    assignCompany,
    resolveIncident,
} from '../../services/incidentService.js';
import { isManagerOrAdmin, isCompany, hasRole } from '../../utils/auth.js';

import {
    STATUS_CLASS,
    formatStatus,
    formatDateTime,
    formatRelative,
    normalizeIncident,
} from '../../utils/incidentHelpers.js';
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
    const [history, setHistory] = useState([]);
    const [comments, setComments] = useState([]);
    const [commentsLoading, setCommentsLoading] = useState(false);
    const [commentsError, setCommentsError] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [notFound, setNotFound] = useState(false);
    const [toast, setToast] = useState('');

    const [companyId, setCompanyId] = useState('');

    const [resolveOpen, setResolveOpen] = useState(false);
    const [comment, setComment] = useState('');
    const [resolveError, setResolveError] = useState('');
    const [resolving, setResolving] = useState(false);

    const loadComments = useCallback(async (numericId) => {
        setCommentsLoading(true);
        setCommentsError('');
        try {
            const data = await getIncidentComments(numericId);
            setComments(Array.isArray(data) ? data : []);
        } catch (err) {
            // 403/401: hide section quietly; other errors surface a message
            if (err.status === 403 || err.status === 401) {
                setComments([]);
                setCommentsError('');
            } else {
                setCommentsError(err.message || 'Failed to load comments.');
            }
        } finally {
            setCommentsLoading(false);
        }
    });

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

            const historyData = await getIncidentStatusHistory(numericId);
            setHistory(historyData);

            // Load comments in parallel for authorized roles only
            await loadComments(numericId);
        } catch (err) {
            if (err.status === 404) {
                setNotFound(true);
            } else {
                setError(err.message || 'Failed to load incident.');
            }
        } finally {
            setLoading(false);
        }
    }, [id, loadComments]);


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

    const fileType = (
        incident.attachmentType ||
        incident.attachment?.fileType ||
        ''
    ).toLowerCase();
    const fileName = (
        incident.attachmentName ||
        incident.attachment?.fileName ||
        incident.photoUrl ||
        ''
    ).toLowerCase();
    // Prefer MIME type; fall back to extension if type is missing/wrong
    const isImage =
        fileType.startsWith('image/') ||
        /\.(jpe?g|png|gif|webp)$/i.test(fileName);
    const isPdf =
        fileType === 'application/pdf' ||
        fileType.includes('pdf') ||
        fileName.endsWith('.pdf');


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
                                <a
                                    href={incident.photoUrl}
                                    target="_blank"
                                    rel="noreferrer"
                                    className="incident-detail__photo-link"
                                    title="Open full image in new tab"
                                >
                                    <img
                                        src={incident.photoUrl}
                                        alt={incident.attachmentName || incident.title}
                                    />
                                </a>
                            ) : incident.photoUrl ? (
                                <a
                                    href={incident.photoUrl}
                                    target="_blank"
                                    rel="noreferrer"
                                    className="incident-detail__attachment-link"
                                >
                                    {isPdf ? '📄' : '📎'}{' '}
                                    {incident.attachmentName || 'View attachment'}
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

                    <div className="incident-detail__card">
                        <h3>Status History</h3>

                        {history.length === 0 ? (
                            <p className="incident-detail__muted">
                                No status changes yet.
                            </p>
                        ) : (
                            <ul className="incident-detail__activity">
                                {history.map(item => (
                                    <li key={item.statusHistoryId}>
                                        <span className="incident-detail__activity-dot" />

                                        <div>
                                            <p className="incident-detail__activity-text">
                                                {item.oldStatus && (
                                                    <>
                                                        <strong>{formatStatus(item.oldStatus)}</strong>
                                                        {' → '}
                                                    </>
                                                )}
                                                <strong>{formatStatus(item.newStatus)}</strong>
                                            </p>

                                            {item.remarks && (
                                                <p className="incident-detail__activity-text">
                                                    {item.remarks}
                                                </p>
                                            )}

                                            {item.changedByName && (
                                                <p className="incident-detail__activity-text">
                                                    Changed by {item.changedByName}
                                                </p>
                                            )}

                                            <span className="incident-detail__activity-time">
                            {formatDateTime(item.changedAt)}
                        </span>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>

                    <div className="incident-detail__card">
                        <h3>Comments</h3>

                        {commentsLoading && (
                            <p className="incident-detail__state">Loading comments…</p>
                        )}

                        {!commentsLoading && commentsError && (
                            <div className="incident-detail__comments-error">
                                <p className="incident-detail__error" role="alert">
                                    {commentsError}
                                </p>
                                <button
                                    type="button"
                                    className="incident-detail__btn"
                                    onClick={() => loadComments(Number(id))}
                                >
                                    Retry
                                </button>
                            </div>
                        )}

                        {!commentsLoading && !commentsError && comments.length === 0 && (
                            <p className="incident-detail__muted">
                                No comments yet.
                            </p>
                        )}

                        {!commentsLoading && !commentsError && comments.length > 0 && (
                            <ul className="incident-detail__comments">
                                {comments.map((item) => (
                                    <li key={item.commentId} className="incident-detail__comment">
                                        <div className="incident-detail__comment-header">
                                                <span className="incident-detail__comment-author">
                                                    {item.authorName || 'Unknown'}
                                                </span>
                                            {item.authorRole && (
                                                <span className="incident-detail__comment-role">
                                                        {item.authorRole}
                                                    </span>
                                            )}
                                            {item.createdAt && (
                                                <span className="incident-detail__comment-time">
                                                        {formatDateTime(item.createdAt)}
                                                    </span>
                                            )}
                                        </div>
                                        <p className="incident-detail__comment-body">
                                            {item.comment}
                                        </p>
                                    </li>
                                ))}
                            </ul>
                        )}
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
