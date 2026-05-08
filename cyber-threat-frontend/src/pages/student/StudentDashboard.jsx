import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Shield,
    ShieldAlert,
    Bell,
    FileText,
    AlertTriangle,
    CheckCircle,
    Clock,
    TrendingUp,
    BookOpen,
    ArrowRight,
    RefreshCw,
    Eye,
} from 'lucide-react';
import alertService  from '../../services/alertService';
import reportService from '../../services/reportService';
import { useAuth }   from '../../context/AuthContext';
import {
    getRiskBadgeClass,
    getStatusBadgeClass,
    getSeverityColor,
    timeAgo,
    truncateText,
    extractUrls,
    normalizeUrl,
} from '../../utils/helpers';

// ==========================================
// AWARENESS TIPS DATA
// ==========================================
const awarenessTips = [
    {
        id:    1,
        icon:  '🔐',
        title: 'Strong Passwords',
        tip:   'Use at least 12 characters with mix of letters, numbers and symbols.',
        color: 'border-cyan-500/20 bg-cyan-500/5',
    },
    {
        id:    2,
        icon:  '📧',
        title: 'Phishing Emails',
        tip:   'Never click suspicious links. Verify sender email before opening attachments.',
        color: 'border-yellow-500/20 bg-yellow-500/5',
    },
    {
        id:    3,
        icon:  '🔗',
        title: 'Suspicious URLs',
        tip:   'Check URLs carefully. Cybercriminals use lookalike domains to steal credentials.',
        color: 'border-red-500/20 bg-red-500/5',
    },
    {
        id:    4,
        icon:  '💾',
        title: 'Data Backup',
        tip:   'Regularly backup important files to prevent data loss from ransomware attacks.',
        color: 'border-green-500/20 bg-green-500/5',
    },
];

// ==========================================
// STAT CARD COMPONENT
// ==========================================
const StatCard = ({
    title,
    value,
    icon: Icon,
    color,
    bgColor,
    borderColor,
    onClick,
}) => (
    <div
        onClick={onClick}
        className={`glass-card p-5 border
                     ${borderColor}
                     ${onClick
                         ? 'cursor-pointer hover:scale-105'
                         : ''
                     }
                     transition-transform duration-200`}>
        <div className="flex items-center gap-4">
            <div className={`w-12 h-12 rounded-xl
                             flex items-center
                             justify-center flex-shrink-0
                             ${bgColor}`}>
                <Icon className={`w-6 h-6 ${color}`}/>
            </div>
            <div>
                <p className="text-2xl font-bold text-white">
                    {value}
                </p>
                <p className="text-xs text-slate-400 mt-0.5">
                    {title}
                </p>
            </div>
        </div>
    </div>
);

