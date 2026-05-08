import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Bell,
    Search,
    Settings,
    LogOut,
    User,
    Shield,
    ChevronDown,
    Menu,
    AlertTriangle,
    Info,
    CheckCircle,
    Clock,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { getInitials, timeAgo } from '../../utils/helpers';
import alertService from '../../services/alertService';
import reportService from '../../services/reportService';

// ==========================================
// NOTIFICATION ICON HELPER
// ==========================================
const NotificationIcon = ({ type }) => {
    switch (type) {
        case 'danger':
            return <AlertTriangle className="w-4 h-4 text-red-400"/>;
        case 'warning':
            return <Clock className="w-4 h-4 text-yellow-400"/>;
        case 'success':
            return <CheckCircle className="w-4 h-4 text-green-400"/>;
        default:
            return <Info className="w-4 h-4 text-cyan-400"/>;
    }
};

// ==========================================
// ✅ ISSUE 2: SHARED DARK DROPDOWN STYLE
// Replaces glass-card (light/transparent) with
// a solid dark panel — fully readable content
// ==========================================
const darkPanel = {
    background:   '#0d1420',
    border:       '1px solid rgba(255,255,255,0.1)',
    borderRadius: '12px',
    boxShadow:    '0 20px 60px rgba(0,0,0,0.7), 0 0 0 1px rgba(0,212,255,0.06)',
};

