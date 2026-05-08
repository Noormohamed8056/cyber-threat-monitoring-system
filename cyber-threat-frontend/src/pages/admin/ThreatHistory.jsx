import React, { useState, useEffect } from 'react';
import {
    History,
    Search,
    RefreshCw,
    Shield,
    ShieldAlert,
    Bell,
    User,
    LogIn,
    CheckCircle,
    XCircle,
    Eye,
    AlertTriangle,
    FileText,
    X,
    Calendar,
    Brain,
    Zap,
    Activity,
} from 'lucide-react';
import adminService from '../../services/adminService';
import {
    formatDateTime,
    timeAgo,
    getRiskBadgeClass,
} from '../../utils/helpers';

// ==========================================
// ACTION TYPE CONFIG
// ==========================================
const actionConfig = {
    REPORT_SUBMITTED: {
        icon:  FileText,
        color: 'text-cyan-400',
        bg:    'bg-cyan-500/10',
        label: 'Report Submitted',
    },
    REPORT_VERIFIED: {
        icon:  CheckCircle,
        color: 'text-green-400',
        bg:    'bg-green-500/10',
        label: 'Report Verified',
    },
    REPORT_DISMISSED: {
        icon:  XCircle,
        color: 'text-slate-400',
        bg:    'bg-slate-500/10',
        label: 'Report Dismissed',
    },
    REPORT_UNDER_REVIEW: {
        icon:  Eye,
        color: 'text-blue-400',
        bg:    'bg-blue-500/10',
        label: 'Under Review',
    },
    ADMIN_REVIEW_STARTED: {
        icon:  Eye,
        color: 'text-blue-400',
        bg:    'bg-blue-500/10',
        label: 'Review Started',
    },
    ADMIN_REMARKS_ADDED: {
        icon:  FileText,
        color: 'text-cyan-400',
        bg:    'bg-cyan-500/10',
        label: 'Remarks Added',
    },
    ALERT_PUBLISHED: {
        icon:  Bell,
        color: 'text-yellow-400',
        bg:    'bg-yellow-500/10',
        label: 'Alert Published',
    },
    ALERT_WITHDRAWN: {
        icon:  Bell,
        color: 'text-red-400',
        bg:    'bg-red-500/10',
        label: 'Alert Withdrawn',
    },
    ALERT_EXPIRED: {
        icon:  Bell,
        color: 'text-slate-400',
        bg:    'bg-slate-500/10',
        label: 'Alert Expired',
    },
    ALERT_UPDATED: {
        icon:  Bell,
        color: 'text-orange-400',
        bg:    'bg-orange-500/10',
        label: 'Alert Updated',
    },
    USER_REGISTERED: {
        icon:  User,
        color: 'text-purple-400',
        bg:    'bg-purple-500/10',
        label: 'User Registered',
    },
    USER_LOGIN: {
        icon:  LogIn,
        color: 'text-cyan-400',
        bg:    'bg-cyan-500/10',
        label: 'User Login',
    },
    USER_DEACTIVATED: {
        icon:  User,
        color: 'text-red-400',
        bg:    'bg-red-500/10',
        label: 'User Deactivated',
    },
    USER_LOGOUT: {
        icon:  LogIn,
        color: 'text-slate-400',
        bg:    'bg-slate-500/10',
        label: 'User Logout',
    },
    RISK_LEVEL_UPDATED: {
        icon:  AlertTriangle,
        color: 'text-orange-400',
        bg:    'bg-orange-500/10',
        label: 'Risk Updated',
    },
    RISK_SCORE_CALCULATED: {
        icon:  Activity,
        color: 'text-cyan-400',
        bg:    'bg-cyan-500/10',
        label: 'Risk Calculated',
    },
    // ==========================================
    // NEW — Intelligence Pipeline Actions
    // ==========================================
    INTELLIGENT_ANALYSIS: {
        icon:  Brain,
        color: 'text-purple-400',
        bg:    'bg-purple-500/10',
        label: 'AI Analysis',
    },
    REPORT_REANALYZED: {
        icon:  Brain,
        color: 'text-blue-400',
        bg:    'bg-blue-500/10',
        label: 'Re-Analyzed',
    },
    AUTO_ALERT_TRIGGERED: {
        icon:  Zap,
        color: 'text-red-400',
        bg:    'bg-red-500/10',
        label: 'Auto Alert',
    },
};

