import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './ReportIssue.css';

// Todo Get /api/categories
const CATEGORIES = [
    'Public Safety',
    'Road & Potholes',
    'Waste & Sanitation',
    'Street Lighting',
    'Water & Drainage',
    'Other',
];

function ReportIssue() {
    const navigate = useNavigate();

    const [form, setForm] = useState({
        title: '',
        category: '',
        address: '',
        description: '',
    });
    const [photo, setPhoto] = useState(null);
    const [error, setError] = useState('');

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handlePhoto = (e) => {
        const file = e.target.files?.[0];
        if (file) setPhoto(file);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (!form.title.trim() || !form.category) {
            setError('Title and category are required.');
            return;
        }

        // Todo POST /api/incidents
        console.log('New incident:', { ...form, photo });

        /*
        try {
            const body = new FormData();
            body.append('title', form.title);
            body.append('category', form.category);
            body.append('address', form.address);
            body.append('description', form.description);
            if (photo) body.append('photo', photo);

            const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/incidents`, {
                method: 'POST',
                headers: {
                    Authorization: `${localStorage.getItem('tokenType')} ${localStorage.getItem('token')}`,
                },
                body,
            });
            if (!res.ok) throw new Error('Failed to submit report');
        } catch (err) {
            setError(err.message);
            return;
        }
        */

        navigate('/app/incidents');
    };

    return (
        <div className="report-page">
            <div className="report-page__header">
                <h1>Report an Incident</h1>
                <p className="report-page__subtitle">
                    Provide details so the right team can resolve it faster.
                </p>
            </div>

            <form className="report-card" onSubmit={handleSubmit}>
                <label className="report-field">
                    Title
                    <input
                        name="title"
                        value={form.title}
                        onChange={handleChange}
                        placeholder="e.g. Pothole on Main Street"
                    />
                </label>

                <label className="report-field">
                    Category
                    <select name="category" value={form.category} onChange={handleChange}>
                        <option value="">Select a category...</option>
                        {CATEGORIES.map((c) => (
                            <option key={c} value={c}>
                                {c}
                            </option>
                        ))}
                    </select>
                </label>

                <label className="report-field">
                    Address / location description
                    <textarea
                        name="address"
                        value={form.address}
                        onChange={handleChange}
                        rows={3}
                        placeholder="Describe the location (street, cross-street, landmark)..."
                    />
                    <span className="report-field__hint">
                        Free-text description — e.g. "Corner of Elm &amp; Oak, near bus stop".
                    </span>
                </label>

                <label className="report-field">
                    Description
                    <textarea
                        name="description"
                        value={form.description}
                        onChange={handleChange}
                        rows={4}
                        placeholder="Describe the problem in detail..."
                    />
                </label>

                <div className="report-field">
                    Photo (optional)
                    <label className="report-upload">
                        <input type="file" accept="image/*" onChange={handlePhoto} hidden />
                        {photo ? photo.name : '📷 Click or drag an image here'}
                    </label>
                </div>

                {error && <p className="report-error">{error}</p>}

                <div className="report-actions">
                    <button
                        type="button"
                        className="report-btn report-btn--ghost"
                        onClick={() => navigate('/app/incidents')}
                    >
                        Cancel
                    </button>
                    <button type="submit" className="report-btn report-btn--primary">
                        Submit report
                    </button>
                </div>
            </form>
        </div>
    );
}

export default ReportIssue;