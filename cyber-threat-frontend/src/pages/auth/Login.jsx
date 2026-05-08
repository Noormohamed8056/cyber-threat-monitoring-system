import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
    Shield,
    Mail,
    Lock,
    Eye,
    EyeOff,
    AlertCircle,
    Loader2,
    ArrowLeft,
} from 'lucide-react';
import { useAuth }    from '../../context/AuthContext';
import authService    from '../../services/authService';

const Login = () => {

    const navigate  = useNavigate();
    const { login } = useAuth();

    const [formData, setFormData] = useState({
        email:    '',
        password: '',
    });
    const [showPassword,
           setShowPassword] = useState(false);
    const [isLoading,
           setIsLoading]    = useState(false);
    const [error,
           setError]        = useState('');

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
        if (error) setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setError('');

        try {
            const response = await authService.login(
                formData.email,
                formData.password
            );

            if (response.success) {
                login(response.user, response.token);
                if (response.user.role === 'ADMIN') {
                    navigate('/admin/dashboard');
                } else {
                    navigate('/student/dashboard');
                }
            }
        } catch (err) {
            setError(
                err.response?.data?.message
                || 'Invalid email or password'
            );
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-cyber-gradient
                        flex items-center justify-center
                        p-4 relative overflow-hidden">

            {/* Background Effects */}
            <div className="absolute inset-0 overflow-hidden">
                <div className="absolute -top-40 -right-40
                                w-80 h-80 rounded-full
                                bg-cyan-500/10 blur-3xl"/>
                <div className="absolute -bottom-40 -left-40
                                w-80 h-80 rounded-full
                                bg-blue-500/10 blur-3xl"/>
                <div className="absolute top-1/2 left-1/2
                                -translate-x-1/2
                                -translate-y-1/2
                                w-96 h-96 rounded-full
                                bg-purple-500/5 blur-3xl"/>
            </div>

            {/* Grid Pattern */}
            <div className="absolute inset-0 opacity-5"
                 style={{
                     backgroundImage: `
                         linear-gradient(
                         rgba(0,212,255,0.3) 1px,
                         transparent 1px),
                         linear-gradient(90deg,
                         rgba(0,212,255,0.3) 1px,
                         transparent 1px)`,
                     backgroundSize: '50px 50px'
                 }}/>

            {/* Back Button */}
            <button
                onClick={() => navigate('/')}
                className="absolute top-6 left-6 z-10
                           flex items-center gap-2
                           text-slate-400
                           hover:text-cyan-400
                           transition-colors text-sm
                           bg-slate-800/60
                           hover:bg-slate-800
                           border border-slate-700/50
                           hover:border-cyan-500/30
                           px-3 py-2 rounded-lg">
                <ArrowLeft className="w-4 h-4"/>
                Back
            </button>

            {/* Login Card */}
            <div className="relative w-full max-w-md
                            animate-fade-in">
                <div className="glass-card p-8">

                    {/* Logo and Title */}
                    <div className="text-center mb-8">
                        <div className="inline-flex
                                        items-center
                                        justify-center
                                        w-16 h-16
                                        rounded-2xl mb-4
                                        bg-cyan-500/20
                                        border
                                        border-cyan-500/30
                                        glow-blue">
                            <Shield className="w-8 h-8
                                               text-cyan-400"/>
                        </div>
                        <h1 className="text-2xl font-bold
                                       text-white mb-1">
                            CyberShield
                        </h1>
                        <p className="text-slate-400 text-sm">
                            AI-Driven Threat Monitoring System
                        </p>
                    </div>

                    {/* Welcome Text */}
                    <div className="mb-6">
                        <h2 className="text-xl font-semibold
                                       text-white">
                            Welcome back
                        </h2>
                        <p className="text-slate-400
                                      text-sm mt-1">
                            Sign in to your account
                            to continue
                        </p>
                    </div>

                    {/* Error Message */}
                    {error && (
                        <div className="mb-4 p-3 rounded-lg
                                        bg-red-500/10
                                        border border-red-500/30
                                        flex items-center gap-2">
                            <AlertCircle className="w-4 h-4
                                                     text-red-400
                                                     flex-shrink-0"/>
                            <p className="text-red-400 text-sm">
                                {error}
                            </p>
                        </div>
                    )}

                    {/* Form */}
                    <form onSubmit={handleSubmit}
                          className="space-y-4">

                        {/* Email */}
                        <div>
                            <label className="block text-sm
                                              font-medium
                                              text-slate-300 mb-2">
                                Email Address
                            </label>
                            <div className="relative">
                                <Mail className="absolute left-3
                                                 top-1/2
                                                 -translate-y-1/2
                                                 w-4 h-4
                                                 text-slate-400
                                                 pointer-events-none
                                                 z-10"/>
                                <input
                                    type="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleChange}
                                    placeholder="Enter your email"
                                    className="cyber-input"
                                    style={{
                                        paddingLeft: '2.25rem'
                                    }}
                                    required
                                />
                            </div>
                        </div>

                        {/* Password */}
                        <div>
                            <label className="block text-sm
                                              font-medium
                                              text-slate-300 mb-2">
                                Password
                            </label>
                            <div className="relative">
                                <Lock className="absolute left-3
                                                 top-1/2
                                                 -translate-y-1/2
                                                 w-4 h-4
                                                 text-slate-400
                                                 pointer-events-none
                                                 z-10"/>
                                <input
                                    type={showPassword
                                        ? 'text'
                                        : 'password'}
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    placeholder="Enter your password"
                                    className="cyber-input"
                                    style={{
                                        paddingLeft:  '2.25rem',
                                        paddingRight: '2.5rem',
                                    }}
                                    required
                                />
                                <button
                                    type="button"
                                    onClick={() =>
                                        setShowPassword(
                                            !showPassword
                                        )
                                    }
                                    className="absolute right-3
                                               top-1/2
                                               -translate-y-1/2
                                               text-slate-400
                                               hover:text-cyan-400
                                               transition-colors">
                                    {showPassword
                                        ? <EyeOff className="w-4 h-4"/>
                                        : <Eye    className="w-4 h-4"/>
                                    }
                                </button>
                            </div>
                        </div>

                        {/* Submit Button */}
                        <button
                            type="submit"
                            disabled={isLoading}
                            className="w-full btn-cyber
                                       flex items-center
                                       justify-center gap-2
                                       py-3 mt-2
                                       disabled:opacity-50
                                       disabled:cursor-not-allowed">
                            {isLoading ? (
                                <>
                                    <Loader2 className="w-4 h-4
                                                        animate-spin"/>
                                    Signing in...
                                </>
                            ) : (
                                <>
                                    <Shield className="w-4 h-4"/>
                                    Sign In
                                </>
                            )}
                        </button>
                    </form>

                    {/* Register Link */}
                    <div className="mt-6 text-center">
                        <p className="text-slate-400 text-sm">
                            Don't have an account?{' '}
                            <Link
                                to="/register"
                                className="text-cyan-400
                                           hover:text-cyan-300
                                           font-medium
                                           transition-colors">
                                Create student account
                            </Link>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;