// ==========================================
// FALLBACK CONFIG
// ==========================================
const defaultConfig = {
    icon:  Activity,
    color: 'text-slate-400',
    bg:    'bg-slate-500/10',
    label: 'System Action',
};

// ==========================================
// HISTORY DETAIL MODAL
// ==========================================
const HistoryDetailModal = ({ record, onClose }) => {

    const config =
        actionConfig[record.actionType]
        || defaultConfig;
    const IconComponent = config.icon;

    return (
        <div className="fixed inset-0 z-50
                        flex items-center justify-center
                        p-4"
             style={{
                 background:     'rgba(0,0,0,0.7)',
                 backdropFilter: 'blur(4px)',
             }}>
            <div className="glass-card w-full max-w-lg
                            border border-white/10
                            max-h-[90vh] overflow-y-auto">

                {/* Header */}
                <div className="flex items-center
                                justify-between p-6
                                border-b border-white/5">
                    <div className="flex items-center gap-3">
                        <div className={`w-10 h-10 rounded-xl
                                         flex items-center
                                         justify-center
                                         ${config.bg}`}>
                            <IconComponent
                                className={`w-5 h-5
                                            ${config.color}`}
                            />
                        </div>
                        <div>
                            <h2 className="text-base font-bold
                                           text-white">
                                Activity Detail
                            </h2>
                            <p className="text-xs
                                          text-slate-400">
                                {config.label}
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
                <div className="p-6 space-y-4">

                    {/* AI Analysis Badge */}
                    {record.actionType
                        === 'INTELLIGENT_ANALYSIS'
                        && (
                        <div className="flex items-center
                                        gap-2 p-3 rounded-xl
                                        bg-purple-500/10
                                        border
                                        border-purple-500/20">
                            <Brain className="w-4 h-4
                                              text-purple-400
                                              flex-shrink-0"/>
                            <p className="text-xs
                                          text-purple-300">
                                This record was generated
                                by the AI Intelligence
                                Pipeline — 9-stage analysis
                            </p>
                        </div>
                    )}

                    {/* Description */}
                    <div className="p-4 rounded-xl
                                    bg-white/3
                                    border border-white/5">
                        <p className="text-xs font-medium
                                      text-slate-400
                                      uppercase tracking-wider
                                      mb-2">
                            Description
                        </p>
                        <p className="text-sm text-slate-300
                                      leading-relaxed">
                            {record.actionDescription}
                        </p>
                    </div>

                    {/* Details Grid */}
                    <div className="grid grid-cols-2 gap-3">

                        {/* Performed By */}
                        <div className="p-3 rounded-xl
                                        bg-white/3
                                        border border-white/5">
                            <p className="text-xs
                                          text-slate-500 mb-1">
                                Performed By
                            </p>
                            <p className="text-sm font-medium
                                          text-white">
                                {record.performedBy?.fullName
                                    || 'System'}
                            </p>
                            <p className="text-xs
                                          text-slate-500">
                                {record.performedBy?.role}
                            </p>
                        </div>

                        {/* Timestamp */}
                        <div className="p-3 rounded-xl
                                        bg-white/3
                                        border border-white/5">
                            <p className="text-xs
                                          text-slate-500 mb-1">
                                Timestamp
                            </p>
                            <p className="text-sm font-medium
                                          text-white">
                                {formatDateTime(
                                    record.createdAt
                                )}
                            </p>
                            <p className="text-xs
                                          text-slate-500">
                                {timeAgo(record.createdAt)}
                            </p>
                        </div>

                        {/* Status Change */}
                        {(record.previousStatus
                            || record.newStatus) && (
                            <div className="col-span-2
                                            p-3 rounded-xl
                                            bg-white/3
                                            border
                                            border-white/5">
                                <p className="text-xs
                                              text-slate-500
                                              mb-2">
                                    Status Change
                                </p>
                                <div className="flex items-center
                                                gap-3">
                                    {record.previousStatus && (
                                        <span className="badge-pending">
                                            {record.previousStatus}
                                        </span>
                                    )}
                                    {record.previousStatus
                                        && record.newStatus
                                        && (
                                        <span className="text-slate-400
                                                         text-xs">
                                            →
                                        </span>
                                    )}
                                    {record.newStatus && (
                                        <span className="badge-verified">
                                            {record.newStatus}
                                        </span>
                                    )}
                                </div>
                            </div>
                        )}

                        {/* Risk Level */}
                        {record.riskLevelAtTime && (
                            <div className="p-3 rounded-xl
                                            bg-white/3
                                            border border-white/5">
                                <p className="text-xs
                                              text-slate-500
                                              mb-1">
                                    Risk Level
                                </p>
                                <span className={
                                    getRiskBadgeClass(
                                        record.riskLevelAtTime
                                    )
                                }>
                                    {record.riskLevelAtTime}
                                </span>
                            </div>
                        )}

                        {/* Risk Score */}
                        {record.riskScoreAtTime && (
                            <div className="p-3 rounded-xl
                                            bg-white/3
                                            border border-white/5">
                                <p className="text-xs
                                              text-slate-500
                                              mb-1">
                                    Risk Score
                                </p>
                                <div className="flex items-center
                                                gap-2">
                                    <p className="text-sm
                                                  font-bold
                                                  text-white">
                                        {record.riskScoreAtTime}
                                        /100
                                    </p>
                                    <div className="flex-1
                                                    h-1.5
                                                    rounded-full
                                                    bg-white/10">
                                        <div
                                            className="h-1.5
                                                       rounded-full"
                                            style={{
                                                width: `${record.riskScoreAtTime}%`,
                                                background:
                                                    record.riskScoreAtTime
                                                    >= 70
                                                    ? '#ff4757'
                                                    : record.riskScoreAtTime
                                                      >= 40
                                                    ? '#ffd32a'
                                                    : '#00ff88',
                                            }}
                                        />
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>

                    {/* Linked Incident */}
                    {record.incidentReport && (
                        <div className="p-3 rounded-xl
                                        bg-cyan-500/5
                                        border
                                        border-cyan-500/20">
                            <p className="text-xs
                                          text-cyan-400
                                          font-medium mb-1">
                                Linked Incident
                            </p>
                            <p className="text-sm text-white">
                                #{record.incidentReport.id}
                                {' — '}
                                {record.incidentReport.title}
                            </p>
                            {record.incidentReport
                                .riskLevel && (
                                <span className={
                                    getRiskBadgeClass(
                                        record.incidentReport
                                              .riskLevel
                                    )
                                }>
                                    {record.incidentReport
                                           .riskLevel}
                                </span>
                            )}
                        </div>
                    )}

                    {/* Linked Alert */}
                    {record.cyberAlert && (
                        <div className="p-3 rounded-xl
                                        bg-yellow-500/5
                                        border
                                        border-yellow-500/20">
                            <p className="text-xs
                                          text-yellow-400
                                          font-medium mb-1">
                                Linked Alert
                            </p>
                            <p className="text-sm text-white">
                                #{record.cyberAlert.id}
                                {' — '}
                                {record.cyberAlert.title}
                            </p>
                            {record.cyberAlert
                                .severity && (
                                <p className="text-xs
                                              text-yellow-400
                                              mt-1">
                                    Severity:{' '}
                                    {record.cyberAlert
                                           .severity}
                                </p>
                            )}
                        </div>
                    )}

                    <button
                        onClick={onClose}
                        className="w-full py-3 rounded-xl
                                   bg-white/5
                                   border border-white/10
                                   text-slate-300
                                   hover:bg-white/10
                                   transition-all text-sm">
                        Close
                    </button>
                </div>
            </div>
        </div>
    );
};

// ==========================================
// THREAT HISTORY PAGE
// ==========================================
const ThreatHistory = () => {

    const [history,
           setHistory]       = useState([]);
    const [filtered,
           setFiltered]      = useState([]);
    const [isLoading,
           setIsLoading]     = useState(true);
    const [searchQuery,
           setSearchQuery]   = useState('');
    const [actionFilter,
           setActionFilter]  = useState('ALL');
    const [selectedRecord,
           setSelectedRecord]= useState(null);
    const [activeTab,
           setActiveTab]     = useState('all');

    // ==========================================
    // FETCH HISTORY
    // ==========================================
    const fetchHistory = async () => {
        setIsLoading(true);
        try {
            const response = await adminService
                .getAllHistory();
            if (response.success) {
                setHistory(response.history || []);
                setFiltered(response.history || []);
            }
        } catch (error) {
            console.error('Error:', error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchHistory();
    }, []);

    // ==========================================
    // FILTER
    // ==========================================
    useEffect(() => {
        let result = [...history];

        if (searchQuery) {
            result = result.filter(h =>
                h.actionDescription?.toLowerCase()
                 .includes(
                     searchQuery.toLowerCase()
                 ) ||
                h.performedBy?.fullName
                 ?.toLowerCase()
                 .includes(
                     searchQuery.toLowerCase()
                 )
            );
        }

        if (actionFilter !== 'ALL') {
            result = result.filter(
                h => h.actionType === actionFilter
            );
        }

        if (activeTab === 'reports') {
            result = result.filter(h =>
                h.actionType?.includes('REPORT')
                || h.actionType
                   === 'INTELLIGENT_ANALYSIS'
                || h.actionType
                   === 'REPORT_REANALYZED'
            );
        } else if (activeTab === 'alerts') {
            result = result.filter(h =>
                h.actionType?.includes('ALERT')
            );
        } else if (activeTab === 'users') {
            result = result.filter(h =>
                h.actionType?.includes('USER')
            );
        } else if (activeTab === 'ai') {
            result = result.filter(h =>
                h.actionType
                    === 'INTELLIGENT_ANALYSIS'
                || h.actionType
                    === 'REPORT_REANALYZED'
                || h.actionType
                    === 'AUTO_ALERT_TRIGGERED'
                || h.actionType
                    === 'RISK_SCORE_CALCULATED'
                || h.actionType
                    === 'RISK_LEVEL_UPDATED'
            );
        }

        setFiltered(result);
    }, [
        searchQuery,
        actionFilter,
        history,
        activeTab,
    ]);

    // ==========================================
    // STATS
    // ==========================================
    const stats = {
        total:   history.length,
        reports: history.filter(h =>
                     h.actionType?.includes('REPORT')
                 ).length,
        alerts:  history.filter(h =>
                     h.actionType?.includes('ALERT')
                 ).length,
        users:   history.filter(h =>
                     h.actionType?.includes('USER')
                 ).length,
        ai:      history.filter(h =>
                     h.actionType
                         === 'INTELLIGENT_ANALYSIS'
                     || h.actionType
                         === 'REPORT_REANALYZED'
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
                <div>
                    <h1 className="text-2xl font-bold
                                   text-white">
                        Threat History
                    </h1>
                    <p className="text-slate-400
                                  text-sm mt-1">
                        Complete audit log of all
                        activities
                    </p>
                </div>
                <button
                    onClick={fetchHistory}
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
                            lg:grid-cols-5 gap-4">
                {[
                    {
                        label:  'Total',
                        value:  stats.total,
                        color:  'text-cyan-400',
                        bg:     'bg-cyan-500/10',
                        border: 'border-cyan-500/10',
                        icon:   History,
                    },
                    {
                        label:  'Reports',
                        value:  stats.reports,
                        color:  'text-blue-400',
                        bg:     'bg-blue-500/10',
                        border: 'border-blue-500/10',
                        icon:   FileText,
                    },
                    {
                        label:  'Alerts',
                        value:  stats.alerts,
                        color:  'text-yellow-400',
                        bg:     'bg-yellow-500/10',
                        border: 'border-yellow-500/10',
                        icon:   Bell,
                    },
                    {
                        label:  'Users',
                        value:  stats.users,
                        color:  'text-purple-400',
                        bg:     'bg-purple-500/10',
                        border: 'border-purple-500/10',
                        icon:   User,
                    },
                    {
                        label:  'AI Analysis',
                        value:  stats.ai,
                        color:  'text-pink-400',
                        bg:     'bg-pink-500/10',
                        border: 'border-pink-500/10',
                        icon:   Brain,
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
                    { key: 'all',     label: 'All'         },
                    { key: 'reports', label: '📋 Reports'  },
                    { key: 'alerts',  label: '🔔 Alerts'   },
                    { key: 'users',   label: '👤 Users'    },
                    { key: 'ai',      label: '🧠 AI'       },
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
                                setSearchQuery(
                                    e.target.value
                                )
                            }
                            placeholder="Search activities..."
                            className="cyber-input pl-9
                                       h-9 text-sm"
                        />
                    </div>
                    <select
                        value={actionFilter}
                        onChange={(e) =>
                            setActionFilter(e.target.value)
                        }
                        className="cyber-input h-9 text-sm
                                   w-48 cursor-pointer">
                        <option value="ALL">
                            All Actions
                        </option>
                        <optgroup label="Reports">
                            <option value="REPORT_SUBMITTED">
                                Report Submitted
                            </option>
                            <option value="REPORT_VERIFIED">
                                Report Verified
                            </option>
                            <option value="REPORT_DISMISSED">
                                Report Dismissed
                            </option>
                            <option value="INTELLIGENT_ANALYSIS">
                                AI Analysis
                            </option>
                            <option value="REPORT_REANALYZED">
                                Re-Analyzed
                            </option>
                        </optgroup>
                        <optgroup label="Alerts">
                            <option value="ALERT_PUBLISHED">
                                Alert Published
                            </option>
                            <option value="ALERT_WITHDRAWN">
                                Alert Withdrawn
                            </option>
                        </optgroup>
                        <optgroup label="Users">
                            <option value="USER_REGISTERED">
                                User Registered
                            </option>
                            <option value="USER_LOGIN">
                                User Login
                            </option>
                        </optgroup>
                    </select>
                    <p className="text-xs text-slate-400
                                  ml-auto">
                        {filtered.length} records
                    </p>
                </div>
            </div>

            {/* History Table */}
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
                    <History className="w-12 h-12
                                        text-slate-600
                                        mx-auto mb-3"/>
                    <p className="text-slate-400 text-sm">
                        No history records found
                    </p>
                </div>
            ) : (
                <div className="glass-card overflow-hidden">
                    <table className="cyber-table">
                        <thead>
                            <tr>
                                <th>Action</th>
                                <th>Description</th>
                                <th>Performed By</th>
                                <th>Risk</th>
                                <th>Time</th>
                                <th>View</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filtered.map(record => {
                                const config =
                                    actionConfig[
                                        record.actionType
                                    ]
                                    || defaultConfig;
                                const IconComp =
                                    config.icon;

                                return (
                                    <tr key={record.id}>
                                        <td>
                                            <div className="flex
                                                            items-center
                                                            gap-2">
                                                <div className={`w-7
                                                                 h-7
                                                                 rounded-lg
                                                                 flex
                                                                 items-center
                                                                 justify-center
                                                                 ${config.bg}`}>
                                                    <IconComp
                                                        className={`w-3.5
                                                                    h-3.5
                                                                    ${config.color}`}
                                                    />
                                                </div>
                                                <span className="text-xs
                                                                 text-slate-300
                                                                 font-medium">
                                                    {config.label}
                                                </span>
                                            </div>
                                        </td>
                                        <td>
                                            <p className="text-xs
                                                          text-slate-400
                                                          max-w-xs
                                                          line-clamp-2">
                                                {record.actionDescription}
                                            </p>
                                        </td>
                                        <td>
                                            <p className="text-xs
                                                          text-slate-300">
                                                {record.performedBy
                                                    ?.fullName
                                                    || 'System'}
                                            </p>
                                            <p className="text-xs
                                                          text-slate-500">
                                                {record.performedBy
                                                    ?.role}
                                            </p>
                                        </td>
                                        <td>
                                            {record.riskLevelAtTime
                                                ? (
                                                <span className={
                                                    getRiskBadgeClass(
                                                        record.riskLevelAtTime
                                                    )
                                                }>
                                                    {record.riskLevelAtTime}
                                                </span>
                                            ) : (
                                                <span className="text-xs
                                                                 text-slate-600">
                                                    —
                                                </span>
                                            )}
                                        </td>
                                        <td>
                                            <p className="text-xs
                                                          text-slate-400">
                                                {timeAgo(
                                                    record.createdAt
                                                )}
                                            </p>
                                        </td>
                                        <td>
                                            <button
                                                onClick={() =>
                                                    setSelectedRecord(
                                                        record
                                                    )
                                                }
                                                className="flex
                                                           items-center
                                                           gap-1.5
                                                           px-3 py-1.5
                                                           rounded-lg
                                                           bg-white/5
                                                           border
                                                           border-white/10
                                                           text-slate-300
                                                           hover:text-cyan-400
                                                           transition-all
                                                           text-xs">
                                                <Eye className="w-3 h-3"/>
                                                View
                                            </button>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Detail Modal */}
            {selectedRecord && (
                <HistoryDetailModal
                    record={selectedRecord}
                    onClose={() =>
                        setSelectedRecord(null)
                    }
                />
            )}
        </div>
    );
};

export default ThreatHistory;
