import React, { useState, useEffect } from 'react';
import {
    FileText,
    Search,
    RefreshCw,
    Download,
    Filter,
    AlertTriangle,
    CheckCircle,
    Info,
    XCircle,
    Shield,
    User,
    Bell,
    Activity,
    Clock,
    Eye,
    X,
    Terminal,
} from 'lucide-react';
import adminService from '../../services/adminService';
import {
    formatDateTime,
    timeAgo,
    getRiskBadgeClass,
} from '../../utils/helpers';

// ==========================================
// MOCK SYSTEM LOG DATA
// ==========================================
const generateMockLogs = () => {
    const types = [
        {
            type:    'INFO',
            color:   'text-cyan-400',
            bg:      'bg-cyan-500/10',
            border:  'border-cyan-500/20',
            icon:    Info,
        },
        {
            type:    'SUCCESS',
            color:   'text-green-400',
            bg:      'bg-green-500/10',
            border:  'border-green-500/20',
            icon:    CheckCircle,
        },
        {
            type:    'WARNING',
            color:   'text-yellow-400',
            bg:      'bg-yellow-500/10',
            border:  'border-yellow-500/20',
            icon:    AlertTriangle,
        },
        {
            type:    'ERROR',
            color:   'text-red-400',
            bg:      'bg-red-500/10',
            border:  'border-red-500/20',
            icon:    XCircle,
        },
    ];

    const messages = [
        {
            msg:      'User login successful',
            service:  'AUTH',
            type:     'SUCCESS',
        },
        {
            msg:      'New incident report submitted',
            service:  'REPORT',
            type:     'INFO',
        },
        {
            msg:      'AI risk scoring completed',
            service:  'AI_ENGINE',
            type:     'SUCCESS',
        },
        {
            msg:      'Alert published to institution',
            service:  'ALERT',
            type:     'SUCCESS',
        },
        {
            msg:      'Failed login attempt detected',
            service:  'AUTH',
            type:     'WARNING',
        },
        {
            msg:      'Database connection pool warning',
            service:  'DATABASE',
            type:     'WARNING',
        },
        {
            msg:      'Report verification completed',
            service:  'REPORT',
            type:     'SUCCESS',
        },
        {
            msg:      'JWT token validation failed',
            service:  'AUTH',
            type:     'ERROR',
        },
        {
            msg:      'New user registration',
            service:  'USER',
            type:     'INFO',
        },
        {
            msg:      'High risk threat detected',
            service:  'AI_ENGINE',
            type:     'WARNING',
        },
        {
            msg:      'Alert expired and auto-closed',
            service:  'ALERT',
            type:     'INFO',
        },
        {
            msg:      'API rate limit threshold reached',
            service:  'API',
            type:     'WARNING',
        },
        {
            msg:      'User account deactivated',
            service:  'USER',
            type:     'INFO',
        },
        {
            msg:      'System backup completed',
            service:  'SYSTEM',
            type:     'SUCCESS',
        },
        {
            msg:      'Email notification sent',
            service:  'NOTIFICATION',
            type:     'INFO',
        },
    ];

    return messages.map((m, i) => {
        const typeConfig = types.find(
            t => t.type === m.type
        );
        return {
            id:        i + 1,
            message:   m.msg,
            service:   m.service,
            type:      m.type,
            color:     typeConfig.color,
            bg:        typeConfig.bg,
            border:    typeConfig.border,
            Icon:      typeConfig.icon,
            timestamp: new Date(
                Date.now() - i * 8 * 60000
            ),
            details:   `Service: ${m.service} | Code: ${200 + i} | Duration: ${Math.floor(Math.random() * 500)}ms`,
        };
    });
};

const mockLogs = generateMockLogs();