// ==========================================
// STUDENT DASHBOARD COMPONENT
// ==========================================
const StudentDashboard = () => {

    const navigate        = useNavigate();
    const { user }        = useAuth();
    const [alerts,
           setAlerts]     = useState([]);
    const [reports,
           setReports]    = useState([]);
    const [isLoading,
           setIsLoading]  = useState(true);

    // ==========================================
    // FETCH DATA
    // ==========================================
    const fetchData = async () => {
        setIsLoading(true);
        try {
            const [alertsRes, reportsRes] =
                await Promise.all([
                    alertService.getMyInstitutionAlerts(),
                    reportService.getMyReports(),
                ]);

            if (alertsRes.success) {
                setAlerts(alertsRes.alerts || []);
            }
            if (reportsRes.success) {
                setReports(reportsRes.reports || []);
            }
        } catch (error) {
            console.error('Dashboard fetch error:', error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    // ==========================================
    // STATS
    // ==========================================
    const stats = {
        totalReports:   reports.length,
        pendingReports: reports.filter(
                            r => r.status === 'PENDING'
                        ).length,
        verifiedThreats:reports.filter(
                            r => r.status === 'VERIFIED'
                        ).length,
        activeAlerts:   alerts.filter(
                            a => a.status === 'ACTIVE'
                        ).length,
    };

    // ==========================================
    // GET SEVERITY BADGE
    // ==========================================
    const getSeverityBadge = (severity) => {
        switch (severity?.toUpperCase()) {
            case 'CRITICAL':
                return 'badge-critical';
            case 'HIGH':
                return 'badge-high';
            case 'MEDIUM':
                return 'badge-medium';
            default:
                return 'badge-low';
        }
    };

    // ==========================================
    // LOADING STATE
    // ==========================================
    if (isLoading) {
        return (
            <div className="flex items-center
                            justify-center min-h-96">
                <div className="flex flex-col
                                items-center gap-4">
                    <RefreshCw className="w-8 h-8
                                          text-cyan-400
                                          animate-spin"/>
                    <p className="text-slate-400 text-sm">
                        Loading dashboard...
                    </p>
                </div>
            </div>
        );
    }

    // ==========================================
    // RENDER
    // ==========================================
    return (
        <div className="space-y-6 animate-fade-in">

            {/* Welcome Banner */}
            <div className="glass-card p-6
                            border border-cyan-500/10
                            relative overflow-hidden">

                {/* Background Glow */}
                <div className="absolute top-0 right-0
                                w-48 h-48 rounded-full
                                bg-cyan-500/5 blur-3xl
                                pointer-events-none"/>

                <div className="relative flex items-center
                                justify-between">
                    <div>
                        <h1 className="text-2xl font-bold
                                       text-white mb-1">
                            Welcome back,{' '}
                            <span className="text-gradient-blue">
                                {user?.fullName
                                    ?.split(' ')[0]
                                    || 'Student'}
                            </span>
                            ! 👋
                        </h1>
                        <p className="text-slate-400 text-sm">
                            Stay safe online. Report any
                            suspicious activity immediately.
                        </p>
                        <p className="text-xs text-slate-500
                                      mt-1">
                            {user?.institutionName
                                || 'Your Institution'}
                        </p>
                    </div>

                    <div className="hidden md:flex
                                    items-center gap-3">
                        <button
                            onClick={() =>
                                navigate(
                                    '/student/report-threat'
                                )
                            }
                            className="btn-cyber flex
                                       items-center gap-2
                                       px-5 py-2.5 text-sm">
                            <ShieldAlert className="w-4 h-4"/>
                            Report Threat
                        </button>
                    </div>
                </div>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-2
                            lg:grid-cols-4 gap-4">
                <StatCard
                    title="My Reports"
                    value={stats.totalReports}
                    icon={FileText}
                    color="text-cyan-400"
                    bgColor="bg-cyan-500/10"
                    borderColor="border-cyan-500/10"
                    onClick={() =>
                        navigate('/student/my-reports')
                    }
                />
                <StatCard
                    title="Pending"
                    value={stats.pendingReports}
                    icon={Clock}
                    color="text-yellow-400"
                    bgColor="bg-yellow-500/10"
                    borderColor="border-yellow-500/10"
                />
                <StatCard
                    title="Verified Threats"
                    value={stats.verifiedThreats}
                    icon={CheckCircle}
                    color="text-green-400"
                    bgColor="bg-green-500/10"
                    borderColor="border-green-500/10"
                />
                <StatCard
                    title="Active Alerts"
                    value={stats.activeAlerts}
                    icon={Bell}
                    color="text-red-400"
                    bgColor="bg-red-500/10"
                    borderColor="border-red-500/10"
                    onClick={() =>
                        navigate('/student/alerts')
                    }
                />
            </div>

            {/* Main Content Grid */}
            <div className="grid grid-cols-1
                            lg:grid-cols-3 gap-6">

                {/* Active Alerts Feed */}
                <div className="lg:col-span-2
                                glass-card p-6">
                    <div className="flex items-center
                                    justify-between mb-5">
                        <div className="flex items-center
                                        gap-2">
                            <Bell className="w-4 h-4
                                             text-red-400"/>
                            <h2 className="text-sm
                                           font-semibold
                                           text-white">
                                Active Security Alerts
                            </h2>
                            {stats.activeAlerts > 0 && (
                                <span className="px-2 py-0.5
                                                 rounded-full
                                                 bg-red-500/20
                                                 text-red-400
                                                 text-xs
                                                 font-medium">
                                    {stats.activeAlerts}
                                </span>
                            )}
                        </div>
                        <button
                            onClick={() =>
                                navigate('/student/alerts')
                            }
                            className="text-xs text-cyan-400
                                       hover:text-cyan-300
                                       flex items-center
                                       gap-1 transition-colors">
                            View all
                            <ArrowRight className="w-3 h-3"/>
                        </button>
                    </div>

                    {alerts.length === 0 ? (
                        <div className="text-center py-10">
                            <Shield className="w-10 h-10
                                               text-slate-600
                                               mx-auto mb-3"/>
                            <p className="text-slate-400
                                          text-sm">
                                No active alerts
                            </p>
                            <p className="text-slate-600
                                          text-xs mt-1">
                                Your institution is safe
                            </p>
                        </div>
                    ) : (
                        <div className="space-y-3">
                            {alerts
                                .slice(0, 4)
                                .map(alert => (
                                <div key={alert.id}
                                     className="p-4 rounded-xl
                                                border
                                                transition-all
                                                hover:bg-white/3
                                                cursor-pointer"
                                     style={{
                                         borderColor:
                                             `${getSeverityColor(
                                                 alert.severity
                                             )}30`,
                                         background:
                                             `${getSeverityColor(
                                                 alert.severity
                                             )}05`,
                                     }}
                                     onClick={() =>
                                         navigate(
                                             '/student/alerts'
                                         )
                                     }>
                                    <div className="flex
                                                    items-start
                                                    justify-between
                                                    mb-2">
                                        <p className="text-sm
                                                      font-semibold
                                                      text-white
                                                      flex-1
                                                      mr-3">
                                            {alert.title}
                                        </p>
                                        <span className={
                                            getSeverityBadge(
                                                alert.severity
                                            )
                                        }>
                                            {alert.severity}
                                        </span>
                                    </div>
                                    <p className="text-xs
                                                  text-slate-400
                                                  leading-relaxed
                                                  mb-2">
                                        {String(truncateText(alert.message, 100))
                                            .split(/((?:https?:\/\/|www\.)[^\s<>"')]+|[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,})/gi)
                                            .map((part, idx) => {
                                                const matches = extractUrls(part);
                                                const emailMatch = String(part || '').trim().match(/^[A-Z0-9._%+-]+@([A-Z0-9.-]+\.[A-Z]{2,})$/i);
                                                if (!matches.length && !emailMatch) {
                                                    return <React.Fragment key={idx}>{part}</React.Fragment>;
                                                }
                                                const url = matches.length ? matches[0] : emailMatch[1];
                                                return (
                                                    <a
                                                        key={`${url}-${idx}`}
                                                        href={normalizeUrl(url)}
                                                        target="_blank"
                                                        rel="noreferrer"
                                                        onClick={(event) => reportService.handleLinkClick(event, url)}
                                                        className="underline text-cyan-300 break-all"
                                                    >
                                                        {part}
                                                    </a>
                                                );
                                            })}
                                    </p>
                                    <p className="text-xs
                                                  text-slate-600">
                                        {timeAgo(
                                            alert.createdAt
                                        )}
                                    </p>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Security Awareness Tips */}
                <div className="glass-card p-6">
                    <div className="flex items-center
                                    gap-2 mb-5">
                        <BookOpen className="w-4 h-4
                                             text-cyan-400"/>
                        <h2 className="text-sm font-semibold
                                       text-white">
                            Security Tips
                        </h2>
                    </div>

                    <div className="space-y-3">
                        {awarenessTips.map(tip => (
                            <div key={tip.id}
                                 className={`p-3 rounded-xl
                                             border
                                             ${tip.color}`}>
                                <div className="flex items-center
                                                gap-2 mb-1">
                                    <span className="text-base">
                                        {tip.icon}
                                    </span>
                                    <p className="text-xs
                                                  font-semibold
                                                  text-white">
                                        {tip.title}
                                    </p>
                                </div>
                                <p className="text-xs
                                              text-slate-400
                                              leading-relaxed">
                                    {tip.tip}
                                </p>
                            </div>
                        ))}
                    </div>

                    <button
                        onClick={() =>
                            navigate('/student/awareness')
                        }
                        className="w-full mt-4 py-2.5
                                   rounded-xl
                                   border border-white/10
                                   text-slate-400
                                   hover:text-cyan-400
                                   hover:border-cyan-500/20
                                   transition-all text-xs
                                   flex items-center
                                   justify-center gap-2">
                        <BookOpen className="w-3 h-3"/>
                        Learn More
                    </button>
                </div>
            </div>

            {/* My Recent Reports */}
            <div className="glass-card p-6">
                <div className="flex items-center
                                justify-between mb-5">
                    <div className="flex items-center gap-2">
                        <FileText className="w-4 h-4
                                             text-cyan-400"/>
                        <h2 className="text-sm font-semibold
                                       text-white">
                            My Recent Reports
                        </h2>
                    </div>
                    <div className="flex items-center gap-3">
                        <button
                            onClick={() =>
                                navigate(
                                    '/student/report-threat'
                                )
                            }
                            className="btn-cyber flex
                                       items-center gap-2
                                       px-4 py-2 text-xs">
                            <ShieldAlert className="w-3 h-3"/>
                            New Report
                        </button>
                        <button
                            onClick={() =>
                                navigate('/student/my-reports')
                            }
                            className="text-xs text-cyan-400
                                       hover:text-cyan-300
                                       flex items-center
                                       gap-1 transition-colors">
                            View all
                            <ArrowRight className="w-3 h-3"/>
                        </button>
                    </div>
                </div>

                {reports.length === 0 ? (
                    <div className="text-center py-10">
                        <ShieldAlert className="w-10 h-10
                                                text-slate-600
                                                mx-auto mb-3"/>
                        <p className="text-slate-400 text-sm">
                            No reports submitted yet
                        </p>
                        <button
                            onClick={() =>
                                navigate(
                                    '/student/report-threat'
                                )
                            }
                            className="btn-cyber px-6 py-2
                                       text-sm mt-4
                                       inline-flex
                                       items-center gap-2">
                            <ShieldAlert className="w-4 h-4"/>
                            Report Your First Threat
                        </button>
                    </div>
                ) : (
                    <div className="space-y-3">
                        {reports
                            .slice(0, 4)
                            .map(report => (
                            <div key={report.id}
                                 className="flex items-center
                                            justify-between
                                            p-4 rounded-xl
                                            bg-white/3
                                            border border-white/5
                                            hover:bg-white/5
                                            transition-colors">
                                <div className="flex items-center
                                                gap-3 flex-1
                                                min-w-0">
                                    <div className={`w-8 h-8
                                                     rounded-lg
                                                     flex items-center
                                                     justify-center
                                                     flex-shrink-0
                                                     ${report.riskLevel === 'HIGH'
                                                         ? 'bg-red-500/10'
                                                         : report.riskLevel === 'MEDIUM'
                                                         ? 'bg-yellow-500/10'
                                                         : 'bg-green-500/10'
                                                     }`}>
                                        <ShieldAlert
                                            className={`w-4 h-4
                                                        ${report.riskLevel === 'HIGH'
                                                            ? 'text-red-400'
                                                            : report.riskLevel === 'MEDIUM'
                                                            ? 'text-yellow-400'
                                                            : 'text-green-400'
                                                        }`}
                                        />
                                    </div>
                                    <div className="min-w-0">
                                        <p className="text-sm
                                                      font-medium
                                                      text-white
                                                      truncate">
                                            {report.title}
                                        </p>
                                        <p className="text-xs
                                                      text-slate-500">
                                            {timeAgo(
                                                report.createdAt
                                            )}
                                        </p>
                                    </div>
                                </div>
                                <div className="flex items-center
                                                gap-2
                                                flex-shrink-0
                                                ml-3">
                                    <span className={
                                        getRiskBadgeClass(
                                            report.riskLevel
                                        )
                                    }>
                                        {report.riskLevel}
                                    </span>
                                    <span className={
                                        getStatusBadgeClass(
                                            report.status
                                        )
                                    }>
                                        {report.status}
                                    </span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Quick Actions */}
            <div className="grid grid-cols-1
                            md:grid-cols-3 gap-4">
                {[
                    {
                        icon:    ShieldAlert,
                        title:   'Report a Threat',
                        desc:    'Submit suspicious emails, URLs or incidents',
                        color:   'text-red-400',
                        bg:      'bg-red-500/10',
                        border:  'border-red-500/20',
                        path:    '/student/report-threat',
                    },
                    {
                        icon:    BookOpen,
                        title:   'Threat Awareness',
                        desc:    'Learn about cybersecurity best practices',
                        color:   'text-cyan-400',
                        bg:      'bg-cyan-500/10',
                        border:  'border-cyan-500/20',
                        path:    '/student/awareness',
                    },
                    {
                        icon:    TrendingUp,
                        title:   'Learning Center',
                        desc:    'Access cybersecurity courses and resources',
                        color:   'text-purple-400',
                        bg:      'bg-purple-500/10',
                        border:  'border-purple-500/20',
                        path:    '/student/learning',
                    },
                ].map((action, i) => (
                    <button
                        key={i}
                        onClick={() =>
                            navigate(action.path)
                        }
                        className={`glass-card p-5
                                    border ${action.border}
                                    text-left
                                    hover:scale-105
                                    transition-transform
                                    duration-200 w-full`}>
                        <div className={`w-10 h-10
                                         rounded-xl
                                         flex items-center
                                         justify-center
                                         mb-3 ${action.bg}`}>
                            <action.icon
                                className={`w-5 h-5
                                            ${action.color}`}
                            />
                        </div>
                        <h3 className="text-sm font-semibold
                                       text-white mb-1">
                            {action.title}
                        </h3>
                        <p className="text-xs text-slate-400
                                      leading-relaxed">
                            {action.desc}
                        </p>
                        <div className={`flex items-center
                                         gap-1 mt-3 text-xs
                                         font-medium
                                         ${action.color}`}>
                            Get started
                            <ArrowRight className="w-3 h-3"/>
                        </div>
                    </button>
                ))}
            </div>
        </div>
    );
};

export default StudentDashboard;
