import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Login.css';

async function parseAuthError(response) {
    let body = null;
    try {
        body = await response.json();
    } catch {
        body = null;
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

function Login() {
    const navigate = useNavigate();
    const [mode, setMode] = useState('signin');

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [isLoggingIn, setIsLoggingIn] = useState(false);

    const [registerForm, setRegisterForm] = useState({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        phone: '',
    });
    const [registerError, setRegisterError] = useState('');
    const [isRegistering, setIsRegistering] = useState(false);

    const [loginError, setLoginError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoginError('');
        setIsLoggingIn(true);
        try {
            const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password }),
            });

            if (!res.ok) {
                throw new Error(await parseAuthError(res));
            }

            const data = await res.json();

            localStorage.setItem('token', data.accessToken);
            localStorage.setItem('tokenType', data.tokenType);

            localStorage.setItem('user', JSON.stringify({
                userId: data.userId,
                fullName: data.fullName,
                email: data.email,
                role: data.role,
            }));

            navigate('/app/dashboard');

        } catch (err) {
            setLoginError(err.message);
        } finally {
            setIsLoggingIn(false);
        }
    };

    const handleRegisterChange = (e) => {
        setRegisterForm({ ...registerForm, [e.target.name]: e.target.value });
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        setRegisterError('');
        setIsRegistering(true);
        try {
            const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    fullName: `${registerForm.firstName} ${registerForm.lastName}`.trim(),
                    email: registerForm.email,
                    password: registerForm.password,
                }),
            });

            if (!res.ok) {
                throw new Error(await parseAuthError(res));
            }

            const data = await res.json();
            console.log('Registered:', data);
            setMode('signin');
        } catch (err) {
            setRegisterError(err.message);
        } finally {
            setIsRegistering(false);
        }
    };

    return (
        <div className="login-page">
            <aside className="login-hero">
                <div className="login-hero__brand">
                    <span className="login-hero__logo">🏙️</span>
                    <span>FixMyCity</span>
                </div>

                <h1>Smart City Incident Reporting</h1>
                <p>
                    Report public infrastructure issues and help municipal teams
                    resolve them faster — from potholes to broken streetlights.
                </p>

                <ul className="login-hero__list">
                    <li>Report incidents with photo and location</li>
                    <li>Track status from open to resolved</li>
                    <li>Role-based workflows for citizens, managers &amp; contractors</li>
                </ul>
            </aside>

            <section className="login-form-section">
                <div className="login-tabs">
                    <button
                        className={mode === 'signin' ? 'active' : ''}
                        onClick={() => setMode('signin')}
                    >
                        Sign in
                    </button>
                    <button
                        className={mode === 'signup' ? 'active' : ''}
                        onClick={() => setMode('signup')}
                    >
                        Create account
                    </button>
                </div>

                {mode === 'signin' ? (
                    <>
                        <h2>Welcome back</h2>
                        <p className="login-subtitle">Sign in to your FixMyCity account.</p>

                        <form onSubmit={handleSubmit} className="login-form">
                            <label>
                                Email
                                <input
                                    type="email"
                                    placeholder="citizen@demo.com"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    disabled={isLoggingIn}
                                />
                            </label>

                            <label>
                                Password
                                <input
                                    type="password"
                                    placeholder="••••"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    disabled={isLoggingIn}
                                />
                            </label>

                            {loginError && <p className="login-error">{loginError}</p>}

                            <button type="submit" className="login-submit" disabled={isLoggingIn}>
                                {isLoggingIn ? 'Signing in…' : 'Sign in'}
                            </button>
                        </form>

                    </>
                ) : (
                    <>
                        <h2>Create your account</h2>
                        <p className="login-subtitle">
                            Already have one?{' '}
                            <button type="button" className="login-link" onClick={() => setMode('signin')}>
                                Sign in
                            </button>
                        </p>

                        <form onSubmit={handleRegister} className="login-form">
                            <label>
                                First name
                                <input
                                    name="firstName"
                                    value={registerForm.firstName}
                                    onChange={handleRegisterChange}
                                    placeholder="Aisha"
                                    disabled={isRegistering}
                                />
                            </label>

                            <label>
                                Last name
                                <input
                                    name="lastName"
                                    value={registerForm.lastName}
                                    onChange={handleRegisterChange}
                                    placeholder="Patel"
                                    disabled={isRegistering}
                                />
                            </label>

                            <label>
                                Email
                                <input
                                    type="email"
                                    name="email"
                                    value={registerForm.email}
                                    onChange={handleRegisterChange}
                                    placeholder="you@example.com"
                                    disabled={isRegistering}
                                />
                            </label>

                            <label>
                                Password
                                <input
                                    type="password"
                                    name="password"
                                    value={registerForm.password}
                                    onChange={handleRegisterChange}
                                    placeholder="••••••••"
                                    disabled={isRegistering}
                                />
                            </label>

                            <label>
                                Phone
                                <input
                                    name="phone"
                                    value={registerForm.phone}
                                    onChange={handleRegisterChange}
                                    placeholder="+371 23456789"
                                    disabled={isRegistering}
                                />
                            </label>

                            {registerError && <p className="login-error">{registerError}</p>}

                            <button type="submit" className="login-submit" disabled={isRegistering}>
                                {isRegistering ? 'Creating account…' : 'Create account'}
                            </button>
                        </form>
                    </>
                )}
            </section>
        </div>
    );
}

export default Login;