import React, { useEffect, useState } from 'react';
import { useNavigate }      from 'react-router-dom';
import {
    Settings as SettingsIcon,
    Bell,
    Shield,
    Moon,
    Trash2,
    ArrowLeft,
    CheckCircle,
    ChevronRight,
    LogOut,
    Lock,
    Eye,
    Download,
} from 'lucide-react';
import { useAuth }    from '../../context/AuthContext';
import authService from '../../services/authService';
import reportService from '../../services/reportService';

const SETTINGS_STORAGE_KEY = 'student_settings_v1';

// ==========================================
// TOGGLE COMPONENT
// ==========================================
const Toggle = ({ enabled, onChange }) => (
    <button
        onClick={() => onChange(!enabled)}
        className={`relative inline-flex
                    items-center w-11 h-6
                    rounded-full transition-all
                    duration-300 focus:outline-none
                    ${enabled
                        ? 'bg-cyan-500'
                        : 'bg-slate-700'
                    }`}>
        <span className={`inline-block w-4 h-4
                          rounded-full bg-white
                          transform transition-transform
                          duration-300
                          ${enabled
                              ? 'translate-x-6'
                              : 'translate-x-1'
                          }`}/>
    </button>
);

// ==========================================
// SETTINGS SECTION COMPONENT
// ==========================================
const SettingsSection = ({ title, icon, children }) => (
    <div className="glass-card border border-white/5
                    overflow-hidden">
        <div className="flex items-center gap-3 p-5
                        border-b border-white/5">
            <div className="w-8 h-8 rounded-lg
                            bg-cyan-500/10
                            border border-cyan-500/20
                            flex items-center
                            justify-center">
                {React.createElement(icon, {
                    className: 'w-4 h-4 text-cyan-400',
                })}
            </div>
            <h3 className="text-sm font-bold text-white">
                {title}
            </h3>
        </div>
        <div className="divide-y divide-white/5">
            {children}
        </div>
    </div>
);

// ==========================================
// SETTINGS ROW COMPONENT
// ==========================================
const SettingsRow = ({
    label,
    description,
    action,
    danger = false,
}) => (
    <div className={`flex items-center
                     justify-between p-5
                     hover:bg-white/2
                     transition-colors
                     ${danger
                         ? 'hover:bg-red-500/3'
                         : ''
                     }`}>
        <div className="flex-1 min-w-0 mr-4">
            <p className={`text-sm font-medium
                           ${danger
                               ? 'text-red-400'
                               : 'text-white'
                           }`}>
                {label}
            </p>
            {description && (
                <p className="text-xs text-slate-500 mt-0.5">
                    {description}
                </p>
            )}
        </div>
        <div className="flex-shrink-0">
            {action}
        </div>
    </div>
);

