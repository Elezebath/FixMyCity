/**
 * Auth helpers — reuse the same localStorage keys set by Login.jsx.
 */

export function getToken() {
    return localStorage.getItem('token');
}

export function getTokenType() {
    return localStorage.getItem('tokenType') || 'Bearer';
}

export function getAuthHeader() {
    const token = getToken();
    if (!token) return {};
    return { Authorization: `${getTokenType()} ${token}` };
}

export function getCurrentUser() {
    try {
        return JSON.parse(localStorage.getItem('user') || 'null');
    } catch {
        return null;
    }
}

export function getUserRole() {
    return getCurrentUser()?.role || '';
}

export function hasRole(...roles) {
    const role = getUserRole();
    return roles.includes(role);
}

export function isCitizen() {
    return hasRole('CITIZEN');
}

export function isManagerOrAdmin() {
    return hasRole('MANAGER', 'ADMIN');
}

/** Company-role users (backend resolve endpoint uses hasRole('COMPANY')). */
export function isCompany() {
    return hasRole('COMPANY');
}

export function clearAuth() {
    localStorage.removeItem('token');
    localStorage.removeItem('tokenType');
    localStorage.removeItem('user');
}