// ==========================================
// LOG DETAIL MODAL
// ==========================================
const LogDetailModal = ({ log, onClose }) => (
    <div className="fixed inset-0 z-50
                    flex items-center justify-center
                    p-4"
         style={{
             background: 'rgba(0,0,0,0.7)',
             backdropFilter: 'blur(4px)',
         }}>
        <div className="glass-card w-full max-w-lg
                        border border-white/10">

            {/* Header */}
            <div className="flex items-center
                            justify-between p-6
                            border-b border-white/5">
                <div className="flex items-center gap-3">
                    <div className={`w-10 h-10 rounded-xl
                                     flex items-center
                                     justify-center
                                     ${log.bg}`}>
                        <log.Icon className={`w-5 h-5
                                              ${log.color}`}/>
                    </div>
                    <div>
                        <h2 className="text-base font-bold
                                       text-white">
                            Log Entry #{log.id}
                        </h2>
                        <p className="text-xs text-slate-400">
                            {log.service} Service
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

                {/* Type Badge */}
                <div className="flex items-center gap-2">
                    <span className={`px-3 py-1
                                      rounded-full text-xs
                                      font-bold border
                                      ${log.bg}
                                      ${log.color}
                                      ${log.border}`}>
                        {log.type}
                    </span>
                    <span className="badge-active">
                        {log.service}
                    </span>
                </div>

                {/* Message */}
                <div className="p-4 rounded-xl
                                bg-white/3
                                border border-white/5">
                    <p className="text-xs font-medium
                                  text-slate-400
                                  uppercase tracking-wider
                                  mb-2">
                        Message
                    </p>
                    <p className="text-sm text-slate-300">
                        {log.message}
                    </p>
                </div>

                {/* Details */}
                <div className="p-4 rounded-xl
                                bg-white/3
                                border border-white/5">
                    <p className="text-xs font-medium
                                  text-slate-400
                                  uppercase tracking-wider
                                  mb-2">
                        Technical Details
                    </p>
                    <p className="text-xs font-mono
                                  text-cyan-400">
                        {log.details}
                    </p>
                </div>

                {/* Timestamp */}
                <div className="p-4 rounded-xl
                                bg-white/3
                                border border-white/5">
                    <p className="text-xs font-medium
                                  text-slate-400
                                  uppercase tracking-wider
                                  mb-2">
                        Timestamp
                    </p>
                    <p className="text-sm text-white">
                        {formatDateTime(log.timestamp)}
                    </p>
                    <p className="text-xs text-slate-500
                                  mt-1">
                        {timeAgo(log.timestamp)}
                    </p>
                </div>

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

// ==========================================
// SYSTEM LOGS PAGE
// ==========================================
const SystemLogs = () => {

    const [logs,        setLogs]
        = useState([]);
    const [filtered,    setFiltered]
        = useState([]);
    const [isLoading,   setIsLoading]
        = useState(true);
    const [searchQuery, setSearchQuery]
        = useState('');
    const [typeFilter,  setTypeFilter]
        = useState('ALL');
    const [serviceFilter,
           setServiceFilter] = useState('ALL');
    const [selectedLog, setSelectedLog]
        = useState(null);
    const [autoRefresh, setAutoRefresh]
        = useState(false);
    const [historyLogs, setHistoryLogs]
        = useState([]);

    // ==========================================
    // FETCH LOGS
    // ==========================================
    const fetchLogs = async () => {
        setIsLoading(true);
        try {
            // Fetch real activity history
            const response = await adminService
                .getRecentActivity(7);

            // Combine mock logs with real history
            const realLogs = response.success
                ? response.activity
                    .slice(0, 10)
                    .map((h, i) => ({
                        id:        1000 + i,
                        message:   h.actionDescription,
                        service:   h.actionType
                                    ?.includes('REPORT')
                                    ? 'REPORT'
                                    : h.actionType
                                      ?.includes('ALERT')
                                    ? 'ALERT'
                                    : h.actionType
                                      ?.includes('USER')
                                    ? 'USER'
                                    : 'SYSTEM',
                        type:      h.actionType
                                    ?.includes('VERIFIED')
                                    || h.actionType
                                       ?.includes('SUCCESS')
                                    ? 'SUCCESS'
                                    : h.actionType
                                      ?.includes('DISMISSED')
                                    ? 'WARNING'
                                    : 'INFO',
                        color:     h.actionType
                                    ?.includes('VERIFIED')
                                    ? 'text-green-400'
                                    : h.actionType
                                      ?.includes('DISMISSED')
                                    ? 'text-yellow-400'
                                    : 'text-cyan-400',
                        bg:        h.actionType
                                    ?.includes('VERIFIED')
                                    ? 'bg-green-500/10'
                                    : h.actionType
                                      ?.includes('DISMISSED')
                                    ? 'bg-yellow-500/10'
                                    : 'bg-cyan-500/10',
                        border:    h.actionType
                                    ?.includes('VERIFIED')
                                    ? 'border-green-500/20'
                                    : h.actionType
                                      ?.includes('DISMISSED')
                                    ? 'border-yellow-500/20'
                                    : 'border-cyan-500/20',
                        Icon:      h.actionType
                                    ?.includes('VERIFIED')
                                    ? CheckCircle
                                    : h.actionType
                                      ?.includes('DISMISSED')
                                    ? AlertTriangle
                                    : Info,
                        timestamp: new Date(h.createdAt),
                        details:   `User: ${h.performedBy?.email || 'System'} | Action: ${h.actionType}`,
                    }))
                : [];

            // Merge with mock logs
            const allLogs = [
                ...realLogs,
                ...mockLogs,
            ].sort((a, b) =>
                new Date(b.timestamp)
                - new Date(a.timestamp)
            );

            setLogs(allLogs);
            setFiltered(allLogs);

        } catch (error) {
            // Fallback to mock logs
            setLogs(mockLogs);
            setFiltered(mockLogs);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchLogs();
    }, []);

    // ==========================================
    // AUTO REFRESH
    // ==========================================
    useEffect(() => {
        let interval;
        if (autoRefresh) {
            interval = setInterval(() => {
                fetchLogs();
            }, 30000);
        }
        return () => clearInterval(interval);
    }, [autoRefresh]);

    // ==========================================
    // FILTER LOGS
    // ==========================================
    useEffect(() => {
        let result = [...logs];

        if (searchQuery) {
            result = result.filter(l =>
                l.message?.toLowerCase()
                 .includes(searchQuery.toLowerCase()) ||
                l.service?.toLowerCase()
                 .includes(searchQuery.toLowerCase())
            );
        }

        if (typeFilter !== 'ALL') {
            result = result.filter(
                l => l.type === typeFilter
            );
        }

        if (serviceFilter !== 'ALL') {
            result = result.filter(
                l => l.service === serviceFilter
            );
        }

        setFiltered(result);
    }, [searchQuery, typeFilter, serviceFilter, logs]);

    // ==========================================
    // STATS
    // ==========================================
    const stats = {
        total:    logs.length,
        errors:   logs.filter(
                      l => l.type === 'ERROR'
                  ).length,
        warnings: logs.filter(
                      l => l.type === 'WARNING'
                  ).length,
        success:  logs.filter(
                      l => l.type === 'SUCCESS'
                  ).length,
    };

    // ==========================================
    // GET UNIQUE SERVICES
    // ==========================================
    const services = [
        'ALL',
        ...new Set(logs.map(l => l.service)),
    ];

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
                        System Logs
                    </h1>
                    <p className="text-slate-400
                                  text-sm mt-1">
                        Real-time system activity
                        and audit logs
                    </p>
                </div>
                <div className="flex items-center gap-3">

                    {/* Auto Refresh Toggle */}
                    <div className="flex items-center
                                    gap-2 px-4 py-2
                                    rounded-xl bg-white/5
                                    border border-white/10">
                        <div className={`w-2 h-2
                                         rounded-full
                                         ${autoRefresh
                                             ? 'bg-green-400 animate-pulse'
                                             : 'bg-slate-600'
                                         }`}/>
                        <span className="text-xs
                                         text-slate-400">
                            Auto
                        </span>
                        <button
                            onClick={() =>
                                setAutoRefresh(!autoRefresh)
                            }
                            className={`relative inline-flex
                                        items-center w-8 h-4
                                        rounded-full
                                        transition-all
                                        ${autoRefresh
                                            ? 'bg-cyan-500'
                                            : 'bg-slate-700'
                                        }`}>
                            <span className={`inline-block
                                              w-3 h-3
                                              rounded-full
                                              bg-white
                                              transform
                                              transition-transform
                                              ${autoRefresh
                                                  ? 'translate-x-4'
                                                  : 'translate-x-0.5'
                                              }`}/>
                        </button>
                    </div>

                    <button
                        onClick={fetchLogs}
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

                    <button
                        onClick={() =>
                            alert('Export coming soon!')
                        }
                        className="flex items-center gap-2
                                   px-4 py-2 rounded-xl
                                   bg-white/5
                                   border border-white/10
                                   text-slate-300
                                   hover:text-cyan-400
                                   transition-all text-sm">
                        <Download className="w-4 h-4"/>
                        Export
                    </button>
                </div>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-2
                            lg:grid-cols-4 gap-4">
                {[
                    {
                        label:  'Total Logs',
                        value:  stats.total,
                        icon:   FileText,
                        color:  'text-cyan-400',
                        bg:     'bg-cyan-500/10',
                        border: 'border-cyan-500/10',
                    },
                    {
                        label:  'Success',
                        value:  stats.success,
                        icon:   CheckCircle,
                        color:  'text-green-400',
                        bg:     'bg-green-500/10',
                        border: 'border-green-500/10',
                    },
                    {
                        label:  'Warnings',
                        value:  stats.warnings,
                        icon:   AlertTriangle,
                        color:  'text-yellow-400',
                        bg:     'bg-yellow-500/10',
                        border: 'border-yellow-500/10',
                    },
                    {
                        label:  'Errors',
                        value:  stats.errors,
                        icon:   XCircle,
                        color:  'text-red-400',
                        bg:     'bg-red-500/10',
                        border: 'border-red-500/10',
                    },
                ].map((stat, i) => (
                    <div key={i}
                         className={`glass-card p-5
                                     border ${stat.border}`}>
                        <div className="flex items-center
                                        gap-3">
                            <div className={`w-10 h-10
                                             rounded-xl
                                             flex items-center
                                             justify-center
                                             ${stat.bg}`}>
                                <stat.icon
                                    className={`w-5 h-5
                                                ${stat.color}`}
                                />
                            </div>
                            <div>
                                <p className={`text-2xl
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
                            placeholder="Search logs..."
                            className="cyber-input pl-9
                                       h-9 text-sm"
                        />
                    </div>

                    {/* Type Filter */}
                    <select
                        value={typeFilter}
                        onChange={(e) =>
                            setTypeFilter(e.target.value)
                        }
                        className="cyber-input h-9
                                   text-sm w-36
                                   cursor-pointer">
                        <option value="ALL">All Types</option>
                        <option value="INFO">Info</option>
                        <option value="SUCCESS">
                            Success
                        </option>
                        <option value="WARNING">
                            Warning
                        </option>
                        <option value="ERROR">Error</option>
                    </select>

                    {/* Service Filter */}
                    <select
                        value={serviceFilter}
                        onChange={(e) =>
                            setServiceFilter(e.target.value)
                        }
                        className="cyber-input h-9
                                   text-sm w-40
                                   cursor-pointer">
                        {services.map(s => (
                            <option key={s} value={s}>
                                {s === 'ALL'
                                    ? 'All Services'
                                    : s
                                }
                            </option>
                        ))}
                    </select>

                    <p className="text-xs text-slate-400
                                  ml-auto">
                        {filtered.length} logs
                    </p>
                </div>
            </div>

            {/* Logs Terminal View */}
            <div className="glass-card overflow-hidden">

                {/* Terminal Header */}
                <div className="flex items-center gap-2
                                px-4 py-3
                                border-b border-white/5
                                bg-white/2">
                    <div className="flex items-center gap-1.5">
                        <div className="w-3 h-3 rounded-full
                                        bg-red-500/60"/>
                        <div className="w-3 h-3 rounded-full
                                        bg-yellow-500/60"/>
                        <div className="w-3 h-3 rounded-full
                                        bg-green-500/60"/>
                    </div>
                    <div className="flex items-center gap-2
                                    ml-3">
                        <Terminal className="w-3 h-3
                                             text-slate-400"/>
                        <span className="text-xs
                                         text-slate-400
                                         font-mono">
                            system.log — CyberShield
                        </span>
                    </div>
                    <div className="ml-auto flex items-center
                                    gap-1.5">
                        <div className={`w-2 h-2 rounded-full
                                         ${autoRefresh
                                             ? 'bg-green-400 animate-pulse'
                                             : 'bg-slate-600'
                                         }`}/>
                        <span className="text-xs
                                         text-slate-500
                                         font-mono">
                            {autoRefresh
                                ? 'live'
                                : 'paused'
                            }
                        </span>
                    </div>
                </div>

                {/* Log Entries */}
                {isLoading ? (
                    <div className="flex items-center
                                    justify-center py-16">
                        <RefreshCw className="w-6 h-6
                                              text-cyan-400
                                              animate-spin"/>
                    </div>
                ) : filtered.length === 0 ? (
                    <div className="text-center py-16">
                        <FileText className="w-12 h-12
                                             text-slate-600
                                             mx-auto mb-3"/>
                        <p className="text-slate-400 text-sm">
                            No logs found
                        </p>
                    </div>
                ) : (
                    <div className="divide-y divide-white/3
                                    max-h-[600px]
                                    overflow-y-auto">
                        {filtered.map(log => (
                            <div
                                key={log.id}
                                className="flex items-start
                                           gap-4 px-4 py-3
                                           hover:bg-white/2
                                           transition-colors
                                           font-mono
                                           cursor-pointer"
                                onClick={() =>
                                    setSelectedLog(log)
                                }>

                                {/* Timestamp */}
                                <span className="text-xs
                                                 text-slate-600
                                                 flex-shrink-0
                                                 mt-0.5 w-32">
                                    {timeAgo(log.timestamp)}
                                </span>

                                {/* Type Badge */}
                                <span className={`text-xs
                                                  font-bold
                                                  flex-shrink-0
                                                  w-16
                                                  ${log.color}`}>
                                    [{log.type}]
                                </span>

                                {/* Service */}
                                <span className="text-xs
                                                 text-slate-500
                                                 flex-shrink-0
                                                 w-24">
                                    {log.service}
                                </span>

                                {/* Message */}
                                <span className="text-xs
                                                 text-slate-300
                                                 flex-1">
                                    {log.message}
                                </span>

                                {/* View Button */}
                                <button
                                    className="flex items-center
                                               gap-1 text-xs
                                               text-slate-500
                                               hover:text-cyan-400
                                               transition-colors
                                               flex-shrink-0">
                                    <Eye className="w-3 h-3"/>
                                </button>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Log Detail Modal */}
            {selectedLog && (
                <LogDetailModal
                    log={selectedLog}
                    onClose={() => setSelectedLog(null)}
                />
            )}
        </div>
    );
};

export default SystemLogs;