// ==========================================
// SETTINGS PAGE
// ==========================================
const Settings = () => {

    const navigate      = useNavigate();
    const { logout, user }    = useAuth();

    // ==========================================
    // NOTIFICATION SETTINGS
    // ==========================================
    const [notifications, setNotifications] = useState({
        emailAlerts:      true,
        pushAlerts:       true,
        criticalOnly:     false,
        weeklyDigest:     true,
        reportUpdates:    true,
        newAlerts:        true,
    });

    // ==========================================
    // PRIVACY SETTINGS
    // ==========================================
    const [privacy, setPrivacy] = useState({
        showProfile:      true,
        shareActivity:    false,
        dataCollection:   true,
    });

    // ==========================================
    // APPEARANCE SETTINGS
    // ==========================================
    const [appearance, setAppearance] = useState({
        darkMode:         true,
        compactMode:      false,
        animations:       true,
    });

    // ==========================================
    // SUCCESS/ERROR STATE
    // ==========================================
    const [saveSuccess, setSaveSuccess] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [isSaving, setIsSaving] = useState(false);
    const [isExporting, setIsExporting] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [showDeleteConfirm,
           setShowDeleteConfirm] = useState(false);

    useEffect(() => {
        try {
            const raw = localStorage.getItem(SETTINGS_STORAGE_KEY);
            if (!raw) return;
            const saved = JSON.parse(raw);
            if (saved.notifications) {
                setNotifications((prev) => ({ ...prev, ...saved.notifications }));
            }
            if (saved.privacy) {
                setPrivacy((prev) => ({ ...prev, ...saved.privacy }));
            }
            if (saved.appearance) {
                setAppearance((prev) => ({ ...prev, ...saved.appearance }));
            }
        } catch (error) {
            console.error('Failed to load settings', error);
        }
    }, []);

    // ==========================================
    // HANDLE SAVE SETTINGS
    // ==========================================
    const handleSave = () => {
        setIsSaving(true);
        setErrorMessage('');
        try {
            localStorage.setItem(
                SETTINGS_STORAGE_KEY,
                JSON.stringify({
                    notifications,
                    privacy,
                    appearance,
                })
            );
            setSaveSuccess('Settings saved successfully!');
            setTimeout(() => setSaveSuccess(''), 3000);
        } catch (error) {
            console.error('Failed to save settings', error);
            setErrorMessage('Failed to save settings. Please try again.');
        } finally {
            setIsSaving(false);
        }
    };

    // ==========================================
    // HANDLE LOGOUT
    // ==========================================
    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const handleExportData = async () => {
        setIsExporting(true);
        setErrorMessage('');
        try {
            const [profileRes, reportsRes] = await Promise.all([
                authService.getCurrentUser(),
                reportService.getMyReports(),
            ]);

            const exportPayload = {
                exportedAt: new Date().toISOString(),
                user: profileRes?.user || user || null,
                settings: {
                    notifications,
                    privacy,
                    appearance,
                },
                reports: reportsRes?.reports || [],
            };

            const blob = new Blob(
                [JSON.stringify(exportPayload, null, 2)],
                { type: 'application/json' }
            );
            const url = URL.createObjectURL(blob);
            const anchor = document.createElement('a');
            anchor.href = url;
            anchor.download = `cybershield-data-${Date.now()}.json`;
            document.body.appendChild(anchor);
            anchor.click();
            anchor.remove();
            URL.revokeObjectURL(url);
            setSaveSuccess('Data exported successfully!');
            setTimeout(() => setSaveSuccess(''), 3000);
        } catch (error) {
            console.error('Export failed', error);
            setErrorMessage('Failed to export data. Please try again.');
        } finally {
            setIsExporting(false);
        }
    };

    const handleDeleteAccount = async () => {
        setIsDeleting(true);
        setErrorMessage('');
        try {
            await authService.deleteAccount();
            localStorage.removeItem(SETTINGS_STORAGE_KEY);
            logout();
            navigate('/login');
        } catch (error) {
            console.error('Delete account failed', error);
            setErrorMessage(
                error?.response?.data?.message
                || 'Failed to delete account. Please try again.'
            );
        } finally {
            setIsDeleting(false);
            setShowDeleteConfirm(false);
        }
    };

    // ==========================================
    // RENDER
    // ==========================================
    return (
        <div className="space-y-6 animate-fade-in
                        max-w-2xl mx-auto">

            {/* Header */}
            <div className="flex items-center
                            justify-between">
                <div className="flex items-center gap-4">
                    <button
                        onClick={() => navigate(-1)}
                        className="w-9 h-9 rounded-xl
                                   flex items-center
                                   justify-center
                                   bg-white/5
                                   border border-white/10
                                   text-slate-400
                                   hover:text-cyan-400
                                   transition-all">
                        <ArrowLeft className="w-4 h-4"/>
                    </button>
                    <div>
                        <h1 className="text-2xl font-bold
                                       text-white">
                            Settings
                        </h1>
                        <p className="text-slate-400
                                      text-sm mt-1">
                            Manage your preferences
                        </p>
                    </div>
                </div>
                <button
                    onClick={handleSave}
                    disabled={isSaving}
                    className="btn-cyber flex items-center
                               gap-2 px-4 py-2 text-sm disabled:opacity-60">
                    <CheckCircle className="w-4 h-4"/>
                    {isSaving ? 'Saving...' : 'Save Settings'}
                </button>
            </div>

            {/* Success Message */}
            {saveSuccess && (
                <div className="p-4 rounded-xl
                                bg-green-500/10
                                border border-green-500/30
                                flex items-center gap-3">
                    <CheckCircle className="w-5 h-5
                                             text-green-400"/>
                    <p className="text-green-400 text-sm">
                        {saveSuccess}
                    </p>
                </div>
            )}

            {errorMessage && (
                <div className="p-4 rounded-xl
                                bg-red-500/10
                                border border-red-500/30
                                flex items-center gap-3">
                    <Trash2 className="w-5 h-5 text-red-400"/>
                    <p className="text-red-400 text-sm">
                        {errorMessage}
                    </p>
                </div>
            )}

            {/* Notification Settings */}
            <SettingsSection
                title="Notifications"
                icon={Bell}>

                <SettingsRow
                    label="Email Alerts"
                    description="Receive security alerts via email"
                    action={
                        <Toggle
                            enabled={
                                notifications.emailAlerts
                            }
                            onChange={(val) =>
                                setNotifications({
                                    ...notifications,
                                    emailAlerts: val,
                                })
                            }
                        />
                    }
                />
                <SettingsRow
                    label="Push Notifications"
                    description="Browser push notifications for alerts"
                    action={
                        <Toggle
                            enabled={
                                notifications.pushAlerts
                            }
                            onChange={(val) =>
                                setNotifications({
                                    ...notifications,
                                    pushAlerts: val,
                                })
                            }
                        />
                    }
                />
                <SettingsRow
                    label="Critical Alerts Only"
                    description="Only notify for HIGH and CRITICAL severity"
                    action={
                        <Toggle
                            enabled={
                                notifications.criticalOnly
                            }
                            onChange={(val) =>
                                setNotifications({
                                    ...notifications,
                                    criticalOnly: val,
                                })
                            }
                        />
                    }
                />
                <SettingsRow
                    label="Report Status Updates"
                    description="Get notified when admin reviews your report"
                    action={
                        <Toggle
                            enabled={
                                notifications.reportUpdates
                            }
                            onChange={(val) =>
                                setNotifications({
                                    ...notifications,
                                    reportUpdates: val,
                                })
                            }
                        />
                    }
                />
                <SettingsRow
                    label="New Institution Alerts"
                    description="Notify when new alerts are published"
                    action={
                        <Toggle
                            enabled={
                                notifications.newAlerts
                            }
                            onChange={(val) =>
                                setNotifications({
                                    ...notifications,
                                    newAlerts: val,
                                })
                            }
                        />
                    }
                />
                <SettingsRow
                    label="Weekly Security Digest"
                    description="Weekly summary of security activity"
                    action={
                        <Toggle
                            enabled={
                                notifications.weeklyDigest
                            }
                            onChange={(val) =>
                                setNotifications({
                                    ...notifications,
                                    weeklyDigest: val,
                                })
                            }
                        />
                    }
                />
            </SettingsSection>

            {/* Appearance Settings */}
            <SettingsSection
                title="Appearance"
                icon={Moon}>

                <SettingsRow
                    label="Dark Mode"
                    description="Use dark theme (recommended)"
                    action={
                        <Toggle
                            enabled={appearance.darkMode}
                            onChange={(val) =>
                                setAppearance({
                                    ...appearance,
                                    darkMode: val,
                                })
                            }
                        />
                    }
                />
                <SettingsRow
                    label="Compact Mode"
                    description="Reduce spacing for more content"
                    action={
                        <Toggle
                            enabled={
                                appearance.compactMode
                            }
                            onChange={(val) =>
                                setAppearance({
                                    ...appearance,
                                    compactMode: val,
                                })
                            }
                        />
                    }
                />
                <SettingsRow
                    label="Animations"
                    description="Enable page transition animations"
                    action={
                        <Toggle
                            enabled={
                                appearance.animations
                            }
                            onChange={(val) =>
                                setAppearance({
                                    ...appearance,
                                    animations: val,
                                })
                            }
                        />
                    }
                />
            </SettingsSection>

            {/* Privacy Settings */}
            <SettingsSection
                title="Privacy"
                icon={Eye}>

                <SettingsRow
                    label="Show Profile to Admins"
                    description="Allow admins to view your profile details"
                    action={
                        <Toggle
                            enabled={privacy.showProfile}
                            onChange={(val) =>
                                setPrivacy({
                                    ...privacy,
                                    showProfile: val,
                                })
                            }
                        />
                    }
                />
                <SettingsRow
                    label="Share Activity Data"
                    description="Help improve the system with anonymous data"
                    action={
                        <Toggle
                            enabled={
                                privacy.shareActivity
                            }
                            onChange={(val) =>
                                setPrivacy({
                                    ...privacy,
                                    shareActivity: val,
                                })
                            }
                        />
                    }
                />
                <SettingsRow
                    label="Data Collection"
                    description="Allow collection of usage statistics"
                    action={
                        <Toggle
                            enabled={
                                privacy.dataCollection
                            }
                            onChange={(val) =>
                                setPrivacy({
                                    ...privacy,
                                    dataCollection: val,
                                })
                            }
                        />
                    }
                />
            </SettingsSection>

            {/* Security Settings */}
            <SettingsSection
                title="Security"
                icon={Shield}>

                <SettingsRow
                    label="Change Password"
                    description="Update your account password"
                    action={
                        <button
                            onClick={() =>
                                navigate(
                                    '/student/profile'
                                )
                            }
                            className="flex items-center
                                       gap-2 px-4 py-2
                                       rounded-lg
                                       bg-white/5
                                       border border-white/10
                                       text-slate-300
                                       hover:text-cyan-400
                                       hover:border-cyan-500/20
                                       transition-all
                                       text-xs">
                            <Lock className="w-3 h-3"/>
                            Update
                            <ChevronRight className="w-3 h-3"/>
                        </button>
                    }
                />
                <SettingsRow
                    label="Active Sessions"
                    description="You are currently logged in"
                    action={
                        <span className="flex items-center
                                         gap-1.5 text-xs
                                         text-green-400">
                            <div className="w-2 h-2
                                            rounded-full
                                            bg-green-400
                                            animate-pulse"/>
                            1 Active
                        </span>
                    }
                />
                <SettingsRow
                    label="Download My Data"
                    description="Export your account data and reports"
                    action={
                        <button
                            onClick={handleExportData}
                            disabled={isExporting}
                            className="flex items-center
                                       gap-2 px-4 py-2
                                       rounded-lg
                                       bg-white/5
                                       border border-white/10
                                       text-slate-300
                                       hover:text-cyan-400
                                       hover:border-cyan-500/20
                                       transition-all
                                       text-xs disabled:opacity-60">
                            <Download className="w-3 h-3"/>
                            {isExporting ? 'Exporting...' : 'Export'}
                        </button>
                    }
                />
            </SettingsSection>

            {/* Account Actions */}
            <SettingsSection
                title="Account Actions"
                icon={SettingsIcon}>

                <SettingsRow
                    label="Sign Out"
                    description="Sign out from your current session"
                    action={
                        <button
                            onClick={handleLogout}
                            className="flex items-center
                                       gap-2 px-4 py-2
                                       rounded-lg
                                       bg-white/5
                                       border border-white/10
                                       text-slate-300
                                       hover:text-red-400
                                       hover:border-red-500/20
                                       transition-all
                                       text-xs">
                            <LogOut className="w-3 h-3"/>
                            Sign Out
                        </button>
                    }
                />
                <SettingsRow
                    label="Delete Account"
                    description="Permanently delete your account and all data"
                    danger={true}
                    action={
                        <button
                            onClick={() =>
                                setShowDeleteConfirm(true)
                            }
                            className="flex items-center
                                       gap-2 px-4 py-2
                                       rounded-lg
                                       bg-red-500/10
                                       border
                                       border-red-500/20
                                       text-red-400
                                       hover:bg-red-500/20
                                       transition-all
                                       text-xs">
                            <Trash2 className="w-3 h-3"/>
                            Delete
                        </button>
                    }
                />
            </SettingsSection>

            {/* App Info */}
            <div className="glass-card p-5
                            border border-white/5">
                <div className="flex items-center
                                justify-between">
                    <div>
                        <p className="text-xs font-medium
                                      text-slate-400">
                            CyberShield
                        </p>
                        <p className="text-xs text-slate-600">
                            Version 1.0.0 •
                            AI-Driven Threat Monitoring
                        </p>
                    </div>
                    <div className="flex items-center
                                    gap-1.5">
                        <div className="w-2 h-2 rounded-full
                                        bg-green-400
                                        animate-pulse"/>
                        <span className="text-xs
                                         text-green-400">
                            Online
                        </span>
                    </div>
                </div>
            </div>

            {/* Delete Confirmation Modal */}
            {showDeleteConfirm && (
                <div className="fixed inset-0 z-50
                                flex items-center
                                justify-center p-4"
                     style={{
                         background: 'rgba(0,0,0,0.7)',
                         backdropFilter: 'blur(4px)',
                     }}>
                    <div className="glass-card w-full
                                    max-w-sm p-6
                                    border border-red-500/20">
                        <div className="text-center">
                            <div className="w-14 h-14
                                            rounded-2xl
                                            bg-red-500/10
                                            border
                                            border-red-500/20
                                            flex items-center
                                            justify-center
                                            mx-auto mb-4">
                                <Trash2 className="w-7 h-7
                                                    text-red-400"/>
                            </div>
                            <h3 className="text-lg font-bold
                                           text-white mb-2">
                                Delete Account?
                            </h3>
                            <p className="text-sm
                                          text-slate-400 mb-6">
                                This action is permanent
                                and cannot be undone.
                                All your reports and data
                                will be deleted.
                            </p>
                            <div className="flex gap-3">
                                <button
                                    onClick={() =>
                                        setShowDeleteConfirm(
                                            false
                                        )
                                    }
                                    className="flex-1 py-2.5
                                               rounded-xl
                                               bg-white/5
                                               border
                                               border-white/10
                                               text-slate-300
                                               hover:bg-white/10
                                               transition-all
                                               text-sm">
                                    Cancel
                                </button>
                                <button
                                    onClick={handleDeleteAccount}
                                    disabled={isDeleting}
                                    className="flex-1
                                               btn-danger disabled:opacity-60
                                               py-2.5 text-sm">
                                    {isDeleting ? 'Deleting...' : 'Delete'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Settings;
