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

/** Target timezone for all displayed timestamps. */
const DISPLAY_TZ = 'Europe/Riga';

/**
 * Backend sends naive LocalDateTime strings (e.g. "2026-07-13T14:30:00") with
 * no timezone/offset info. The DB stores these in UTC (confirmed via
 * `SELECT @@system_time_zone;` -> UTC), so without correction `new Date(...)`
 * would parse them using the *browser's* local timezone instead of UTC,
 * silently shifting every timestamp by the viewer's offset.
 *
 * This appends 'Z' to naive strings so they're parsed as UTC instants.
 * Strings that already carry an explicit offset/zone (e.g. end in 'Z' or
 * '+02:00') are passed through unchanged.
 */
function parseAsUtc(value) {
    if (value instanceof Date) return value;
    if (typeof value !== 'string') return new Date(value);

    const hasExplicitOffset = /Z$|[+-]\d{2}:\d{2}$/.test(value);
    const iso = hasExplicitOffset ? value : `${value}Z`;
    return new Date(iso);
}

export function formatDateTime(value) {
    if (!value) return '—';
    try {
        const d = parseAsUtc(value);
        if (Number.isNaN(d.getTime())) return String(value);
        return d.toLocaleString('en-GB', {
            timeZone: DISPLAY_TZ,
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
        const d = parseAsUtc(value);
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

export function formatDate(value) {
    if (!value) return '—';
    try {
        const d = parseAsUtc(value);
        if (Number.isNaN(d.getTime())) return String(value);
        return d.toLocaleDateString('en-GB', {
            timeZone: DISPLAY_TZ,
            year: 'numeric',
            month: 'short',
            day: 'numeric',
        });
    } catch {
        return String(value);
    }
}

/**
 * Build a browser-loadable URL for an attachment filePath from the API.
 * API returns paths like "/uploads/{uuid}.jpg". Files are served by the
 * backend at the same path (see WebMvcConfig).
 */
export function resolveAttachmentUrl(filePath) {
    if (!filePath) return null;
    // Already absolute
    if (/^https?:\/\//i.test(filePath)) return filePath;

    // Backend stores "/uploads/{file}" and serves it at the same path (WebMvcConfig).
    // Always prefix with the API origin so the browser does not request the Vite host.
    const base = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:3100').replace(
        /\/$/,
        ''
    );
    const path = filePath.startsWith('/') ? filePath : `/${filePath}`;
    return `${base}${path}`;
}


/** Normalize API incident (incidentId) for UI */
export function normalizeIncident(raw) {
    if (!raw) return null;
    const attachment = raw.attachment || null;
    return {
        ...raw,
        id: raw.incidentId ?? raw.id,
        category: raw.categoryName ?? raw.category,
        address: raw.locationAddress ?? raw.address,
        reportedBy: raw.citizenName ?? raw.reportedBy,
        attachment,
        photoUrl: attachment?.filePath
            ? resolveAttachmentUrl(attachment.filePath)
            : raw.photoUrl || null,
        attachmentName: attachment?.fileName || null,
        attachmentType: attachment?.fileType || null,
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