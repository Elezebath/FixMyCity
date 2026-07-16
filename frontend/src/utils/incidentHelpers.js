/** Backend IncidentStatus enum values */
export const INCIDENT_STATUSES = [
    'NEW',
    'ASSIGNED',
    'IN_PROGRESS',
    'RESOLVED',
    'REJECTED',
    'CLOSED',
];

export const STATUS_CLASS = {
    NEW: 'badge badge--open',
    Open: 'badge badge--open',
    ASSIGNED: 'badge badge--assigned',
    Assigned: 'badge badge--assigned',
    IN_PROGRESS: 'badge badge--progress',
    'In progress': 'badge badge--progress',
    RESOLVED: 'badge badge--resolved',
    Resolved: 'badge badge--resolved',
    REJECTED: 'badge badge--rejected',
    CLOSED: 'badge badge--closed',
};

export function formatStatus(status) {
    if (!status) return '—';
    return String(status).replace(/_/g, ' ');
}

export function formatDateTime(value) {
    if (!value) return '—';
    try {
        const d = new Date(value);
        if (Number.isNaN(d.getTime())) return String(value);
        return d.toLocaleString(undefined, {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    } catch {
        return String(value);
    }
}

export function formatRelative(value) {
    if (!value) return '';
    try {
        const d = new Date(value);
        const diffMs = Date.now() - d.getTime();
        if (Number.isNaN(diffMs)) return '';
        const mins = Math.floor(diffMs / 60000);
        if (mins < 1) return 'just now';
        if (mins < 60) return `${mins}m ago`;
        const hours = Math.floor(mins / 60);
        if (hours < 24) return `${hours}h ago`;
        const days = Math.floor(hours / 24);
        if (days < 30) return `${days}d ago`;
        return formatDateTime(value);
    } catch {
        return '';
    }
}

/** Normalize API incident (incidentId) for UI */
export function normalizeIncident(raw) {
    if (!raw) return null;
    return {
        ...raw,
        id: raw.incidentId ?? raw.id,
        category: raw.categoryName ?? raw.category,
        address: raw.locationAddress ?? raw.address,
        reportedBy: raw.citizenName ?? raw.reportedBy,
        photoUrl: raw.attachment?.filePath
            ? `${import.meta.env.VITE_API_BASE_URL}${raw.attachment.filePath.startsWith('/') ? '' : '/'}${raw.attachment.filePath}`
            : raw.photoUrl || null,
        attachmentName: raw.attachment?.fileName || null,
    };
}

export const ALLOWED_ATTACHMENT_TYPES = [
    'image/jpeg',
    'image/png',
    'image/gif',
    'image/webp',
    'application/pdf',
];

export const MAX_ATTACHMENT_BYTES = 5 * 1024 * 1024; // 5 MB

export function validateAttachment(file) {
    if (!file) return null;
    if (!ALLOWED_ATTACHMENT_TYPES.includes(file.type)) {
        return 'Invalid file type. Allowed: JPEG, PNG, GIF, WebP, PDF.';
    }
    if (file.size > MAX_ATTACHMENT_BYTES) {
        return 'File size exceeds maximum of 5 MB.';
    }
    return null;
}
