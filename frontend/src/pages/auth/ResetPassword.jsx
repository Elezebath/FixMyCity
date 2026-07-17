import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import './ResetPassword.css';

function ResetPassword() {

    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const token = searchParams.get('token');

    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();

        setError('');
        setSuccess('');

        if (!token) {
            setError('Missing password reset token.');
            return;
        }

        if (newPassword.length < 8) {
            setError('Password must contain at least 8 characters.');
            return;
        }

        if (newPassword !== confirmPassword) {
            setError('Passwords do not match.');
            return;
        }

        setLoading(true);

        try {
            const res = await fetch(
                `${import.meta.env.VITE_API_BASE_URL}/api/auth/reset-password`,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        token,
                        newPassword,
                    }),
                }
            );

            const data = await res.json();

            if (!res.ok) {
                throw new Error(data.error || 'Unable to reset password.');
            }

            setSuccess(data.message);
            setNewPassword('');
            setConfirmPassword('');

            setTimeout(() => {
                navigate('/');
            }, 2500);

        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    if (!token) {
        return (
            <div className="password-page">
                <div className="password-card">
                    <h2>Invalid Password Reset Link</h2>

                    <p className="password-error">
                        This password reset link is invalid or missing.
                    </p>

                    <Link to="/" className="back-link">
                        ← Back to Sign in
                    </Link>
                </div>
            </div>
        );
    }
        return (
            <div className="password-page">
                <div className="password-card">

                    <h2>Reset Password</h2>

                    <p className="password-subtitle">
                        Enter your new password below.
                    </p>

                    <form onSubmit={handleSubmit} className="password-form">

                        <label>
                            New Password
                            <input
                                type="password"
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                                placeholder="••••••••"
                                required
                                disabled={loading || success !== ''}
                            />
                        </label>

                        <label>
                            Confirm Password
                            <input
                                type="password"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                placeholder="••••••••"
                                required
                                disabled={loading || success !== ''}
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
                            className="password-submit"
                            disabled={loading || success !== ''}
                        >
                            {loading ? 'Resetting...' : 'Reset Password'}
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

export default ResetPassword;