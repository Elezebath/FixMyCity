import {useState, useEffect} from 'react';
import './AdminUsers.css';
import { getCategories } from '../../services/categoryService';
import { getCompany, updateCompany } from '../../services/companyService';

const ROLES = ['CITIZEN', 'MANAGER', 'COMPANY', 'ADMIN'];

function AdminUsers() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingUser, setEditingUser] = useState(null); // user object or null

    const token = localStorage.getItem('token');
    const tokenType = localStorage.getItem('tokenType');
    const authHeader = {Authorization: `${tokenType} ${token}`};

    const fetchUsers = async () => {
        setLoading(true);
        setError('');
        try {
            const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/admin/users`, {
                headers: {...authHeader},
            });
            if (!res.ok) {
                const err = await res.json().catch(() => null);
                throw new Error(err?.message || 'Failed to load users');
            }
            const data = await res.json();
            setUsers(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void fetchUsers();
    }, []);

    const handleRoleChange = async (userId, newRole) => {
        try {
            const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/admin/users/${userId}/role`, {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json', ...authHeader},
                body: JSON.stringify({role: newRole}),
            });
            if (!res.ok) {
                const err = await res.json().catch(() => null);
                throw new Error(err?.message || 'Failed to update role');
            }
            await fetchUsers();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleStatusToggle = async (userId, currentlyEnabled) => {
        try {
            const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/admin/users/${userId}/status`, {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json', ...authHeader},
                body: JSON.stringify({enabled: !currentlyEnabled}),
            });
            if (!res.ok) {
                const err = await res.json().catch(() => null);
                throw new Error(err?.message || 'Failed to update status');
            }
            await fetchUsers();
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className="admin-users-page">
            <div className="admin-users-header">
                <h1>User Administration</h1>
                <button className="admin-btn-primary" onClick={() => setShowCreateModal(true)}>
                    <span>+</span> New user
                </button>
            </div>

            {error && <p className="admin-error">{error}</p>}
            {loading ? (
                <p>Loading...</p>
            ) : (
                <table className="admin-users-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Full name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Status</th>
                        <th>Created</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {users.map((u) => (
                        <tr key={u.id}>
                            <td>{u.id}</td>
                            <td>{u.fullName}</td>
                            <td>{u.email}</td>
                            <td>
                                <select
                                    className={`role-pill role-pill--${u.role.toLowerCase()}`}
                                    value={u.role}
                                    onChange={(e) => handleRoleChange(u.id, e.target.value)}
                                >
                                    {ROLES.map((r) => (
                                        <option key={r} value={r}>{r}</option>
                                    ))}
                                </select>
                            </td>
                            <td>
                                <span className={u.enabled ? 'status-badge status-badge--enabled' : 'status-badge status-badge--disabled'}>
                                    <span className="status-dot"/>
                                    {u.enabled ? 'Enabled' : 'Disabled'}
                                </span>
                            </td>
                            <td>{new Date(u.createdAt).toLocaleDateString()}</td>
                            <td className="admin-actions">
                                <button
                                    className={u.enabled ? 'action-btn action-btn--danger' : 'action-btn action-btn--success'}
                                    onClick={() => handleStatusToggle(u.id, u.enabled)}
                                >
                                    {u.enabled ? '⛔ Disable' : '✓ Enable'}
                                </button>
                                <button className="action-btn action-btn--neutral" onClick={() => setEditingUser(u)}>
                                    ✎ Edit
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}

            {showCreateModal && (
                <CreateUserModal
                    onClose={() => setShowCreateModal(false)}
                    onCreated={() => {
                        setShowCreateModal(false);
                        fetchUsers();
                    }}
                    authHeader={authHeader}
                />
            )}

            {editingUser && (
                <EditUserModal
                    user={editingUser}
                    onClose={() => setEditingUser(null)}
                    onSaved={() => {
                        setEditingUser(null);
                        fetchUsers();
                    }}
                    authHeader={authHeader}
                />
            )}
        </div>
    );
}

function CreateUserModal({onClose, onCreated, authHeader}) {
    const [form, setForm] = useState({
        fullName: '',
        email: '',
        password: '',
        role: 'CITIZEN',
        company: {
            companyName: '',
            registrationNo: '',
            categoryId: '',
            contactEmail: '',
            contactPhone: '',
            address: ''
        }
    });
    const [error, setError] = useState('');

    const [categories, setCategories] = useState([]);

    useEffect(() => {
        if (form.role !== 'COMPANY') {
            return;
        }

        const loadCategories = async () => {
            try {
                const data = await getCategories();
                setCategories(data);
            } catch (err) {
                setError(err.message);
            }
        };

        void loadCategories();
    }, [form.role]);

    const handleChange = (e) => setForm({...form, [e.target.name]: e.target.value});

    const handleCompanyChange = (e) => {
        setForm({
            ...form,
            company: {
                ...form.company,
                [e.target.name]: e.target.value
            }
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            const payload = {
                ...form,
                company:
                    form.role === 'COMPANY'
                        ? {
                            ...form.company,
                            categoryId: Number(form.company.categoryId)
                        }
                        : null
            };

            const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/admin/users`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...authHeader
                },
                body: JSON.stringify(payload),
            });
            if (!res.ok) {
                const err = await res.json().catch(() => null);
                throw new Error(err?.message || 'Failed to create user');
            }
            onCreated();
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className="admin-modal-overlay">
            <div className="admin-modal">
                <h2>Create user</h2>
                <form onSubmit={handleSubmit}>
                    <label>Full name
                        <input name="fullName" value={form.fullName} onChange={handleChange} required/>
                    </label>
                    <label>Email
                        <input type="email" name="email" value={form.email} onChange={handleChange} required/>
                    </label>
                    <label>Password
                        <input type="password" name="password" value={form.password} onChange={handleChange} required/>
                    </label>
                    <label>Role
                        <select name="role" value={form.role} onChange={handleChange}>
                            {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
                        </select>
                    </label>
                    {form.role === 'COMPANY' && (
                        <>
                            <label>Company name
                                <input
                                    name="companyName"
                                    value={form.company.companyName}
                                    onChange={handleCompanyChange}
                                    required
                                />
                            </label>

                            <label>Registration number
                                <input
                                    name="registrationNo"
                                    value={form.company.registrationNo}
                                    onChange={handleCompanyChange}
                                    required
                                />
                            </label>

                            <label>
                                Category
                                <select
                                    name="categoryId"
                                    value={form.company.categoryId}
                                    onChange={handleCompanyChange}
                                    required
                                >
                                    <option value="">Select category</option>

                                    {categories.map(category => (
                                        <option
                                            key={category.categoryId}
                                            value={category.categoryId}
                                        >
                                            {category.name}
                                        </option>
                                    ))}
                                </select>
                            </label>

                            <label>Contact email
                                <input
                                    type="email"
                                    name="contactEmail"
                                    value={form.company.contactEmail}
                                    onChange={handleCompanyChange}
                                    required
                                />
                            </label>

                            <label>Contact phone
                                <input
                                    name="contactPhone"
                                    value={form.company.contactPhone}
                                    onChange={handleCompanyChange}
                                />
                            </label>

                            <label>Address
                                <input
                                    name="address"
                                    value={form.company.address}
                                    onChange={handleCompanyChange}
                                />
                            </label>
                        </>
                    )}
                    {error && <p className="admin-error">{error}</p>}
                    <div className="admin-modal-actions">
                        <button type="button" onClick={onClose}>Cancel</button>
                        <button type="submit" className="admin-btn-primary">Create</button>
                    </div>
                </form>
            </div>
        </div>
    );
}

