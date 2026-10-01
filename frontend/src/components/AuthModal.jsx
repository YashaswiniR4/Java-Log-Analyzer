import React, { useState } from 'react';

export default function AuthModal({ isOpen, onClose, onAuthSuccess, initialMode = 'login' }) {
  const [mode, setMode] = useState(initialMode); // 'login', 'register', 'otp', 'forgot', 'reset'
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Form State
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    username: '',
    phone: '',
    password: '',
    confirmPassword: '',
    otpCode: '',
    resetToken: '',
    newPassword: '',
  });

  if (!isOpen) return null;

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setErrorMessage('');
    setSuccessMessage('');
  };

  const calculatePasswordStrength = (pass) => {
    if (!pass) return { score: 0, label: 'Empty', color: '#4b5563' };
    let score = 0;
    if (pass.length >= 6) score += 1;
    if (pass.length >= 10) score += 1;
    if (/[A-Z]/.test(pass)) score += 1;
    if (/[0-9]/.test(pass)) score += 1;
    if (/[^A-Za-z0-9]/.test(pass)) score += 1;

    if (score <= 2) return { score, label: 'Weak ⚠️', color: '#ef4444' };
    if (score <= 4) return { score, label: 'Medium ⚡', color: '#f59e0b' };
    return { score, label: 'Strong 💪', color: '#10b981' };
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) {
      setErrorMessage('Password and Confirm Password do not match');
      return;
    }
    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      const res = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: formData.name,
          email: formData.email,
          username: formData.username,
          phone: formData.phone,
          password: formData.password,
          confirmPassword: formData.confirmPassword,
        }),
      });
      const data = await res.json();
      setLoading(false);

      if (data.success) {
        if (data.otpCode) {
          setFormData(prev => ({ ...prev, otpCode: data.otpCode }));
        }
        setSuccessMessage(data.message || 'Registration successful! Verification OTP generated.');
        setMode('otp');
      } else {
        setErrorMessage(data.message || 'Registration failed');
      }
    } catch (err) {
      setLoading(false);
      setErrorMessage('Network error during registration: ' + err.message);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      const res = await fetch('/api/auth/verify-otp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: formData.email,
          otpCode: formData.otpCode || '123456',
        }),
      });
      const data = await res.json();
      setLoading(false);

      if (data.success) {
        setSuccessMessage('Account activated successfully! Please log in.');
        setTimeout(() => {
          setMode('login');
        }, 1200);
      } else {
        setErrorMessage(data.message || 'Invalid OTP code');
      }
    } catch (err) {
      setLoading(false);
      setErrorMessage('Verification failed: ' + err.message);
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          identifier: formData.email || formData.username,
          password: formData.password,
        }),
      });
      const data = await res.json();
      setLoading(false);

      if (data.success && data.token) {
        setSuccessMessage('Login successful! Redirecting to Dashboard...');
        if (onAuthSuccess) {
          onAuthSuccess(data.user, data.token);
        }
      } else {
        setErrorMessage(data.message || 'Invalid credentials');
      }
    } catch (err) {
      setLoading(false);
      setErrorMessage('Login failed: ' + err.message);
    }
  };

  const handleForgotPassword = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      const res = await fetch('/api/auth/forgot-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: formData.email }),
      });
      const data = await res.json();
      setLoading(false);

      if (data.resetToken) {
        setFormData(prev => ({ ...prev, resetToken: data.resetToken }));
        setSuccessMessage(data.message + ' (Demo Reset Token: ' + data.resetToken + ')');
      } else {
        setSuccessMessage(data.message || 'If an account exists for this email, you will receive a password reset link.');
      }
    } catch (err) {
      setLoading(false);
      setErrorMessage('Error requesting password reset');
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    if (formData.newPassword !== formData.confirmPassword) {
      setErrorMessage('New passwords do not match');
      return;
    }
    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      const res = await fetch('/api/auth/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          token: formData.resetToken,
          newPassword: formData.newPassword,
          confirmPassword: formData.confirmPassword,
        }),
      });
      const data = await res.json();
      setLoading(false);

      if (data.success) {
        setSuccessMessage('Password reset successfully! Please log in.');
        setTimeout(() => setMode('login'), 1500);
      } else {
        setErrorMessage(data.message || 'Password reset failed');
      }
    } catch (err) {
      setLoading(false);
      setErrorMessage('Reset error: ' + err.message);
    }
  };

  const strength = calculatePasswordStrength(formData.password);

  return (
    <div className="auth-overlay">
      <div className="auth-card glassmorphism">
        {/* Hide close button if user is not logged in */}
        {onClose && (
          <button className="auth-close-btn" onClick={onClose} title="Close Modal">
            ✕
          </button>
        )}

        {/* Auth Header Logo */}
        <div className="auth-header">
          <div className="auth-logo">⚡ LogAnalyzer <span style={{ fontSize: '0.8rem', background: '#3b82f6', color: '#fff', padding: '2px 8px', borderRadius: '4px', marginLeft: '4px' }}>PRO</span></div>
          <p className="auth-subtitle">Enterprise Log Observability & Authentication System</p>
        </div>

        {/* Tab Navigation */}
        <div className="auth-tabs">
          <button
            className={`auth-tab ${mode === 'login' ? 'active' : ''}`}
            onClick={() => { setMode('login'); setErrorMessage(''); setSuccessMessage(''); }}
          >
            🔐 Sign In
          </button>
          <button
            className={`auth-tab ${mode === 'register' ? 'active' : ''}`}
            onClick={() => { setMode('register'); setErrorMessage(''); setSuccessMessage(''); }}
          >
            ✍️ Create Account
          </button>
        </div>

        {/* Alert Banners */}
        {errorMessage && <div className="auth-alert error">❌ {errorMessage}</div>}
        {successMessage && <div className="auth-alert success">✅ {successMessage}</div>}

        {/* LOGIN FORM */}
        {mode === 'login' && (
          <form className="auth-form" onSubmit={handleLogin}>
            <div className="form-group">
              <label>Email or Username</label>
              <input
                type="text"
                name="email"
                placeholder="user@company.com or username"
                value={formData.email}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <div className="label-row">
                <label>Password</label>
                <button
                  type="button"
                  className="link-btn"
                  onClick={() => { setMode('forgot'); setErrorMessage(''); setSuccessMessage(''); }}
                >
                  Forgot Password?
                </button>
              </div>
              <input
                type="password"
                name="password"
                placeholder="••••••••••••"
                value={formData.password}
                onChange={handleChange}
                required
              />
            </div>

            <button type="submit" className="auth-submit-btn" disabled={loading}>
              {loading ? 'Authenticating...' : 'Sign In ➔'}
            </button>
          </form>
        )}

        {/* REGISTER FORM */}
        {mode === 'register' && (
          <form className="auth-form" onSubmit={handleRegister}>
            <div className="form-row">
              <div className="form-group">
                <label>Full Name</label>
                <input
                  type="text"
                  name="name"
                  placeholder="Yashaswini R"
                  value={formData.name}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="form-group">
                <label>Username</label>
                <input
                  type="text"
                  name="username"
                  placeholder="yashu"
                  value={formData.username}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Email Address</label>
                <input
                  type="email"
                  name="email"
                  placeholder="user@company.com"
                  value={formData.email}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="form-group">
                <label>Phone Number</label>
                <input
                  type="tel"
                  name="phone"
                  placeholder="+1 (555) 019-2834"
                  value={formData.phone}
                  onChange={handleChange}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Password</label>
                <input
                  type="password"
                  name="password"
                  placeholder="BCrypt hashed ($2a$12$...)"
                  value={formData.password}
                  onChange={handleChange}
                  required
                />
                {formData.password && (
                  <div className="strength-meter">
                    <div className="strength-bar" style={{ width: `${(strength.score / 5) * 100}%`, backgroundColor: strength.color }}></div>
                    <span style={{ color: strength.color }}>{strength.label}</span>
                  </div>
                )}
              </div>
              <div className="form-group">
                <label>Confirm Password</label>
                <input
                  type="password"
                  name="confirmPassword"
                  placeholder="Re-enter password"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="security-note">
              🔒 <strong>Security Policy:</strong> Passwords are never stored in plain text. Always hashed using <strong>BCrypt $2a$12$...</strong> algorithm.
            </div>

            <button type="submit" className="auth-submit-btn" disabled={loading}>
              {loading ? 'Creating Account & Hashing...' : 'Register & Send OTP ✉️'}
            </button>
          </form>
        )}

        {/* OTP EMAIL VERIFICATION FORM */}
        {mode === 'otp' && (
          <form className="auth-form" onSubmit={handleVerifyOtp}>
            <div className="otp-info">
              An Email Verification Code (OTP) was generated for <strong>{formData.email}</strong>.
              <div style={{ marginTop: '6px', background: 'rgba(59, 130, 246, 0.15)', padding: '6px 10px', borderRadius: '6px', border: '1px solid rgba(59, 130, 246, 0.3)', color: '#93c5fd' }}>
                🔑 <strong>Verification Code:</strong> <span style={{ fontFamily: 'monospace', fontSize: '1rem', color: '#60a5fa', fontWeight: 700 }}>{formData.otpCode || '123456'}</span> (or enter <strong>123456</strong>)
              </div>
            </div>
            <div className="form-group">
              <label>6-Digit Verification OTP Code</label>
              <input
                type="text"
                name="otpCode"
                placeholder="e.g. 123456"
                value={formData.otpCode || '123456'}
                onChange={handleChange}
                maxLength="6"
                required
                style={{ fontSize: '1.25rem', letterSpacing: '4px', textAlign: 'center' }}
              />
            </div>
            <button type="submit" className="auth-submit-btn" disabled={loading}>
              {loading ? 'Verifying Code...' : 'Activate Account & Sign In'}
            </button>
          </form>
        )}

        {/* FORGOT PASSWORD FORM */}
        {mode === 'forgot' && (
          <form className="auth-form" onSubmit={handleForgotPassword}>
            <div className="otp-info">
              Enter your registered email address below. You will receive a password reset link valid for 30 minutes.
            </div>
            <div className="form-group">
              <label>Registered Email</label>
              <input
                type="email"
                name="email"
                placeholder="user@company.com"
                value={formData.email}
                onChange={handleChange}
                required
              />
            </div>
            <button type="submit" className="auth-submit-btn" disabled={loading}>
              {loading ? 'Sending Request...' : 'Send Password Reset Link ✉️'}
            </button>
            <div className="label-row" style={{ marginTop: '12px' }}>
              <button
                type="button"
                className="link-btn"
                onClick={() => setMode('reset')}
              >
                Already have a reset token link?
              </button>
            </div>
          </form>
        )}

        {/* RESET PASSWORD FORM */}
        {mode === 'reset' && (
          <form className="auth-form" onSubmit={handleResetPassword}>
            <div className="form-group">
              <label>Password Reset Token</label>
              <input
                type="text"
                name="resetToken"
                placeholder="Enter reset token from email link"
                value={formData.resetToken}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>New Password</label>
                <input
                  type="password"
                  name="newPassword"
                  placeholder="New password"
                  value={formData.newPassword}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="form-group">
                <label>Confirm New Password</label>
                <input
                  type="password"
                  name="confirmPassword"
                  placeholder="Re-enter new password"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>
            <button type="submit" className="auth-submit-btn" disabled={loading}>
              {loading ? 'Updating Password...' : 'Reset Password & Update Database'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
