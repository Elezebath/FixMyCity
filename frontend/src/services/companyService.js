import { getAuthHeader } from '../utils/auth';

const COMPANIES_URL = `${import.meta.env.VITE_API_BASE_URL}/api/companies`;

export async function getCompany(companyId) {
    const response = await fetch(`${COMPANIES_URL}/${companyId}`, {
        headers: {
            ...getAuthHeader()
        }
    });

    if (!response.ok) {
        if (response.status === 404) {
            throw new Error('Company not found.');
        }

        if (response.status === 401) {
            throw new Error('Please sign in.');
        }

        throw new Error('Failed to load company.');
    }

    return response.json();
}

export async function updateCompany(companyId, company) {
    const response = await fetch(`${COMPANIES_URL}/${companyId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            ...getAuthHeader()
        },
        body: JSON.stringify(company)
    });

    if (!response.ok) {
        const err = await response.json().catch(() => null);
        throw new Error(err?.error || err?.message || 'Failed to update company.');
    }

    return response.json();
}