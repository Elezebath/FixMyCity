import { getAuthHeader } from '../utils/auth';

/**
 * Citizen-accessible category list (GET /api/categories).
 * Falls back to admin endpoint only if the public one is unavailable.
 */
const PUBLIC_CATEGORIES = `${import.meta.env.VITE_API_BASE_URL}/api/categories`;
const ADMIN_CATEGORIES = `${import.meta.env.VITE_API_BASE_URL}/api/admin/categories`;

export async function getCategories() {
    // Prefer public/authenticated list for all roles
    let response = await fetch(PUBLIC_CATEGORIES, {
        headers: { ...getAuthHeader() },
    });

    // Fallback for older backends that only expose admin categories
    if (response.status === 404) {
        response = await fetch(ADMIN_CATEGORIES, {
            headers: { ...getAuthHeader() },
        });
    }

    if (!response.ok) {
        if (response.status === 403) {
            throw new Error(
                'Unable to load categories for your role. Ask an admin to ensure GET /api/categories is available.'
            );
        }
        if (response.status === 401) {
            throw new Error('Please sign in to load categories.');
        }
        throw new Error('Failed to load categories.');
    }

    return response.json();
}
