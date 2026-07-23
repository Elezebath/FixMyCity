import { useEffect, useState } from 'react';
import { normalizeIncident, formatDate } from '../../utils/incidentHelpers.js';
import './Assignment.css';

const API_BASE = import.meta.env.VITE_API_BASE_URL;

function getAuthHeaders() {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : '',
    };
}

export default function Assignment() {
    const [incidents, setIncidents] = useState([]);
    const [companies, setCompanies] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [selectedIncident, setSelectedIncident] = useState(null);
    const [companyId, setCompanyId] = useState('');
    const [dueDate, setDueDate] = useState('');
    const [assigning, setAssigning] = useState(false);
    const [assignError, setAssignError] = useState(null);

    useEffect(() => {
        loadData();
    }, []);

    async function loadData() {
        setLoading(true);
        setError(null);
        try {
            const [incidentsRes, companiesRes] = await Promise.all([
                fetch(`${API_BASE}/api/incidents/status/NEW`, { headers: getAuthHeaders() }),
                fetch(`${API_BASE}/api/companies`, { headers: getAuthHeaders() }),
            ]);

            if (!incidentsRes.ok) throw new Error('Failed to load incidents');
            if (!companiesRes.ok) throw new Error('Failed to load companies');

            const incidentsData = await incidentsRes.json();
            const companiesData = await companiesRes.json();
            const rawIncidents = incidentsData.content ?? incidentsData;

            setIncidents(rawIncidents.map(normalizeIncident));
            setCompanies(companiesData.content ?? companiesData);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }

    function openAssignModal(incident) {
        setSelectedIncident(incident);
        setCompanyId('');
        setDueDate('');
        setAssignError(null);
    }

    function closeAssignModal() {
        setSelectedIncident(null);
    }

    async function handleAssign(e) {
        e.preventDefault();
        if (!companyId) {
            setAssignError('Please select a company');
            return;
        }

        setAssigning(true);
        setAssignError(null);

        try {
            const res = await fetch(
                `${API_BASE}/api/incidents/${selectedIncident.id}/assign`,
                {
                    method: 'PATCH',
                    headers: getAuthHeaders(),
                    body: JSON.stringify({
                        companyId: Number(companyId),
                        dueDate: dueDate || null,
                    }),
                }
            );

            if (!res.ok) {
                const body = await res.json().catch(() => null);
                throw new Error(body?.message || 'Failed to assign incident');
            }

            setIncidents((prev) => prev.filter((i) => i.id !== selectedIncident.id));
            closeAssignModal();
        } catch (err) {
            setAssignError(err.message);
        } finally {
            setAssigning(false);
        }
    }

    if (loading) return <div className="assignment-page">Loading incidents...</div>;
    if (error) return <div className="assignment-page assignment-error">Error: {error}</div>;

    return (
        <div className="assignment-page">
            <h1>Unassigned Incidents</h1>

            {incidents.length === 0 ? (
                <p>No unassigned incidents.</p>
            ) : (
                <table className="assignment-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Category</th>
                        <th>Location</th>
                        <th>Created</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    {incidents.map((incident) => (
                        <tr key={incident.id}>
                            <td className="assignment-table__id">{incident.id}</td>
                            <td className="assignment-table__category">{incident.category}</td>
                            <td className="assignment-table__location">{incident.address}</td>
                            <td className="assignment-table__date">
                                {formatDate(incident.createdAt)}
                            </td>
                            <td>
                                <button className="assign-btn" onClick={() => openAssignModal(incident)}>
                                    Assign
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}

            {selectedIncident && (
                <div className="assignment-modal-overlay" onClick={closeAssignModal}>
                    <div className="assignment-modal" onClick={(e) => e.stopPropagation()}>
                        <h2>Assign Incident #{selectedIncident.id}</h2>

                        <form onSubmit={handleAssign}>
                            <label>
                                Company
                                <select
                                    value={companyId}
                                    onChange={(e) => setCompanyId(e.target.value)}
                                    required
                                >
                                    <option value="">Select a company</option>
                                    {companies.map((company) => (
                                        <option key={company.companyId} value={company.companyId}>
                                            {company.companyName}
                                        </option>
                                    ))}
                                </select>
                            </label>

                            <label>
                                Due date (optional)
                                <input
                                    type="date"
                                    value={dueDate}
                                    onChange={(e) => setDueDate(e.target.value)}
                                />
                            </label>

                            {assignError && <p className="assignment-error">{assignError}</p>}

                            <div className="assignment-modal-actions">
                                <button type="button" onClick={closeAssignModal} disabled={assigning}>
                                    Cancel
                                </button>
                                <button type="submit" disabled={assigning}>
                                    {assigning ? 'Assigning...' : 'Confirm Assign'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}