import { getAuthHeader, clearAuth } from '../utils/auth';

const API_BASE = `${import.meta.env.VITE_API_BASE_URL}/api/incidents`;

/**
 * Parse error body from Spring (field map or { error: "..." }).
 */
async function parseError(response) {
    let body = null;
    try {
        body = await response.json();
    } catch {
        body = null;
    }

    if (response.status === 401) {
        clearAuth();
        window.location.href = '/';
        throw new Error('Session expired. Please sign in again.');
    }

    if (body && typeof body === 'object') {
        if (body.error) return body.error;
        if (body.message) return body.message;
        // Field-level validation map from MethodArgumentNotValidException
        const fieldMessages = Object.entries(body)
            .filter(([k]) => k !== 'status' && k !== 'path' && k !== 'timestamp')
            .map(([, v]) => v)
            .filter(Boolean);
        if (fieldMessages.length) return fieldMessages.join(' ');
    }

    return `Request failed (${response.status})`;
}

function jsonHeaders() {
    return {
        'Content-Type': 'application/json',
        ...getAuthHeader(),
    };
}

export async function getIncidents() {
    const response = await fetch(API_BASE, {
        headers: { ...getAuthHeader() },
    });

    if (!response.ok) {
        throw new Error(await parseError(response));
    }

    return response.json();
}

export async function getIncidentsByStatus(status) {
    const response = await fetch(`${API_BASE}/status/${status}`, {
        headers: { ...getAuthHeader() },
    });
    if (!response.ok) throw new Error(await parseError(response));
    return response.json();
}

export async function getIncident(id) {
    const response = await fetch(`${API_BASE}/${id}`, {
        headers: { ...getAuthHeader() },
    });

    if (!response.ok) {
        if (response.status === 404) {
            throw Object.assign(new Error('Incident not found'), { status: 404 });
        }
        throw new Error(await parseError(response));
    }

    return response.json();
}

/**
 * Create incident — multipart/form-data (backend uses @ModelAttribute).
 * Fields: title, description, categoryId, locationAddress, attachment?
 */
export async function createIncident(formData) {
    // Do NOT set Content-Type — browser sets multipart boundary
    const response = await fetch(API_BASE, {
        method: 'POST',
        headers: { ...getAuthHeader() },
        body: formData,
    });

    if (!response.ok) {
        const message = await parseError(response);
        const err = new Error(message);
        err.status = response.status;
        throw err;
    }

    return response.json();
}

/**
 * PATCH /api/incidents/{id}/assign  { companyId }
 */
export async function assignCompany(id, companyId) {
    const response = await fetch(`${API_BASE}/${id}/assign`, {
        method: 'PATCH',
        headers: jsonHeaders(),
        body: JSON.stringify({ companyId: Number(companyId) }),
    });

    if (!response.ok) {
        const message = await parseError(response);
        const err = new Error(message);
        err.status = response.status;
        throw err;
    }

    return response.json();
}

/**
 * PATCH /api/incidents/{id}/resolve  { comment }
 */
export async function resolveIncident(id, comment) {
    const response = await fetch(`${API_BASE}/${id}/resolve`, {
        method: 'PATCH',
        headers: jsonHeaders(),
        body: JSON.stringify({ comment }),
    });

    if (!response.ok) {
        const message = await parseError(response);
        const err = new Error(message);
        err.status = response.status;
        throw err;
    }

    return response.json();
}


/**
 * GET /api/incidents/{id}/status-history
 */
export async function getIncidentStatusHistory(id) {
    const response = await fetch(`${API_BASE}/${id}/status-history`, {
        headers: { ...getAuthHeader() },
    });

    if (!response.ok) {
        const message = await parseError(response);
        const err = new Error(message);
        err.status = response.status;
        throw err;
    }

    return response.json();
}
