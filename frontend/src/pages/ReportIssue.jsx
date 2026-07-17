import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createIncident } from '../services/incidentService';
import { getCategories } from '../services/categoryService';
import { hasRole } from '../utils/auth';
import {
    ALLOWED_ATTACHMENT_TYPES,
    validateAttachment,
} from '../utils/incidentHelpers';
import './ReportIssue.css';

function ReportIssue() {
    const navigate = useNavigate();
    const canCreate = hasRole('CITIZEN', 'MANAGER', 'ADMIN');

    const [form, setForm] = useState({
        title: '',
        categoryId: '',
        locationAddress: '',
        description: '',
    });
    const [attachment, setAttachment] = useState(null);
    const [categories, setCategories] = useState([]);
    const [categoriesError, setCategoriesError] = useState('');
    const [categoriesLoading, setCategoriesLoading] = useState(true);
    const [error, setError] = useState('');
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        let cancelled = false;

        async function loadCategories() {
            setCategoriesLoading(true);
            setCategoriesError('');
            try {
                const data = await getCategories();
                if (!cancelled) setCategories(Array.isArray(data) ? data : []);
            } catch (err) {
                if (!cancelled) {
                    setCategoriesError(err.message || 'Failed to load categories.');
                    // Fallback empty — user cannot pick a real categoryId without API
                    setCategories([]);
                }
            } finally {
                if (!cancelled) setCategoriesLoading(false);
            }
        }

        loadCategories();
        return () => {
            cancelled = true;
        };
    }, []);

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleAttachment = (e) => {
        const file = e.target.files?.[0];
        if (!file) {
            setAttachment(null);
            return;
        }
        const fileError = validateAttachment(file);
        if (fileError) {
            setError(fileError);
            e.target.value = '';
            setAttachment(null);
            return;
        }
        setError('');
        setAttachment(file);
    };

    const validateForm = () => {
        if (!form.title.trim()) return 'Title is required.';
        if (form.title.trim().length < 5) return 'Title must be at least 5 characters.';
        if (form.title.trim().length > 150) return 'Title must be at most 150 characters.';
        if (!form.categoryId) return 'Category is required.';
        if (!form.locationAddress.trim()) return 'Location is required.';
        if (!form.description.trim()) return 'Description is required.';
        if (form.description.trim().length < 10) {
            return 'Description must be at least 10 characters.';
        }
        if (form.description.trim().length > 1000) {
            return 'Description must be at most 1000 characters.';
        }
        if (attachment) {
            const fileError = validateAttachment(attachment);
            if (fileError) return fileError;
        }
        return null;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (!canCreate) {
            setError('You do not have permission to report incidents.');
            return;
        }

        const validationError = validateForm();
        if (validationError) {
            setError(validationError);
            return;
        }

        const body = new FormData();
        body.append('title', form.title.trim());
        body.append('description', form.description.trim());
        body.append('categoryId', form.categoryId);
        body.append('locationAddress', form.locationAddress.trim());
        if (attachment) {
            body.append('attachment', attachment);
        }

        setSubmitting(true);
        try {
            const created = await createIncident(body);
            const id = created.incidentId ?? created.id;
            if (id) {
                navigate(`/app/incidents/${id}`);
            } else {
                navigate('/app/incidents');
            }
        } catch (err) {
            if (err.status === 403) {
                setError('Access denied. You cannot create incidents with this account.');
            } else if (err.status === 404) {
                setError(
                    err.message ||
                        'Selected category was not found. Please refresh categories and try again.'
                );
                // Attempt refresh
                try {
                    const data = await getCategories();
                    setCategories(Array.isArray(data) ? data : []);
                } catch {
                    /* ignore */
                }
            } else {
                setError(err.message || 'Failed to submit report.');
            }
        } finally {
            setSubmitting(false);
        }
    };

    if (!canCreate) {
        return (
            <div className="report-page">
                <div className="report-page__header">
                    <h1>Report an Incident</h1>
                    <p className="report-page__subtitle report-error">
                        Your role is not permitted to create incidents.
                    </p>
                </div>
                <button
                    type="button"
                    className="report-btn report-btn--ghost"
                    onClick={() => navigate('/app/incidents')}
                >
                    Back to incidents
                </button>
            </div>
        );
    }

    return (
        <div className="report-page">
            <div className="report-page__header">
                <h1>Report an Incident</h1>
                <p className="report-page__subtitle">
                    Provide details so the right team can resolve it faster.
                </p>
            </div>

            <form className="report-card" onSubmit={handleSubmit} noValidate>
                <label className="report-field">
                    Title
                    <input
                        name="title"
                        value={form.title}
                        onChange={handleChange}
                        placeholder="e.g. Pothole on Main Street"
                        maxLength={150}
                        required
                        disabled={submitting}
                    />
                </label>

                <label className="report-field">
                    Category
                    <select
                        name="categoryId"
                        value={form.categoryId}
                        onChange={handleChange}
                        required
                        disabled={submitting || categoriesLoading}
                    >
                        <option value="">
                            {categoriesLoading
                                ? 'Loading categories…'
                                : 'Select a category...'}
                        </option>
                        {categories.map((c) => (
                            <option
                                key={c.categoryId ?? c.id}
                                value={c.categoryId ?? c.id}
                            >
                                {c.name}
                            </option>
                        ))}
                    </select>
                    {categoriesError && (
                        <span className="report-field__hint report-error">
                            {categoriesError}
                        </span>
                    )}
                </label>

                <label className="report-field">
                    Address / location description
                    <textarea
                        name="locationAddress"
                        value={form.locationAddress}
                        onChange={handleChange}
                        rows={3}
                        maxLength={255}
                        placeholder="Describe the location (street, cross-street, landmark)..."
                        required
                        disabled={submitting}
                    />
                    <span className="report-field__hint">
                        Free-text description — e.g. "Corner of Elm & Oak, near bus
                        stop".
                    </span>
                </label>

                <label className="report-field">
                    Description
                    <textarea
                        name="description"
                        value={form.description}
                        onChange={handleChange}
                        rows={4}
                        maxLength={1000}
                        placeholder="Describe the problem in detail..."
                        required
                        disabled={submitting}
                    />
                </label>

                <div className="report-field">
                    Attachment (optional)
                    <label className="report-upload">
                        <input
                            type="file"
                            accept={ALLOWED_ATTACHMENT_TYPES.join(',')}
                            onChange={handleAttachment}
                            hidden
                            disabled={submitting}
                        />
                        {attachment
                            ? attachment.name
                            : '📷 Click to attach an image or PDF (max 5 MB)'}
                    </label>
                    <span className="report-field__hint">
                        Allowed: JPEG, PNG, GIF, WebP, PDF · max 5 MB
                    </span>
                </div>

                {error && (
                    <p className="report-error" role="alert">
                        {error}
                    </p>
                )}

                <div className="report-actions">
                    <button
                        type="button"
                        className="report-btn report-btn--ghost"
                        onClick={() => navigate('/app/incidents')}
                        disabled={submitting}
                    >
                        Cancel
                    </button>

                    <button
                        type="submit"
                        className="report-btn report-btn--primary"
                        disabled={submitting || categoriesLoading}
                    >
                        {submitting ? 'Submitting…' : 'Submit report'}
                    </button>
                </div>
            </form>
        </div>
    );
}

export default ReportIssue;
