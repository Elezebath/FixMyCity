import { Link, useParams } from 'react-router-dom';
import './IncidentDetail.css';

// TODO GET /api/incidents/:id
const INCIDENTS = {
    1005: {
        id: 1005,
        title: 'Damaged pedestrian crossing sign',
        status: 'Open',
        category: 'Public Safety',
        address: 'School Rd near Lincoln Elementary',
        reportedBy: 'Aisha Patel',
        assignedTo: null,
        createdAt: 'Jul 13 · 09:00 AM',
        reported: '8h ago',
        photoUrl: null,
        description: 'Sign knocked down, safety hazard for schoolchildren.',
        activity: [
            { actor: 'Aisha Patel', action: 'reported this incident.', at: 'Jul 13 · 09:00 AM' },
        ],
    },
};

const STATUS_CLASS = {
    Open: 'badge badge--open',
    Assigned: 'badge badge--assigned',
    'In progress': 'badge badge--progress',
    Resolved: 'badge badge--resolved',
};

function StatusBadge({ status }) {
    return <span className={STATUS_CLASS[status] || 'badge'}>{status}</span>;
}

function IncidentDetail() {
    const { id } = useParams();
    const incident = INCIDENTS[id];

    if (!incident) {
        return (
            <div className="incident-detail">
                <Link to="/app/incidents" className="incident-detail__back">
                    ← Back to incidents
                </Link>
                <p className="incident-detail__notfound">Incident not found.</p>
            </div>
        );
    }

    return (
        <div className="incident-detail">
            <Link to="/app/incidents" className="incident-detail__back">
                ← Back to incidents
            </Link>

            <h1 className="incident-detail__title">{incident.title}</h1>
            <div className="incident-detail__meta">
                <StatusBadge status={incident.status} />
                <span>#{incident.id}</span>
                <span>· Reported {incident.reported}</span>
            </div>

            <div className="incident-detail__grid">
                <div className="incident-detail__main">
                    <div className="incident-detail__card">
                        <div className="incident-detail__photo">
                            {incident.photoUrl ? (
                                <img src={incident.photoUrl} alt={incident.title} />
                            ) : (
                                <span className="incident-detail__photo-empty">
                                    No photo attached
                                </span>
                            )}
                        </div>

                        <h3>Description</h3>
                        <p className="incident-detail__description">{incident.description}</p>
                    </div>

                    <div className="incident-detail__card">
                        <h3>Activity &amp; Comments</h3>
                        <ul className="incident-detail__activity">
                            {incident.activity.map((item, i) => (
                                <li key={i}>
                                    <span className="incident-detail__activity-dot" />
                                    <div>
                                        <p className="incident-detail__activity-text">
                                            <strong>{item.actor}</strong> {item.action}
                                        </p>
                                        <span className="incident-detail__activity-time">
                                            {item.at}
                                        </span>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    </div>
                </div>

                <aside className="incident-detail__card incident-detail__side">
                    <h3>Details</h3>
                    <dl className="incident-detail__props">
                        <div>
                            <dt>Category</dt>
                            <dd>{incident.category}</dd>
                        </div>
                        <div>
                            <dt>Address</dt>
                            <dd>{incident.address}</dd>
                        </div>
                        <div>
                            <dt>Reported by</dt>
                            <dd>{incident.reportedBy}</dd>
                        </div>
                        <div>
                            <dt>Assigned to</dt>
                            <dd>{incident.assignedTo || 'Unassigned'}</dd>
                        </div>
                        <div>
                            <dt>Status</dt>
                            <dd>
                                <StatusBadge status={incident.status} />
                            </dd>
                        </div>
                        <div>
                            <dt>Created</dt>
                            <dd>{incident.createdAt}</dd>
                        </div>
                    </dl>
                </aside>
            </div>
        </div>
    );
}

export default IncidentDetail;