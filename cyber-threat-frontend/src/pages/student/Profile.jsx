import React, { useState } from 'react';
import { useNavigate }      from 'react-router-dom';
import {
    User,
    Mail,
    Building,
    Shield,
    Edit3,
    Save,
    X,
    ArrowLeft,
    CheckCircle,
    AlertCircle,
    Loader2,
    Lock,
    Eye,
    EyeOff,
    GraduationCap,
    Calendar,
    Award,
} from 'lucide-react';
import authService    from '../../services/authService';
import { useAuth }    from '../../context/AuthContext';
import { formatDateTime,
         getInitials } from '../../utils/helpers';

// Reusable inline padding constants
const iconLeft     = { paddingLeft: '2.25rem' };
const iconBoth     = { paddingLeft: '2.25rem', paddingRight: '2.5rem' };

const Profile = () => {

    const navigate             = useNavigate();
    const { user, updateUser } = useAuth();

    const [isEditingProfile,   setIsEditingProfile]   = useState(false);
    const [profileData,        setProfileData]        = useState({
        fullName:        user?.fullName        || '',
        institutionName: user?.institutionName || '',
    });
    const [profileLoading,  setProfileLoading]  = useState(false);
    const [profileSuccess,  setProfileSuccess]  = useState('');
    const [profileError,    setProfileError]    = useState('');

    const [isChangingPassword, setIsChangingPassword] = useState(false);
    const [passwordData,       setPasswordData]       = useState({
        currentPassword: '',
        newPassword:     '',
        confirmPassword: '',
    });
    const [showPasswords, setShowPasswords] = useState({
        current: false,
        new:     false,
        confirm: false,
    });
    const [passwordLoading, setPasswordLoading] = useState(false);
    const [passwordSuccess, setPasswordSuccess] = useState('');
    const [passwordError,   setPasswordError]   = useState('');

    const handleProfileChange = (e) => {
        setProfileData({ ...profileData, [e.target.name]: e.target.value });
        setProfileError('');
    };

    const handlePasswordChange = (e) => {
        setPasswordData({ ...passwordData, [e.target.name]: e.target.value });
        setPasswordError('');
    };

    const handleSaveProfile = async () => {
        if (!profileData.fullName.trim()) {
            setProfileError('Full name is required');
            return;
        }
        setProfileLoading(true);
        setProfileError('');
        try {
            const response = await authService.updateProfile(
                profileData.fullName,
                profileData.institutionName
            );
            if (response.success) {
                updateUser(response.user);
                setProfileSuccess('Profile updated successfully!');
                setIsEditingProfile(false);
                setTimeout(() => setProfileSuccess(''), 3000);
            }
        } catch (err) {
            setProfileError(
                err.response?.data?.message || 'Failed to update profile'
            );
        } finally {
            setProfileLoading(false);
        }
    };

    const handleSavePassword = async () => {
        if (!passwordData.currentPassword) {
            setPasswordError('Current password is required'); return;
        }
        if (passwordData.newPassword.length < 6) {
            setPasswordError('New password must be at least 6 characters'); return;
        }
        if (passwordData.newPassword !== passwordData.confirmPassword) {
            setPasswordError('New passwords do not match'); return;
        }
        setPasswordLoading(true);
        setPasswordError('');
        try {
            await authService.changePassword(
                passwordData.currentPassword,
                passwordData.newPassword
            );
            setPasswordSuccess('Password changed successfully!');
            setIsChangingPassword(false);
            setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
            setTimeout(() => setPasswordSuccess(''), 3000);
        } catch (err) {
            setPasswordError(
                err.response?.data?.message || 'Failed to change password'
            );
        } finally {
            setPasswordLoading(false);
        }
    };

    return (
        <div className="space-y-6 animate-fade-in max-w-2xl mx-auto">

            {/* Header */}
            <div className="flex items-center gap-4">
                <button
                    onClick={() => navigate(-1)}
                    className="w-9 h-9 rounded-xl flex items-center
                               justify-center bg-white/5
                               border border-white/10 text-slate-400
                               hover:text-cyan-400 transition-all">
                    <ArrowLeft className="w-4 h-4"/>
                </button>
                <div>
                    <h1 className="text-2xl font-bold text-white">
                        My Profile
                    </h1>
                    <p className="text-slate-400 text-sm">
                        Manage your account information
                    </p>
                </div>
            </div>

            {/* ── Profile Card ── */}
            <div className="glass-card p-6 border border-white/5">

                {/* Avatar Section */}
                <div className="flex items-center gap-5 mb-6 pb-6
                                border-b border-white/5">
                    <div className="w-20 h-20 rounded-2xl
                                    bg-gradient-to-br
                                    from-cyan-500/30 to-blue-500/30
                                    border border-cyan-500/20
                                    flex items-center justify-center
                                    flex-shrink-0">
                        <span className="text-2xl font-bold text-cyan-400">
                            {getInitials(user?.fullName)}
                        </span>
                    </div>
                    <div>
                        <h2 className="text-xl font-bold text-white">
                            {user?.fullName}
                        </h2>
                        <p className="text-sm text-slate-400">{user?.email}</p>
                        <div className="flex items-center gap-2 mt-2">
                            <span className={`inline-flex items-center gap-1.5
                                              px-3 py-1 rounded-full text-xs
                                              font-medium border ${
                                user?.role === 'ADMIN'
                                    ? 'bg-purple-500/10 text-purple-400 border-purple-500/20'
                                    : 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20'
                            }`}>
                                {user?.role === 'ADMIN'
                                    ? <Shield className="w-3 h-3"/>
                                    : <GraduationCap className="w-3 h-3"/>
                                }
                                {user?.role}
                            </span>
                            <span className="inline-flex items-center gap-1.5
                                             px-3 py-1 rounded-full text-xs
                                             font-medium bg-green-500/10
                                             text-green-400 border border-green-500/20">
                                <div className="w-1.5 h-1.5 rounded-full bg-green-400"/>
                                Active
                            </span>
                        </div>
                    </div>
                </div>

                {/* Messages */}
                {profileSuccess && (
                    <div className="mb-4 p-3 rounded-xl bg-green-500/10
                                    border border-green-500/30
                                    flex items-center gap-2">
                        <CheckCircle className="w-4 h-4 text-green-400"/>
                        <p className="text-green-400 text-sm">{profileSuccess}</p>
                    </div>
                )}
                {profileError && (
                    <div className="mb-4 p-3 rounded-xl bg-red-500/10
                                    border border-red-500/30
                                    flex items-center gap-2">
                        <AlertCircle className="w-4 h-4 text-red-400"/>
                        <p className="text-red-400 text-sm">{profileError}</p>
                    </div>
                )}

                {/* Profile Fields */}
                <div className="space-y-4">

                    {/* ✅ Full Name */}
                    <div>
                        <label className="block text-xs font-medium
                                          text-slate-400 uppercase
                                          tracking-wider mb-2">
                            Full Name
                        </label>
                        <div className="relative">
                            <User className="absolute left-3 top-1/2
                                             -translate-y-1/2 w-4 h-4
                                             text-slate-400 pointer-events-none z-10"/>
                            <input
                                type="text"
                                name="fullName"
                                value={profileData.fullName}
                                onChange={handleProfileChange}
                                disabled={!isEditingProfile}
                                className={`cyber-input text-sm ${
                                    !isEditingProfile
                                        ? 'opacity-60 cursor-not-allowed'
                                        : ''
                                }`}
                                style={iconLeft}
                            />
                        </div>
                    </div>

                    {/* ✅ Email */}
                    <div>
                        <label className="block text-xs font-medium
                                          text-slate-400 uppercase
                                          tracking-wider mb-2">
                            Email Address
                            <span className="ml-2 text-slate-600
                                             normal-case font-normal">
                                (cannot be changed)
                            </span>
                        </label>
                        <div className="relative">
                            <Mail className="absolute left-3 top-1/2
                                             -translate-y-1/2 w-4 h-4
                                             text-slate-400 pointer-events-none z-10"/>
                            <input
                                type="email"
                                value={user?.email || ''}
                                disabled
                                className="cyber-input text-sm
                                           opacity-50 cursor-not-allowed"
                                style={iconLeft}
                            />
                        </div>
                    </div>

                    {/* ✅ Institution */}
                    <div>
                        <label className="block text-xs font-medium
                                          text-slate-400 uppercase
                                          tracking-wider mb-2">
                            Institution Name
                        </label>
                        <div className="relative">
                            <Building className="absolute left-3 top-1/2
                                                  -translate-y-1/2 w-4 h-4
                                                  text-slate-400 pointer-events-none z-10"/>
                            <input
                                type="text"
                                name="institutionName"
                                value={profileData.institutionName}
                                onChange={handleProfileChange}
                                disabled={!isEditingProfile}
                                className={`cyber-input text-sm ${
                                    !isEditingProfile
                                        ? 'opacity-60 cursor-not-allowed'
                                        : ''
                                }`}
                                style={iconLeft}
                            />
                        </div>
                    </div>

                    {/* ✅ Role */}
                    <div>
                        <label className="block text-xs font-medium
                                          text-slate-400 uppercase
                                          tracking-wider mb-2">
                            Role
                        </label>
                        <div className="relative">
                            <Shield className="absolute left-3 top-1/2
                                               -translate-y-1/2 w-4 h-4
                                               text-slate-400 pointer-events-none z-10"/>
                            <input
                                type="text"
                                value={user?.role || ''}
                                disabled
                                className="cyber-input text-sm
                                           opacity-50 cursor-not-allowed"
                                style={iconLeft}
                            />
                        </div>
                    </div>

                    {/* ✅ Member Since */}
                    <div>
                        <label className="block text-xs font-medium
                                          text-slate-400 uppercase
                                          tracking-wider mb-2">
                            Member Since
                        </label>
                        <div className="relative">
                            <Calendar className="absolute left-3 top-1/2
                                                  -translate-y-1/2 w-4 h-4
                                                  text-slate-400 pointer-events-none z-10"/>
                            <input
                                type="text"
                                value={formatDateTime(user?.createdAt) || 'N/A'}
                                disabled
                                className="cyber-input text-sm
                                           opacity-50 cursor-not-allowed"
                                style={iconLeft}
                            />
                        </div>
                    </div>
                </div>

                {/* Edit / Save Buttons */}
                <div className="flex gap-3 mt-6">
                    {!isEditingProfile ? (
                        <button
                            onClick={() => setIsEditingProfile(true)}
                            className="flex items-center gap-2 px-5 py-2.5
                                       rounded-xl bg-white/5 border border-white/10
                                       text-slate-300 hover:text-cyan-400
                                       hover:border-cyan-500/20 transition-all
                                       text-sm font-medium">
                            <Edit3 className="w-4 h-4"/>
                            Edit Profile
                        </button>
                    ) : (
                        <>
                            <button
                                onClick={handleSaveProfile}
                                disabled={profileLoading}
                                className="flex items-center gap-2 px-5 py-2.5
                                           btn-cyber text-sm disabled:opacity-50">
                                {profileLoading
                                    ? <Loader2 className="w-4 h-4 animate-spin"/>
                                    : <Save className="w-4 h-4"/>
                                }
                                {profileLoading ? 'Saving...' : 'Save Changes'}
                            </button>
                            <button
                                onClick={() => {
                                    setIsEditingProfile(false);
                                    setProfileData({
                                        fullName:        user?.fullName        || '',
                                        institutionName: user?.institutionName || '',
                                    });
                                    setProfileError('');
                                }}
                                className="flex items-center gap-2 px-5 py-2.5
                                           rounded-xl bg-white/5 border border-white/10
                                           text-slate-300 hover:bg-white/10
                                           transition-all text-sm">
                                <X className="w-4 h-4"/>
                                Cancel
                            </button>
                        </>
                    )}
                </div>
            </div>

            {/* ── Change Password Card ── */}
            <div className="glass-card p-6 border border-white/5">

                <div className="flex items-center justify-between mb-5">
                    <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-xl bg-yellow-500/10
                                        border border-yellow-500/20
                                        flex items-center justify-center">
                            <Lock className="w-4 h-4 text-yellow-400"/>
                        </div>
                        <div>
                            <h3 className="text-sm font-bold text-white">
                                Password & Security
                            </h3>
                            <p className="text-xs text-slate-400">
                                Keep your account secure
                            </p>
                        </div>
                    </div>
                    {!isChangingPassword && (
                        <button
                            onClick={() => setIsChangingPassword(true)}
                            className="flex items-center gap-2 px-4 py-2
                                       rounded-xl bg-white/5 border border-white/10
                                       text-slate-300 hover:text-yellow-400
                                       hover:border-yellow-500/20 transition-all text-sm">
                            <Edit3 className="w-3 h-3"/>
                            Change
                        </button>
                    )}
                </div>

                {passwordSuccess && (
                    <div className="mb-4 p-3 rounded-xl bg-green-500/10
                                    border border-green-500/30
                                    flex items-center gap-2">
                        <CheckCircle className="w-4 h-4 text-green-400"/>
                        <p className="text-green-400 text-sm">{passwordSuccess}</p>
                    </div>
                )}
                {passwordError && (
                    <div className="mb-4 p-3 rounded-xl bg-red-500/10
                                    border border-red-500/30
                                    flex items-center gap-2">
                        <AlertCircle className="w-4 h-4 text-red-400"/>
                        <p className="text-red-400 text-sm">{passwordError}</p>
                    </div>
                )}

                {!isChangingPassword ? (
                    <div className="flex items-center gap-3 p-4 rounded-xl
                                    bg-white/3 border border-white/5">
                        <Lock className="w-4 h-4 text-slate-400"/>
                        <p className="text-sm text-slate-400">
                            Password is set and secure
                        </p>
                        <div className="ml-auto flex gap-1">
                            {[...Array(8)].map((_, i) => (
                                <div key={i}
                                     className="w-2 h-2 rounded-full bg-slate-600"/>
                            ))}
                        </div>
                    </div>
                ) : (
                    <div className="space-y-4">

                        {/* ✅ Current Password */}
                        <div>
                            <label className="block text-xs font-medium
                                              text-slate-400 uppercase
                                              tracking-wider mb-2">
                                Current Password
                            </label>
                            <div className="relative">
                                <Lock className="absolute left-3 top-1/2
                                                 -translate-y-1/2 w-4 h-4
                                                 text-slate-400 pointer-events-none z-10"/>
                                <input
                                    type={showPasswords.current ? 'text' : 'password'}
                                    name="currentPassword"
                                    value={passwordData.currentPassword}
                                    onChange={handlePasswordChange}
                                    placeholder="Enter current password"
                                    className="cyber-input text-sm"
                                    style={iconBoth}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPasswords({
                                        ...showPasswords,
                                        current: !showPasswords.current
                                    })}
                                    className="absolute right-3 top-1/2
                                               -translate-y-1/2 text-slate-400
                                               hover:text-cyan-400 transition-colors">
                                    {showPasswords.current
                                        ? <EyeOff className="w-4 h-4"/>
                                        : <Eye    className="w-4 h-4"/>
                                    }
                                </button>
                            </div>
                        </div>

                        {/* ✅ New Password */}
                        <div>
                            <label className="block text-xs font-medium
                                              text-slate-400 uppercase
                                              tracking-wider mb-2">
                                New Password
                            </label>
                            <div className="relative">
                                <Lock className="absolute left-3 top-1/2
                                                 -translate-y-1/2 w-4 h-4
                                                 text-slate-400 pointer-events-none z-10"/>
                                <input
                                    type={showPasswords.new ? 'text' : 'password'}
                                    name="newPassword"
                                    value={passwordData.newPassword}
                                    onChange={handlePasswordChange}
                                    placeholder="Min 6 characters"
                                    className="cyber-input text-sm"
                                    style={iconBoth}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPasswords({
                                        ...showPasswords,
                                        new: !showPasswords.new
                                    })}
                                    className="absolute right-3 top-1/2
                                               -translate-y-1/2 text-slate-400
                                               hover:text-cyan-400 transition-colors">
                                    {showPasswords.new
                                        ? <EyeOff className="w-4 h-4"/>
                                        : <Eye    className="w-4 h-4"/>
                                    }
                                </button>
                            </div>
                        </div>

                        {/* ✅ Confirm New Password */}
                        <div>
                            <label className="block text-xs font-medium
                                              text-slate-400 uppercase
                                              tracking-wider mb-2">
                                Confirm New Password
                            </label>
                            <div className="relative">
                                <Lock className="absolute left-3 top-1/2
                                                 -translate-y-1/2 w-4 h-4
                                                 text-slate-400 pointer-events-none z-10"/>
                                <input
                                    type={showPasswords.confirm ? 'text' : 'password'}
                                    name="confirmPassword"
                                    value={passwordData.confirmPassword}
                                    onChange={handlePasswordChange}
                                    placeholder="Repeat new password"
                                    className="cyber-input text-sm"
                                    style={iconBoth}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPasswords({
                                        ...showPasswords,
                                        confirm: !showPasswords.confirm
                                    })}
                                    className="absolute right-3 top-1/2
                                               -translate-y-1/2 text-slate-400
                                               hover:text-cyan-400 transition-colors">
                                    {showPasswords.confirm
                                        ? <EyeOff className="w-4 h-4"/>
                                        : <Eye    className="w-4 h-4"/>
                                    }
                                </button>
                            </div>
                        </div>

                        {/* Buttons */}
                        <div className="flex gap-3">
                            <button
                                onClick={handleSavePassword}
                                disabled={passwordLoading}
                                className="flex-1 btn-cyber py-2.5 text-sm
                                           flex items-center justify-center
                                           gap-2 disabled:opacity-50">
                                {passwordLoading
                                    ? <Loader2 className="w-4 h-4 animate-spin"/>
                                    : <Save className="w-4 h-4"/>
                                }
                                {passwordLoading ? 'Saving...' : 'Update Password'}
                            </button>
                            <button
                                onClick={() => {
                                    setIsChangingPassword(false);
                                    setPasswordData({
                                        currentPassword: '',
                                        newPassword:     '',
                                        confirmPassword: '',
                                    });
                                    setPasswordError('');
                                }}
                                className="flex-1 py-2.5 rounded-xl bg-white/5
                                           border border-white/10 text-slate-300
                                           hover:bg-white/10 transition-all text-sm">
                                Cancel
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* ── Account Stats Card ── */}
            <div className="glass-card p-6 border border-white/5">
                <div className="flex items-center gap-2 mb-5">
                    <Award className="w-4 h-4 text-cyan-400"/>
                    <h3 className="text-sm font-bold text-white">
                        Account Overview
                    </h3>
                </div>
                <div className="grid grid-cols-3 gap-4">
                    {[
                        {
                            label: 'Member Since',
                            value: user?.createdAt
                                ? new Date(user.createdAt).getFullYear()
                                : 'N/A',
                            color: 'text-cyan-400',
                        },
                        {
                            label: 'Account Status',
                            value: 'Active',
                            color: 'text-green-400',
                        },
                        {
                            label: 'Role',
                            value: user?.role || 'STUDENT',
                            color: 'text-purple-400',
                        },
                    ].map((stat, i) => (
                        <div key={i}
                             className="text-center p-4 rounded-xl
                                        bg-white/3 border border-white/5">
                            <p className={`text-lg font-bold ${stat.color}`}>
                                {stat.value}
                            </p>
                            <p className="text-xs text-slate-400 mt-1">
                                {stat.label}
                            </p>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default Profile;