// ==========================================
// NAVBAR COMPONENT
// ==========================================
const Navbar = ({ isCollapsed, setIsCollapsed }) => {

    const navigate = useNavigate();
    const { user, logout, isAdmin } = useAuth();

    const [showNotifications, setShowNotifications] = useState(false);
    const [showProfile,       setShowProfile]       = useState(false);
    const [searchQuery,       setSearchQuery]       = useState('');
    const [notifications,     setNotifications]     = useState([]);

    const notifRef   = useRef(null);
    const profileRef = useRef(null);

    const unreadCount = notifications.filter(n => !n.read).length;

    // ==========================================
    // CLOSE DROPDOWNS ON OUTSIDE CLICK
    // ==========================================
    useEffect(() => {
        const handleClickOutside = (e) => {
            if (notifRef.current   && !notifRef.current.contains(e.target))
                setShowNotifications(false);
            if (profileRef.current && !profileRef.current.contains(e.target))
                setShowProfile(false);
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    useEffect(() => {
        let mounted = true;

        const toNotifType = (severityOrRisk) => {
            const value = String(severityOrRisk || '').toUpperCase();
            if (value === 'CRITICAL' || value === 'HIGH') return 'danger';
            if (value === 'MEDIUM' || value === 'PENDING' || value === 'UNDER_REVIEW') return 'warning';
            if (value === 'VERIFIED' || value === 'ACTIVE') return 'success';
            return 'info';
        };

        const fetchNotifications = async () => {
            if (!user) return;
            try {
                const items = [];
                if (isAdmin()) {
                    const [reportsRes, alertsRes] = await Promise.all([
                        reportService.getRecentReports(),
                        alertService.getActiveAlerts(),
                    ]);
                    const reports = reportsRes?.reports || [];
                    const alerts = alertsRes?.alerts || [];

                    reports.slice(0, 4).forEach((report) => {
                        items.push({
                            id: `report-${report.id}`,
                            type: toNotifType(report.riskLevel || report.status),
                            title: report.riskLevel === 'HIGH' || report.riskLevel === 'CRITICAL'
                                ? 'High Risk Threat Detected'
                                : 'Report Update',
                            message: report.title || 'New report submitted',
                            time: report.createdAt ? new Date(report.createdAt) : new Date(),
                            read: false,
                        });
                    });

                    alerts.slice(0, 4).forEach((alert) => {
                        items.push({
                            id: `alert-${alert.id}`,
                            type: toNotifType(alert.severity || alert.status),
                            title: 'Alert Published',
                            message: alert.title || 'Security alert published',
                            time: alert.createdAt ? new Date(alert.createdAt) : new Date(),
                            read: false,
                        });
                    });
                } else {
                    const [reportsRes, alertsRes] = await Promise.all([
                        reportService.getMyReports(),
                        alertService.getMyInstitutionAlerts(),
                    ]);
                    const reports = reportsRes?.reports || [];
                    const alerts = alertsRes?.alerts || [];

                    alerts.slice(0, 4).forEach((alert) => {
                        items.push({
                            id: `student-alert-${alert.id}`,
                            type: toNotifType(alert.severity || alert.status),
                            title: alert.title || 'Security Alert',
                            message: alert.message || 'New institutional alert',
                            time: alert.createdAt ? new Date(alert.createdAt) : new Date(),
                            read: false,
                        });
                    });

                    reports
                        .filter((report) => report.status === 'VERIFIED' || report.status === 'DISMISSED' || report.status === 'UNDER_REVIEW')
                        .slice(0, 4)
                        .forEach((report) => {
                            items.push({
                                id: `student-report-${report.id}`,
                                type: toNotifType(report.status),
                                title: 'Report Status Updated',
                                message: `${report.title || 'Threat report'} is ${report.status}`,
                                time: report.updatedAt ? new Date(report.updatedAt) : new Date(report.createdAt || Date.now()),
                                read: false,
                            });
                        });
                }

                items.sort((a, b) => b.time - a.time);
                if (mounted) {
                    setNotifications(items.slice(0, 12));
                }
            } catch (error) {
                console.error('Failed to fetch notifications', error);
            }
        };

        fetchNotifications();
        const intervalId = setInterval(fetchNotifications, 60000);
        return () => {
            mounted = false;
            clearInterval(intervalId);
        };
    }, [user, isAdmin]);

    const markAllRead = () =>
        setNotifications(notifications.map(n => ({ ...n, read: true })));

    const markAsRead = (id) =>
        setNotifications(notifications.map(n =>
            n.id === id ? { ...n, read: true } : n
        ));

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const handleProfileNav = (path) => {
        setShowProfile(false);
        navigate(path);
    };

    // ==========================================
    // RENDER
    // ==========================================
    return (
        <div className={`navbar fixed top-0 right-0 z-30
                         h-16 flex items-center
                         justify-between px-6
                         transition-all duration-300
                         ${isCollapsed ? 'left-16' : 'left-64'}`}>

            {/* ── Left Side ── */}
            <div className="flex items-center gap-4">

                {/* Mobile Menu Toggle */}
                <button
                    onClick={() => setIsCollapsed(!isCollapsed)}
                    className="text-slate-400 hover:text-cyan-400
                               transition-colors lg:hidden">
                    <Menu className="w-5 h-5"/>
                </button>

                {/* ✅ ISSUE 3: Search — inline paddingLeft so icon never overlaps */}
                <div className="relative hidden md:block">
                    <Search className="absolute left-3 top-1/2
                                       -translate-y-1/2 w-4 h-4
                                       text-slate-500 pointer-events-none z-10"/>
                    <input
                        type="text"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        placeholder="Search threats, alerts..."
                        className="cyber-input w-64 h-9 text-sm"
                        style={{ paddingLeft: '2.25rem' }}
                    />
                </div>
            </div>

            {/* ── Right Side ── */}
            <div className="flex items-center gap-3">

                {/* System Status */}
                <div className="hidden md:flex items-center gap-2
                                px-3 py-1.5 rounded-lg
                                bg-green-500/10 border border-green-500/20">
                    <div className="w-2 h-2 rounded-full bg-green-400 animate-pulse"/>
                    <span className="text-xs text-green-400 font-medium">
                        System Online
                    </span>
                </div>

                {/* ── Notification Bell ── */}
                <div className="relative" ref={notifRef}>
                    <button
                        onClick={() => {
                            setShowNotifications(!showNotifications);
                            setShowProfile(false);
                        }}
                        className="relative w-9 h-9 rounded-xl
                                   flex items-center justify-center
                                   text-slate-400 hover:text-cyan-400
                                   hover:bg-white/5 transition-all">
                        <Bell className="w-5 h-5"/>
                        {unreadCount > 0 && (
                            <span className="absolute -top-0.5 -right-0.5
                                             w-4 h-4 rounded-full bg-red-500
                                             text-white text-xs
                                             flex items-center justify-center
                                             font-bold">
                                {unreadCount}
                            </span>
                        )}
                    </button>

                    {/* ✅ ISSUE 2: Notification Dropdown — DARK panel */}
                    {showNotifications && (
                        <div
                            className="absolute right-0 top-12 w-80
                                       overflow-hidden z-50"
                            style={darkPanel}>

                            {/* Header */}
                            <div className="flex items-center justify-between
                                            p-4 border-b border-white/8">
                                <h3 className="text-sm font-semibold text-white">
                                    Notifications
                                    {unreadCount > 0 && (
                                        <span className="ml-2 px-1.5 py-0.5
                                                         rounded-full
                                                         bg-red-500/20
                                                         text-red-400 text-xs">
                                            {unreadCount}
                                        </span>
                                    )}
                                </h3>
                                {unreadCount > 0 && (
                                    <button
                                        onClick={markAllRead}
                                        className="text-xs text-cyan-400
                                                   hover:text-cyan-300
                                                   transition-colors">
                                        Mark all read
                                    </button>
                                )}
                            </div>

                            {/* Notification List */}
                            <div className="max-h-72 overflow-y-auto">
                                {notifications.length === 0 ? (
                                    <div className="p-6 text-center">
                                        <Bell className="w-8 h-8 text-slate-600
                                                          mx-auto mb-2"/>
                                        <p className="text-sm text-slate-500">
                                            No notifications
                                        </p>
                                    </div>
                                ) : (
                                    notifications.map((notif) => (
                                        <div
                                            key={notif.id}
                                            onClick={() => markAsRead(notif.id)}
                                            className="flex gap-3 p-4 cursor-pointer
                                                       transition-colors
                                                       border-b border-white/5"
                                            style={{
                                                background: !notif.read
                                                    ? 'rgba(0,212,255,0.04)'
                                                    : 'transparent'
                                            }}
                                            onMouseEnter={e =>
                                                e.currentTarget.style.background =
                                                    'rgba(255,255,255,0.04)'
                                            }
                                            onMouseLeave={e =>
                                                e.currentTarget.style.background =
                                                    !notif.read
                                                        ? 'rgba(0,212,255,0.04)'
                                                        : 'transparent'
                                            }>

                                            {/* Icon */}
                                            <div className="w-8 h-8 rounded-lg
                                                            flex-shrink-0
                                                            flex items-center
                                                            justify-center
                                                            bg-white/5">
                                                <NotificationIcon type={notif.type}/>
                                            </div>

                                            {/* Content */}
                                            <div className="flex-1 min-w-0">
                                                <div className="flex items-center
                                                                justify-between">
                                                    <p className={`text-xs font-semibold
                                                                   truncate ${
                                                        !notif.read
                                                            ? 'text-white'
                                                            : 'text-slate-300'
                                                    }`}>
                                                        {notif.title}
                                                    </p>
                                                    {!notif.read && (
                                                        <div className="w-2 h-2 rounded-full
                                                                        bg-cyan-400
                                                                        flex-shrink-0 ml-2"/>
                                                    )}
                                                </div>
                                                <p className="text-xs text-slate-500
                                                              mt-0.5 line-clamp-1">
                                                    {notif.message}
                                                </p>
                                                <p className="text-xs text-slate-600 mt-1">
                                                    {timeAgo(notif.time)}
                                                </p>
                                            </div>
                                        </div>
                                    ))
                                )}
                            </div>

                            {/* Footer */}
                            <div className="p-3 border-t border-white/8 text-center">
                                <button className="text-xs text-cyan-400
                                                   hover:text-cyan-300
                                                   transition-colors">
                                    View all notifications
                                </button>
                            </div>
                        </div>
                    )}
                </div>

                {/* ── Profile Dropdown ── */}
                <div className="relative" ref={profileRef}>
                    <button
                        onClick={() => {
                            setShowProfile(!showProfile);
                            setShowNotifications(false);
                        }}
                        className="flex items-center gap-2 px-3 py-2
                                   rounded-xl hover:bg-white/5 transition-all">

                        {/* Avatar */}
                        <div className="w-8 h-8 rounded-xl
                                        bg-gradient-to-br
                                        from-cyan-500/40 to-blue-500/40
                                        border border-cyan-500/30
                                        flex items-center justify-center">
                            <span className="text-xs font-bold text-cyan-400">
                                {getInitials(user?.fullName || 'U')}
                            </span>
                        </div>

                        {/* Name */}
                        <div className="hidden md:block text-left">
                            <p className="text-xs font-medium text-white">
                                {user?.fullName || 'User'}
                            </p>
                            <p className="text-xs text-slate-500">
                                {user?.role || 'STUDENT'}
                            </p>
                        </div>

                        <ChevronDown className="w-3 h-3 text-slate-400 hidden md:block"/>
                    </button>

                    {/* ✅ ISSUE 2: Profile Dropdown — DARK panel */}
                    {showProfile && (
                        <div
                            className="absolute right-0 top-12 w-56
                                       overflow-hidden z-50"
                            style={darkPanel}>

                            {/* User Info */}
                            <div className="p-4 border-b border-white/8">
                                <p className="text-sm font-medium text-white">
                                    {user?.fullName}
                                </p>
                                <p className="text-xs text-slate-400 mt-0.5">
                                    {user?.email}
                                </p>
                                <div className="mt-2 inline-flex items-center
                                                gap-1 px-2 py-0.5 rounded-full
                                                bg-cyan-500/10
                                                border border-cyan-500/20">
                                    <Shield className="w-3 h-3 text-cyan-400"/>
                                    <span className="text-xs text-cyan-400 font-medium">
                                        {user?.role}
                                    </span>
                                </div>
                            </div>

                            {/* Menu Items */}
                            <div className="p-2">
                                <button
                                    onClick={() =>
                                        handleProfileNav(
                                            isAdmin()
                                                ? '/admin/dashboard'
                                                : '/student/profile'
                                        )
                                    }
                                    className="w-full flex items-center gap-3
                                               px-3 py-2 rounded-lg text-sm
                                               text-slate-300
                                               hover:bg-cyan-500/10
                                               hover:text-cyan-400
                                               transition-all">
                                    <User className="w-4 h-4"/>
                                    Profile
                                </button>

                                <button
                                    onClick={() =>
                                        handleProfileNav(
                                            isAdmin()
                                                ? '/admin/dashboard'
                                                : '/student/settings'
                                        )
                                    }
                                    className="w-full flex items-center gap-3
                                               px-3 py-2 rounded-lg text-sm
                                               text-slate-300
                                               hover:bg-cyan-500/10
                                               hover:text-cyan-400
                                               transition-all">
                                    <Settings className="w-4 h-4"/>
                                    Settings
                                </button>
                            </div>

                            {/* Logout */}
                            <div className="p-2 border-t border-white/8">
                                <button
                                    onClick={handleLogout}
                                    className="w-full flex items-center gap-3
                                               px-3 py-2 rounded-lg text-sm
                                               text-red-400
                                               hover:bg-red-500/10
                                               transition-all">
                                    <LogOut className="w-4 h-4"/>
                                    Logout
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Navbar;
