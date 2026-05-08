import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
    Shield,
    Mail,
    Lock,
    Eye,
    EyeOff,
    User,
    Building,
    AlertCircle,
    Loader2,
    CheckCircle,
    ArrowLeft,
    GraduationCap,
} from 'lucide-react';
import { useAuth }    from '../../context/AuthContext';
import authService    from '../../services/authService';

const Register = () => {

    const navigate  = useNavigate();
    const { login } = useAuth();

    const [formData, setFormData] = useState({
        fullName:        '',
        email:           '',
        password:        '',
        confirmPassword: '',
        institutionName: '',
        role:            'STUDENT', // ← Always STUDENT
    });

    const [showPassword,
           setShowPassword]        = useState(false);
    const [showConfirmPassword,
           setShowConfirmPassword] = useState(false);
    const [isLoading,
           setIsLoading]           = useState(false);
    const [error,
           setError]               = useState('');
    const [success,
           setSuccess]             = useState('');

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
        if (error) setError('');
    };

    const validateForm = () => {
        if (!formData.fullName.trim()) {
            setError('Full name is required');
            return false;
        }
        if (!formData.email.trim()) {
            setError('Email is required');
            return false;
        }
        if (formData.password.length < 6) {
            setError(
                'Password must be at least 6 characters'
            );
            return false;
        }
        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match');
            return false;
        }
        if (!formData.institutionName.trim()) {
            setError('Institution name is required');
            return false;
        }
        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) return;

        setIsLoading(true);
        setError('');

        try {
            const response = await authService.register({
                fullName:        formData.fullName,
                email:           formData.email,
                password:        formData.password,
                institutionName: formData.institutionName,
                role:            'STUDENT', // ← Force STUDENT
            });

            if (response.success) {
                setSuccess('Registration successful!');
                login(response.user, response.token);
                setTimeout(() => {
                    navigate('/student/dashboard');
                }, 1000);
            }
        } catch (err) {
            setError(
                err.response?.data?.message
                || 'Registration failed. Please try again.'
            );
        } finally {
            setIsLoading(false);
        }
    };

    const iconInputStyle    = { paddingLeft: '2.25rem' };
    const iconInputStylePR  = {
        paddingLeft:  '2.25rem',
        paddingRight: '2.5rem'
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
            </div>

            {/* Grid Pattern */}
            <div className="absolute inset-0 opacity-5"
                 style={{
                     backgroundImage: `
                         linear-gradient(rgba(0,212,255,0.3)
                         1px, transparent 1px),
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

            {/* Register Card */}
            <div className="relative w-full max-w-md
                            animate-fade-in my-8">
                <div className="glass-card p-8">

                    {/* Logo and Title */}
                    <div className="text-center mb-6">
                        <div className="inline-flex
                                        items-center
                                        justify-center
                                        w-14 h-14 rounded-2xl
                                        mb-3 bg-cyan-500/20
                                        border
                                        border-cyan-500/30
                                        glow-blue">
                            <Shield className="w-7 h-7
                                               text-cyan-400"/>
                        </div>
                        <h1 className="text-2xl font-bold
                                       text-white mb-1">
                            CyberShield
                        </h1>
                        <p className="text-slate-400 text-sm">
                            Create your student account
                        </p>
                    </div>

                    {/* Student Badge */}
                    <div className="flex items-center
                                    justify-center mb-6">
                        <div className="flex items-center
                                        gap-2 px-4 py-2
                                        rounded-full
                                        bg-cyan-500/10
                                        border
                                        border-cyan-500/20">
                            <GraduationCap className="w-4 h-4
                                                       text-cyan-400"/>
                            <span className="text-sm
                                             font-medium
                                             text-cyan-400">
                                Student Registration
                            </span>
                        </div>
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

                    {/* Success Message */}
                    {success && (
                        <div className="mb-4 p-3 rounded-lg
                                        bg-green-500/10
                                        border
                                        border-green-500/30
                                        flex items-center gap-2">
                            <CheckCircle className="w-4 h-4
                                                      text-green-400
                                                      flex-shrink-0"/>
                            <p className="text-green-400 text-sm">
                                {success}
                            </p>
                        </div>
                    )}

                    {/* Form */}
                    <form onSubmit={handleSubmit}
                          className="space-y-4">

                        {/* Full Name */}
                        <div>
                            <label className="block text-sm
                                              font-medium
                                              text-slate-300 mb-2">
                                Full Name
                            </label>
                            <div className="relative">
                                <User className="absolute left-3
                                                 top-1/2
                                                 -translate-y-1/2
                                                 w-4 h-4
                                                 text-slate-400
                                                 pointer-events-none
                                                 z-10"/>
                                <input
                                    type="text"
                                    name="fullName"
                                    value={formData.fullName}
                                    onChange={handleChange}
                                    placeholder="Enter your full name"
                                    className="cyber-input"
                                    style={iconInputStyle}
                                    required
                                />
                            </div>
                        </div>

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
                                    style={iconInputStyle}
                                    required
                                />
                            </div>
                        </div>

                        {/* Institution */}
                        <div>
                            <label className="block text-sm
                                              font-medium
                                              text-slate-300 mb-2">
                                Institution Name
                            </label>
                            <div className="relative">
                                <Building className="absolute
                                                      left-3 top-1/2
                                                      -translate-y-1/2
                                                      w-4 h-4
                                                      text-slate-400
                                                      pointer-events-none
                                                      z-10"/>
                                <input
                                    type="text"
                                    name="institutionName"
                                    value={
                                        formData.institutionName
                                    }
                                    onChange={handleChange}
                                    placeholder="Enter institution name"
                                    className="cyber-input"
                                    style={iconInputStyle}
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
                                    placeholder="Min 6 characters"
                                    className="cyber-input"
                                    style={iconInputStylePR}
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

                        {/* Confirm Password */}
                        <div>
                            <label className="block text-sm
                                              font-medium
                                              text-slate-300 mb-2">
                                Confirm Password
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
                                    type={showConfirmPassword
                                        ? 'text'
                                        : 'password'}
                                    name="confirmPassword"
                                    value={
                                        formData.confirmPassword
                                    }
                                    onChange={handleChange}
                                    placeholder="Repeat your password"
                                    className="cyber-input"
                                    style={iconInputStylePR}
                                    required
                                />
                                <button
                                    type="button"
                                    onClick={() =>
                                        setShowConfirmPassword(
                                            !showConfirmPassword
                                        )
                                    }
                                    className="absolute right-3
                                               top-1/2
                                               -translate-y-1/2
                                               text-slate-400
                                               hover:text-cyan-400
                                               transition-colors">
                                    {showConfirmPassword
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
                                    Creating account...
                                </>
                            ) : (
                                <>
                                    <GraduationCap className="w-4 h-4"/>
                                    Create Student Account
                                </>
                            )}
                        </button>
                    </form>

                    {/* Login Link */}
                    <div className="mt-6 text-center">
                        <p className="text-slate-400 text-sm">
                            Already have an account?{' '}
                            <Link
                                to="/login"
                                className="text-cyan-400
                                           hover:text-cyan-300
                                           font-medium
                                           transition-colors">
                                Sign in
                            </Link>
                        </p>
                    </div>

                    {/* Admin Info Note */}
                    <div className="mt-4 p-3 rounded-lg
                                    bg-slate-800/50
                                    border border-slate-700/50">
                        <p className="text-xs text-slate-500
                                      text-center flex items-center
                                      justify-center gap-1.5">
                            <Shield className="w-3 h-3
                                               text-slate-500"/>
                            Admin accounts are
                            pre-configured by the system
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Register;
