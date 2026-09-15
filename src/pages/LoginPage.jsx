import React, { useState } from 'react';
import { Play, Lock, Mail, ArrowRight, Shield, AlertCircle, Sparkles } from 'lucide-react';
import { loginWithEmail } from '../firebase/auth';

export default function LoginPage({ onSwitchToRegister, onLoginSuccess }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email || !password) {
      setError('Please enter both email and password.');
      return;
    }
    setLoading(true);
    setError('');

    const res = await loginWithEmail(email, password);
    setLoading(false);
    if (res.error) {
      setError(res.error);
    } else {
      onLoginSuccess(res.user);
    }
  };

  const handleQuickLogin = (demoEmail, demoPass) => {
    setEmail(demoEmail);
    setPassword(demoPass);
  };

  return (
    <div className="min-h-screen bg-[#0a0a0c] flex flex-col justify-center items-center px-4 py-8 relative overflow-hidden">
      
      {/* Ambient Lighting Gradients */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-red-600/15 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-10 right-10 w-80 h-80 bg-amber-500/10 rounded-full blur-3xl pointer-events-none" />

      {/* Card */}
      <div className="relative z-10 w-full max-w-md bg-[#141419]/90 backdrop-blur-xl border border-white/10 rounded-2xl p-6 sm:p-8 shadow-2xl space-y-6">
        
        {/* Brand Header */}
        <div className="text-center space-y-2">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-gradient-to-tr from-red-700 to-red-500 shadow-xl shadow-red-600/40 mb-2">
            <Play className="w-6 h-6 text-white fill-white translate-x-0.5" />
          </div>
          <h1 className="font-display font-black text-2xl text-white tracking-wider">
            SHORT DRAMA <span className="text-red-500">VIP</span>
          </h1>
          <p className="text-xs text-zinc-400">
            Sign in to stream viral short drama series & resume your episodes
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
              Password
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
                <span>Sign In to Stream</span>
                <ArrowRight className="w-4 h-4" />
              </>
            )}
          </button>
        </form>

        {/* Quick Testing Accounts */}
        <div className="pt-2 border-t border-white/5 space-y-2">
          <p className="text-[11px] text-zinc-400 font-semibold text-center">
            One-Click Login (Authorized Profiles)
          </p>
          <div className="grid grid-cols-2 gap-2">
            <button
              type="button"
              onClick={() => handleQuickLogin('clifordmulumba@gmail.com', 'AdminPass123!')}
              className="flex items-center justify-center gap-1.5 p-2 bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/30 rounded-lg text-[11px] font-bold text-amber-400 transition-colors"
            >
              <Shield className="w-3.5 h-3.5" />
              <span>Admin Profile</span>
            </button>

            <button
              type="button"
              onClick={() => handleQuickLogin('viewer@shortdrama.tv', 'ViewerPass123!')}
              className="flex items-center justify-center gap-1.5 p-2 bg-zinc-800 hover:bg-zinc-700 border border-white/10 rounded-lg text-[11px] font-bold text-zinc-300 transition-colors"
            >
              <Sparkles className="w-3.5 h-3.5 text-red-400" />
              <span>Standard Viewer</span>
            </button>
          </div>
        </div>

        {/* Switch to Register */}
        <div className="text-center pt-2">
          <p className="text-xs text-zinc-400">
            Don't have an account?{' '}
            <button
              type="button"
              onClick={onSwitchToRegister}
              className="text-red-400 font-bold hover:underline"
            >
              Create Free Account
            </button>
          </p>
        </div>

      </div>
    </div>
  );
}
