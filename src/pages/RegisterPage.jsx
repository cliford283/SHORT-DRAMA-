import React, { useState } from 'react';
import { Play, Lock, Mail, User, ArrowRight, AlertCircle, Shield } from 'lucide-react';
import { registerWithEmail } from '../firebase/auth';

export default function RegisterPage({ onSwitchToLogin, onRegisterSuccess }) {
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!displayName || !email || !password) {
      setError('Please fill in all fields.');
      return;
    }
    if (password.length < 6) {
      setError('Password should be at least 6 characters.');
      return;
    }

    setLoading(true);
    setError('');

    const res = await registerWithEmail(displayName, email, password);
    setLoading(false);
    if (res.error) {
      setError(res.error);
    } else {
      onRegisterSuccess(res.user);
    }
  };

  return (
    <div className="min-h-screen bg-[#0a0a0c] flex flex-col justify-center items-center px-4 py-8 relative overflow-hidden">
      
      {/* Ambient Lighting Gradients */}
      <div className="absolute top-1/3 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-red-600/15 rounded-full blur-3xl pointer-events-none" />

      {/* Card */}
      <div className="relative z-10 w-full max-w-md bg-[#141419]/90 backdrop-blur-xl border border-white/10 rounded-2xl p-6 sm:p-8 shadow-2xl space-y-6">
        
        {/* Brand Header */}
        <div className="text-center space-y-2">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-gradient-to-tr from-red-700 to-red-500 shadow-xl shadow-red-600/40 mb-2">
            <Play className="w-6 h-6 text-white fill-white translate-x-0.5" />
          </div>
          <h1 className="font-display font-black text-2xl text-white tracking-wider">
            JOIN SHORT DRAMA
          </h1>
          <p className="text-xs text-zinc-400">
            Create an account to unlock unlimited HD short drama streaming
          </p>
        </div>

        {/* Error Notification */}
        {error && (
          <div className="p-3 bg-red-950/50 border border-red-500/50 rounded-lg flex items-center gap-2.5 text-xs text-red-300 animate-fade-in">
            <AlertCircle className="w-4 h-4 shrink-0 text-red-400" />
            <span>{error}</span>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-zinc-300 mb-1.5">
              Full Name / Nickname
            </label>
            <div className="relative">
              <User className="w-4 h-4 text-zinc-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                placeholder="Elena Watson"
                required
                className="w-full pl-10 pr-4 py-2.5 bg-zinc-900 border border-white/10 rounded-lg text-sm text-white placeholder-zinc-500 focus:outline-none focus:border-red-500 focus:ring-1 focus:ring-red-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-zinc-300 mb-1.5">
              Email Address
            </label>
            <div className="relative">
              <Mail className="w-4 h-4 text-zinc-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@example.com"
                required
                className="w-full pl-10 pr-4 py-2.5 bg-zinc-900 border border-white/10 rounded-lg text-sm text-white placeholder-zinc-500 focus:outline-none focus:border-red-500 focus:ring-1 focus:ring-red-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-zinc-300 mb-1.5">
              Password (min 6 characters)
            </label>
            <div className="relative">
              <Lock className="w-4 h-4 text-zinc-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
                className="w-full pl-10 pr-4 py-2.5 bg-zinc-900 border border-white/10 rounded-lg text-sm text-white placeholder-zinc-500 focus:outline-none focus:border-red-500 focus:ring-1 focus:ring-red-500"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full flex items-center justify-center gap-2 py-3 bg-red-600 hover:bg-red-700 active:scale-98 text-white rounded-lg text-sm font-bold shadow-lg shadow-red-600/40 transition-all disabled:opacity-50"
          >
            {loading ? (
              <span className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
            ) : (
              <>
                <span>Complete Registration</span>
                <ArrowRight className="w-4 h-4" />
              </>
            )}
          </button>
        </form>

        {/* Switch to Login */}
        <div className="text-center pt-2 border-t border-white/5">
          <p className="text-xs text-zinc-400">
            Already registered?{' '}
            <button
              type="button"
              onClick={onSwitchToLogin}
              className="text-red-400 font-bold hover:underline"
            >
              Sign In Here
            </button>
          </p>
        </div>

      </div>
    </div>
  );
}
