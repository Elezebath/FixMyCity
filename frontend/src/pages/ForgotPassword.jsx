import { useState } from 'react';
import { Link } from 'react-router-dom';
import './ForgotPassword.css';

function ForgotPassword() {
    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();

        setLoading(true);
        setError('');
        setSuccess('');

        try {
            const res = await fetch(
                `${import.meta.env.VITE_API_BASE_URL}/api/auth/forgot-password`,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ email }),
                }
            );

            const data = await res.json();

            if (!res.ok) {
                throw new Error(data.error || 'Unable to send reset email.');
            }

            setSuccess(data.message);
            setEmail('');
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="password-page">
            <div className="password-card">

                <h2>Forgot Password</h2>

                <p className="password-subtitle">
                    Enter your email address and we'll send you a password reset link.
                </p>

                <form onSubmit={handleSubmit} className="password-form">

                    <label>
                        Email

                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="you@example.com"
                            required
                            disabled={loading || success}
                        />
                    </label>

                    {error && (
                        <p className="password-error">
                            {error}
                        </p>
                    )}

                    {success && (
                        <p className="password-success">
                            {success}
                        </p>
                    )}

                    <button
                        type="submit"
                        disabled={loading || success}
                        className="password-submit"
                    >
                        {loading ? 'Sending...' : 'Send Reset Link'}
                    </button>

                </form>

                <Link
                    to="/"
                    className="back-link"
                >
                    ← Back to Sign in
                </Link>

            </div>
        </div>
    );
}

export default ForgotPassword;