import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Bell,
    Search,
    AlertTriangle,
    Shield,
    RefreshCw,
    X,
    Calendar,
    Globe,
    Lock,
    ArrowLeft,
    Info,
    Zap,
    ShieldAlert,
    BookOpen,
} from 'lucide-react';
import alertService from '../../services/alertService';
import reportService from '../../services/reportService';
import { useAuth }  from '../../context/AuthContext';
import {
    formatDateTime,
    timeAgo,
    truncateText,
    getSeverityColor,
    extractUrls,
    normalizeUrl,
} from '../../utils/helpers';

const renderLinkedText = (text) => {
    const value = String(text || '');
    const parts = value.split(/((?:https?:\/\/|www\.)[^\s<>"')]+|[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,})/gi);
    return parts.map((part, idx) => {
        const matches = extractUrls(part);
        const emailMatch = String(part || '').trim().match(/^[A-Z0-9._%+-]+@([A-Z0-9.-]+\.[A-Z]{2,})$/i);
        if (!matches.length && !emailMatch) {
            return <React.Fragment key={idx}>{part}</React.Fragment>;
        }
        const url = matches.length ? matches[0] : emailMatch[1];
        const href = normalizeUrl(url);
        return (
            <a
                key={`${url}-${idx}`}
                href={href}
                target="_blank"
                rel="noreferrer"
                onClick={(event) => reportService.handleLinkClick(event, url)}
                className="underline text-cyan-300 break-all"
            >
                {part}
            </a>
        );
    });
};

// ==========================================
// ALERT DETAIL MODAL
// ==========================================
const AlertDetailModal = ({ alert, onClose }) => {

    const color = getSeverityColor(alert.severity);

    const getAlertIcon = (type) => {
        switch (type) {
            case 'PHISHING_ALERT':    return '🎣';
            case 'MALWARE_ALERT':     return '🦠';
            case 'RANSOMWARE_ALERT':  return '🔒';
            case 'DATA_BREACH_ALERT': return '💾';
            case 'THREAT_WARNING':    return '⚠️';
            case 'AWARENESS_TIP':     return '📚';
            default:                  return '🔔';
        }
    };

    return (
        <div className="fixed inset-0 z-50
                        flex items-center justify-center
                        p-4"
             style={{
                 background: 'rgba(0,0,0,0.7)',
                 backdropFilter: 'blur(4px)'
             }}>
            <div className="glass-card w-full max-w-lg
                            max-h-[90vh] overflow-y-auto
                            border"
                 style={{
                     borderColor: `${color}30`
                 }}>

                {/* Header */}
                <div className="flex items-center
                                justify-between p-6
                                border-b border-white/5">
                    <div className="flex items-center gap-3">
                        <div className="w-12 h-12 rounded-xl
                                        flex items-center
                                        justify-center
                                        text-2xl"
                             style={{
                                 background: `${color}15`,
                                 border: `1px solid ${color}30`,
                             }}>
                            {getAlertIcon(alert.alertType)}
                        </div>
                        <div>
                            <h2 className="text-base font-bold
                                           text-white">
                                Security Alert
                            </h2>
                            <p className="text-xs
                                          text-slate-400">
                                #{alert.id} •{' '}
                                {timeAgo(alert.createdAt)}
                            </p>
                        </div>
                    </div>
                    <button
                        onClick={onClose}
                        className="w-8 h-8 rounded-lg
                                   flex items-center
                                   justify-center
                                   text-slate-400
                                   hover:text-white
                                   hover:bg-white/5
                                   transition-all">
                        <X className="w-4 h-4"/>
                    </button>
                </div>

                {/* Body */}
                <div className="p-6 space-y-5">

                    {/* Title */}
                    <h3 className="text-lg font-bold
                                   text-white">
                        {alert.title}
                    </h3>

                    {/* Severity Badge */}
                    <div className="flex items-center
                                    gap-2 flex-wrap">
                        <span className="px-3 py-1
                                         rounded-full
                                         text-xs font-bold"
                              style={{
                                  color,
                                  background: `${color}20`,
                                  border: `1px solid ${color}40`,
                              }}>
                            {alert.severity} SEVERITY
                        </span>
                        <span className="badge-active">
                            {alert.alertType?.replace(
                                /_/g, ' '
                            )}
                        </span>
                        {alert.status === 'ACTIVE' && (
                            <span className="flex items-center
                                             gap-1 px-2 py-1
                                             rounded-full
                                             bg-green-500/10
                                             border
                                             border-green-500/20
                                             text-xs
                                             text-green-400">
                                <div className="w-1.5 h-1.5
                                                rounded-full
                                                bg-green-400
                                                animate-pulse"/>
                                Active
                            </span>
                        )}
                    </div>

                    {/* Message */}
                    <div className="p-4 rounded-xl
                                    border"
                         style={{
                             background: `${color}05`,
                             borderColor: `${color}20`,
                         }}>
                        <p className="text-xs font-medium
                                      uppercase tracking-wider
                                      mb-2"
                           style={{ color }}>
                            Alert Message
                        </p>
                        <p className="text-sm text-slate-300
                                      leading-relaxed whitespace-pre-line">
                            {renderLinkedText(alert.message)}
                        </p>
                    </div>

                    {/* Details Grid */}
                    <div className="grid grid-cols-2 gap-3">
                        <div className="p-3 rounded-xl
                                        bg-white/3
                                        border border-white/5">
                            <p className="text-xs
                                          text-slate-500 mb-1">
                                Published
                            </p>
                            <p className="text-xs font-medium
                                          text-white">
                                {formatDateTime(
                                    alert.createdAt
                                )}
                            </p>
                        </div>
                        <div className="p-3 rounded-xl
                                        bg-white/3
                                        border border-white/5">
                            <p className="text-xs
                                          text-slate-500 mb-1">
                                Target
                            </p>
                            <p className="text-xs font-medium
                                          text-white flex
                                          items-center gap-1">
                                {alert.isPublic
                                    ? <><Globe className="w-3 h-3"/>
                                        All Institutions</>
                                    : <><Lock className="w-3 h-3"/>
                                        {alert.targetInstitution}</>
                                }
                            </p>
                        </div>
                        {alert.expiresAt && (
                            <div className="p-3 rounded-xl
                                            bg-white/3
                                            border border-white/5
                                            col-span-2">
                                <p className="text-xs
                                              text-slate-500 mb-1">
                                    Expires
                                </p>
                                <p className="text-xs font-medium
                                              text-white">
                                    {formatDateTime(
                                        alert.expiresAt
                                    )}
                                </p>
                            </div>
                        )}
                    </div>

                    {/* Safety Tips */}
                    <div className="p-4 rounded-xl
                                    bg-cyan-500/5
                                    border border-cyan-500/20">
                        <p className="text-xs font-medium
                                      text-cyan-400
                                      uppercase tracking-wider
                                      mb-3">
                            🛡️ Stay Safe
                        </p>
                        <ul className="space-y-2">
                            <li className="text-xs
                                           text-slate-400
                                           flex items-start
                                           gap-2">
                                <span className="text-cyan-400
                                                 mt-0.5">•</span>
                                Do not click suspicious links
                                or download unknown files
                            </li>
                            <li className="text-xs
                                           text-slate-400
                                           flex items-start
                                           gap-2">
                                <span className="text-cyan-400
                                                 mt-0.5">•</span>
                                Report any related incidents
                                immediately
                            </li>
                            <li className="text-xs
                                           text-slate-400
                                           flex items-start
                                           gap-2">
                                <span className="text-cyan-400
                                                 mt-0.5">•</span>
                                Contact your IT department
                                if you are affected
                            </li>
                        </ul>
                    </div>

                    {/* Close Button */}
                    <button
                        onClick={onClose}
                        className="w-full py-3 rounded-xl
                                   bg-white/5
                                   border border-white/10
                                   text-slate-300
                                   hover:bg-white/10
                                   transition-all text-sm
                                   font-medium">
                        Close
                    </button>
                </div>
            </div>
        </div>
    );
};

// ==========================================
// ALERT CARD COMPONENT
// ==========================================
const AlertCard = ({ alert, onClick }) => {

    const color = getSeverityColor(alert.severity);

    const getAlertIcon = (type) => {
        switch (type) {
            case 'PHISHING_ALERT':    return '🎣';
            case 'MALWARE_ALERT':     return '🦠';
            case 'RANSOMWARE_ALERT':  return '🔒';
            case 'DATA_BREACH_ALERT': return '💾';
            case 'THREAT_WARNING':    return '⚠️';
            case 'AWARENESS_TIP':     return '📚';
            default:                  return '🔔';
        }
    };

    return (
        <div
            onClick={onClick}
            className="glass-card p-5 cursor-pointer
                       hover:scale-[1.01]
                       transition-all duration-200
                       border"
            style={{
                borderColor: `${color}20`,
            }}>

            {/* Header */}
            <div className="flex items-start
                            justify-between mb-3">
                <div className="flex items-start gap-3
                                flex-1 min-w-0">
                    <div className="w-10 h-10 rounded-xl
                                    flex items-center
                                    justify-center
                                    text-xl flex-shrink-0"
                         style={{
                             background: `${color}15`,
                             border: `1px solid ${color}25`,
                         }}>
                        {getAlertIcon(alert.alertType)}
                    </div>
                    <div className="min-w-0">
                        <p className="text-sm font-semibold
                                      text-white truncate
                                      mb-0.5">
                            {alert.title}
                        </p>
                        <div className="flex items-center
                                        gap-2">
                            <span className="text-xs"
                                  style={{ color }}>
                                ● {alert.severity}
                            </span>
                            <span className="text-xs
                                             text-slate-500">
                                {timeAgo(alert.createdAt)}
                            </span>
                        </div>
                    </div>
                </div>

                {/* Active Badge */}
                {alert.status === 'ACTIVE' && (
                    <div className="flex items-center gap-1
                                    px-2 py-1 rounded-full
                                    bg-green-500/10
                                    border border-green-500/20
                                    flex-shrink-0 ml-2">
                        <div className="w-1.5 h-1.5
                                        rounded-full
                                        bg-green-400
                                        animate-pulse"/>
                        <span className="text-xs
                                         text-green-400
                                         font-medium">
                            LIVE
                        </span>
                    </div>
                )}
            </div>

            {/* Message */}
            <p className="text-xs text-slate-400
                          leading-relaxed mb-4">
                {renderLinkedText(truncateText(alert.message, 100))}
            </p>

            {/* Footer */}
            <div className="flex items-center
                            justify-between">
                <div className="flex items-center gap-2
                                text-xs text-slate-500">
                    {alert.isPublic ? (
                        <div className="flex items-center
                                        gap-1">
                            <Globe className="w-3 h-3"/>
                            <span>All Students</span>
                        </div>
                    ) : (
                        <div className="flex items-center
                                        gap-1">
                            <Lock className="w-3 h-3"/>
                            <span>
                                {alert.targetInstitution}
                            </span>
                        </div>
                    )}
                </div>
                <span className="text-xs font-medium
                                 px-2 py-1 rounded-full"
                      style={{
                          color,
                          background: `${color}15`,
                          border: `1px solid ${color}25`,
                      }}>
                    {alert.alertType?.replace(/_/g, ' ')}
                </span>
            </div>
        </div>
    );
};

// ==========================================
// CYBER ALERTS PAGE
// ==========================================
const CyberAlerts = () => {

    const navigate      = useNavigate();
    const { user }      = useAuth();

    const [alerts,
           setAlerts]   = useState([]);
    const [filtered,
           setFiltered] = useState([]);
    const [isLoading,
           setIsLoading]= useState(true);
    const [searchQuery,
           setSearchQuery] = useState('');
    const [severityFilter,
           setSeverityFilter] = useState('ALL');
    const [activeTab,
           setActiveTab]    = useState('all');
    const [selectedAlert,
           setSelectedAlert] = useState(null);

    // ==========================================
    // FETCH ALERTS
    // ==========================================
    const fetchAlerts = async () => {
        setIsLoading(true);
        try {
            const response = await alertService
                .getMyInstitutionAlerts();
            if (response.success) {
                const activeAlerts = (response.alerts || [])
                    .filter(alert => alert.status === 'ACTIVE');
                setAlerts(activeAlerts);
                setFiltered(activeAlerts);
            }
        } catch (error) {
            console.error('Error:', error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchAlerts();
    }, []);

    // ==========================================
    // FILTER
    // ==========================================
    useEffect(() => {
        let result = [...alerts];

        if (searchQuery) {
            result = result.filter(a =>
                a.title?.toLowerCase()
                 .includes(searchQuery.toLowerCase()) ||
                a.message?.toLowerCase()
                 .includes(searchQuery.toLowerCase())
            );
        }

        if (severityFilter !== 'ALL') {
            result = result.filter(
                a => a.severity === severityFilter
            );
        }

        if (activeTab === 'critical') {
            result = result.filter(
                a => a.severity === 'CRITICAL'
                  || a.severity === 'HIGH'
            );
        } else if (activeTab === 'awareness') {
            result = result.filter(
                a => a.alertType === 'AWARENESS_TIP'
                  || a.alertType === 'GENERAL_NOTICE'
            );
        }

        setFiltered(result);
    }, [
        searchQuery,
        severityFilter,
        alerts,
        activeTab,
    ]);

    // ==========================================
    // STATS
    // ==========================================
    const stats = {
        total:    alerts.length,
        critical: alerts.filter(
                      a => a.severity === 'CRITICAL'
                  ).length,
        high:     alerts.filter(
                      a => a.severity === 'HIGH'
                  ).length,
        awareness:alerts.filter(
                      a => a.alertType === 'AWARENESS_TIP'
                  ).length,
    };

    // ==========================================
    // RENDER
    // ==========================================
    return (
        <div className="space-y-6 animate-fade-in">

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
                            Cyber Alerts
                        </h1>
                        <p className="text-slate-400
                                      text-sm mt-1">
                            Security alerts for{' '}
                            {user?.institutionName
                                || 'your institution'}
                        </p>
                    </div>
                </div>
                <button
                    onClick={fetchAlerts}
                    className="flex items-center gap-2
                               px-4 py-2 rounded-xl
                               bg-white/5
                               border border-white/10
                               text-slate-300
                               hover:text-cyan-400
                               transition-all text-sm">
                    <RefreshCw className="w-4 h-4"/>
                    Refresh
                </button>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-2
                            lg:grid-cols-4 gap-4">
                {[
                    {
                        label: 'Total Alerts',
                        value: stats.total,
                        icon:  Bell,
                        color: 'text-cyan-400',
                        bg:    'bg-cyan-500/10',
                        border:'border-cyan-500/10',
                    },
                    {
                        label: 'Critical',
                        value: stats.critical,
                        icon:  Zap,
                        color: 'text-purple-400',
                        bg:    'bg-purple-500/10',
                        border:'border-purple-500/10',
                    },
                    {
                        label: 'High Severity',
                        value: stats.high,
                        icon:  AlertTriangle,
                        color: 'text-red-400',
                        bg:    'bg-red-500/10',
                        border:'border-red-500/10',
                    },
                    {
                        label: 'Awareness Tips',
                        value: stats.awareness,
                        icon:  BookOpen,
                        color: 'text-green-400',
                        bg:    'bg-green-500/10',
                        border:'border-green-500/10',
                    },
                ].map((stat, i) => (
                    <div key={i}
                         className={`glass-card p-4
                                     border ${stat.border}`}>
                        <div className="flex items-center
                                        gap-3">
                            <div className={`w-9 h-9
                                             rounded-xl
                                             flex items-center
                                             justify-center
                                             ${stat.bg}`}>
                                <stat.icon
                                    className={`w-4 h-4
                                                ${stat.color}`}
                                />
                            </div>
                            <div>
                                <p className={`text-xl
                                               font-bold
                                               ${stat.color}`}>
                                    {stat.value}
                                </p>
                                <p className="text-xs
                                              text-slate-400">
                                    {stat.label}
                                </p>
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {/* Tabs */}
            <div className="flex items-center gap-1
                            p-1 rounded-xl bg-white/3
                            border border-white/5 w-fit">
                {[
                    { key: 'all',      label: 'All Alerts' },
                    { key: 'critical', label: '⚡ Critical' },
                    { key: 'awareness',label: '📚 Awareness' },
                ].map(tab => (
                    <button
                        key={tab.key}
                        onClick={() =>
                            setActiveTab(tab.key)
                        }
                        className={`px-4 py-2 rounded-lg
                                    text-sm font-medium
                                    transition-all
                                    ${activeTab === tab.key
                                        ? 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/30'
                                        : 'text-slate-400 hover:text-slate-200'
                                    }`}>
                        {tab.label}
                    </button>
                ))}
            </div>

            {/* Filters */}
            <div className="glass-card p-4">
                <div className="flex flex-wrap
                                items-center gap-3">
                    <div className="relative flex-1
                                    min-w-48">
                        <Search className="absolute left-3
                                           top-1/2
                                           -translate-y-1/2
                                           w-4 h-4
                                           text-slate-400"/>
                        <input
                            type="text"
                            value={searchQuery}
                            onChange={(e) =>
                                setSearchQuery(e.target.value)
                            }
                            placeholder="Search alerts..."
                            className="cyber-input pl-9
                                       h-9 text-sm"
                        />
                    </div>
                    <select
                        value={severityFilter}
                        onChange={(e) =>
                            setSeverityFilter(e.target.value)
                        }
                        className="cyber-input h-9
                                   text-sm w-36
                                   cursor-pointer">
                        <option value="ALL">
                            All Severity
                        </option>
                        <option value="LOW">Low</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="HIGH">High</option>
                        <option value="CRITICAL">
                            Critical
                        </option>
                    </select>
                    <p className="text-xs text-slate-400
                                  ml-auto">
                        {filtered.length} alerts
                    </p>
                </div>
            </div>

            {/* Alerts Grid */}
            {isLoading ? (
                <div className="flex items-center
                                justify-center py-16">
                    <RefreshCw className="w-6 h-6
                                          text-cyan-400
                                          animate-spin"/>
                </div>
            ) : filtered.length === 0 ? (
                <div className="glass-card p-16
                                text-center">
                    <Shield className="w-12 h-12
                                       text-slate-600
                                       mx-auto mb-3"/>
                    <p className="text-slate-400 text-sm
                                  mb-1">
                        No alerts found
                    </p>
                    <p className="text-slate-600 text-xs">
                        Your institution is currently safe
                    </p>
                </div>
            ) : (
                <div className="grid grid-cols-1
                                lg:grid-cols-2 gap-4">
                    {filtered.map(alert => (
                        <AlertCard
                            key={alert.id}
                            alert={alert}
                            onClick={() =>
                                setSelectedAlert(alert)
                            }
                        />
                    ))}
                </div>
            )}

            {/* Alert Detail Modal */}
            {selectedAlert && (
                <AlertDetailModal
                    alert={selectedAlert}
                    onClose={() =>
                        setSelectedAlert(null)
                    }
                />
            )}
        </div>
    );
};

export default CyberAlerts;