function EditUserModal({user, onClose, onSaved, authHeader}) {
    const [form, setForm] = useState({
        fullName: user.fullName,
        email: user.email,
        company: {
            companyName: '',
            registrationNo: '',
            categoryId: '',
            contactEmail: '',
            contactPhone: '',
            address: ''
        }
    });

    const [categories, setCategories] = useState([]);

    useEffect(() => {
        const loadData = async () => {
            try {
                const categoryData = await getCategories();
                setCategories(categoryData);

                if (user.role === 'COMPANY' && user.companyId) {
                    const company = await getCompany(user.companyId);

                    setForm(prev => ({
                        ...prev,
                        company: {
                            companyName: company.companyName,
                            registrationNo: company.registrationNo,
                            categoryId: company.categoryId,
                            contactEmail: company.contactEmail,
                            contactPhone: company.contactPhone ?? '',
                            address: company.address ?? ''
                        }
                    }));
                }
            } catch (err) {
                setError(err.message);
            }
        };

        void loadData();
    }, [user]);

    const handleCompanyChange = (e) => {
        setForm({
            ...form,
            company: {
                ...form.company,
                [e.target.name]: e.target.value
            }
        });
    };
    const [error, setError] = useState('');

    const handleChange = (e) => setForm({...form, [e.target.name]: e.target.value});

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/admin/users/${user.id}/profile`, {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json', ...authHeader},
                body: JSON.stringify(form),
            });
            if (!res.ok) {
                const err = await res.json().catch(() => null);
                throw new Error(err?.message || 'Failed to update profile');
            }

            if (user.role === 'COMPANY' && user.companyId) {
                await updateCompany(user.companyId, {
                    companyName: form.company.companyName,
                    registrationNo: form.company.registrationNo,
                    categoryId: Number(form.company.categoryId),
                    contactEmail: form.company.contactEmail,
                    contactPhone: form.company.contactPhone,
                    address: form.company.address
                });
            }
            onSaved();
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className="admin-modal-overlay">
            <div className="admin-modal">
                <h2>Edit user #{user.id}</h2>
                <form onSubmit={handleSubmit}>
                    <label>Full name
                        <input name="fullName" value={form.fullName} onChange={handleChange} required/>
                    </label>
                    <label>Email
                        <input
                            type="email"
                            name="email"
                            value={form.email}
                            onChange={handleChange}
                            required
                        />
                    </label>

                    {user.role === 'COMPANY' && (
                        <>
                            <label>
                                Company name
                                <input
                                    name="companyName"
                                    value={form.company.companyName}
                                    onChange={handleCompanyChange}
                                    required
                                />
                            </label>

                            <label>
                                Registration number
                                <input
                                    name="registrationNo"
                                    value={form.company.registrationNo}
                                    onChange={handleCompanyChange}
                                    required
                                />
                            </label>

                            <label>
                                Category
                                <select
                                    name="categoryId"
                                    value={form.company.categoryId}
                                    onChange={handleCompanyChange}
                                    required
                                >
                                    <option value="">Select category</option>

                                    {categories.map(category => (
                                        <option
                                            key={category.categoryId}
                                            value={category.categoryId}
                                        >
                                            {category.name}
                                        </option>
                                    ))}
                                </select>
                            </label>

                            <label>
                                Contact email
                                <input
                                    type="email"
                                    name="contactEmail"
                                    value={form.company.contactEmail}
                                    onChange={handleCompanyChange}
                                    required
                                />
                            </label>

                            <label>
                                Contact phone
                                <input
                                    name="contactPhone"
                                    value={form.company.contactPhone}
                                    onChange={handleCompanyChange}
                                />
                            </label>

                            <label>
                                Address
                                <input
                                    name="address"
                                    value={form.company.address}
                                    onChange={handleCompanyChange}
                                />
                            </label>
                        </>
                    )}

                    {error && <p className="admin-error">{error}</p>}
                    <div className="admin-modal-actions">
                        <button type="button" onClick={onClose}>Cancel</button>
                        <button type="submit" className="admin-btn-primary">Save</button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default AdminUsers;