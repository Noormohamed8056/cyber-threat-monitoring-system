import React, { useState, useEffect } from 'react';
import {
    Bell,
    BellPlus,
    Search,
    Eye,
    X,
    RefreshCw,
    AlertTriangle,
    CheckCircle,
    XCircle,
    Shield,
    Calendar,
    Building,
    Globe,
    Lock,
} from 'lucide-react';
import alertService  from '../../services/alertService';
import reportService from '../../services/reportService';
import {
    formatDateTime,
    getRiskBadgeClass,
    getStatusBadgeClass,
    timeAgo,
    truncateText,
    extractUrls,
    normalizeUrl,
} from '../../utils/helpers';
import {
    ALERT_TYPES,
    ALERT_SEVERITIES,
} from '../../utils/constants';

// ==========================================
// PUBLISH ALERT MODAL
// ==========================================
const PublishAlertModal = ({ onClose, onPublish }) => {

    const [formData, setFormData] = useState({
        title:             '',
        message:           '',
        alertType:         'THREAT_WARNING',
        severity:          'MEDIUM',
        targetInstitution: '',
        isPublic:          true,
        expiresAt:         '',
    });
    const [isLoading, setIsLoading] = useState(false);
    const [error,     setError]     = useState('');

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData({
            ...formData,
            [name]: type === 'checkbox' ? checked : value,
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!formData.title.trim()) {
            setError('Title is required');
            return;
        }
        if (!formData.message.trim()) {
            setError('Message is required');
            return;
        }
        setIsLoading(true);
        setError('');
        try {
            await onPublish({
                ...formData,
                expiresAt: formData.expiresAt
                    ? formData.expiresAt + ':00'
                    : null,
            });
            onClose();
        } catch (err) {
            setError(
                err.response?.data?.message
                || 'Failed to publish alert'
            );
        } finally {
            setIsLoading(false);
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
            <div className="glass-card w-full max-w-xl
                            max-h-[90vh] overflow-y-auto
                            border border-white/10">

                {/* Header */}
                <div className="flex items-center
                                justify-between p-6
                                border-b border-white/5">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl
                                        bg-cyan-500/10
                                        border border-cyan-500/20
                                        flex items-center
                                        justify-center">
                            <BellPlus className="w-5 h-5
                                                 text-cyan-400"/>
                        </div>
                        <div>
                            <h2 className="text-lg font-bold
                                           text-white">
                                Publish New Alert
                            </h2>
                            <p className="text-xs
                                          text-slate-400">
                                Notify students about threats
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

                {/* Form */}
                <form onSubmit={handleSubmit}
                      className="p-6 space-y-4">

                    {error && (
                        <div className="p-3 rounded-lg
                                        bg-red-500/10
                                        border border-red-500/30
                                        text-red-400 text-sm">
                            {error}
                        </div>
                    )}

                    {/* Title */}
                    <div>
                        <label className="block text-xs
                                          font-medium
                                          text-slate-400
                                          uppercase
                                          tracking-wider mb-2">
                            Alert Title *
                        </label>
                        <input
                            type="text"
                            name="title"
                            value={formData.title}
                            onChange={handleChange}
                            placeholder="Enter alert title"
                            className="cyber-input text-sm"
                            required
                        />
                    </div>

                    {/* Message */}
                    <div>
                        <label className="block text-xs
                                          font-medium
                                          text-slate-400
                                          uppercase
                                          tracking-wider mb-2">
                            Alert Message *
                        </label>
                        <textarea
                            name="message"
                            value={formData.message}
                            onChange={handleChange}
                            placeholder="Enter detailed alert message"
                            className="cyber-input text-sm
                                       resize-none h-28"
                            required
                        />
                    </div>

                    {/* Alert Type and Severity */}
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-xs
                                              font-medium
                                              text-slate-400
                                              uppercase
                                              tracking-wider mb-2">
                                Alert Type *
                            </label>
                            <select
                                name="alertType"
                                value={formData.alertType}
                                onChange={handleChange}
                                className="cyber-input
                                           text-sm
                                           cursor-pointer">
                                {ALERT_TYPES.map(type => (
                                    <option
                                        key={type.value}
                                        value={type.value}>
                                        {type.label}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div>
                            <label className="block text-xs
                                              font-medium
                                              text-slate-400
                                              uppercase
                                              tracking-wider mb-2">
                                Severity *
                            </label>
                            <select
                                name="severity"
                                value={formData.severity}
                                onChange={handleChange}
                                className="cyber-input
                                           text-sm
                                           cursor-pointer">
                                {ALERT_SEVERITIES.map(s => (
                                    <option
                                        key={s.value}
                                        value={s.value}>
                                        {s.label}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>

                    {/* Target Institution */}
                    <div>
                        <label className="block text-xs
                                          font-medium
                                          text-slate-400
                                          uppercase
                                          tracking-wider mb-2">
                            Target Institution
                            (leave empty for all)
                        </label>
                        <div className="relative">
                            <Building className="absolute
                                                  left-3 top-1/2
                                                  -translate-y-1/2
                                                  w-4 h-4
                                                  text-slate-400"/>
                            <input
                                type="text"
                                name="targetInstitution"
                                value={formData.targetInstitution}
                                onChange={handleChange}
                                placeholder="e.g. MIT, Harvard..."
                                className="cyber-input
                                           text-sm pl-10"
                            />
                        </div>
                    </div>

                    {/* Expiry Date */}
                    <div>
                        <label className="block text-xs
                                          font-medium
                                          text-slate-400
                                          uppercase
                                          tracking-wider mb-2">
                            Expiry Date (Optional)
                        </label>
                        <div className="relative">
                            <Calendar className="absolute
                                                  left-3 top-1/2
                                                  -translate-y-1/2
                                                  w-4 h-4
                                                  text-slate-400"/>
                            <input
                                type="datetime-local"
                                name="expiresAt"
                                value={formData.expiresAt}
                                onChange={handleChange}
                                className="cyber-input
                                           text-sm pl-10"
                            />
                        </div>
                    </div>

                    {/* Public Toggle */}
                    <div className="flex items-center
                                    justify-between p-4
                                    rounded-xl bg-white/3
                                    border border-white/5">
                        <div className="flex items-center
                                        gap-3">
                            {formData.isPublic
                                ? <Globe className="w-4 h-4
                                                    text-cyan-400"/>
                                : <Lock  className="w-4 h-4
                                                    text-slate-400"/>
                            }
                            <div>
                                <p className="text-sm
                                              font-medium
                                              text-white">
                                    {formData.isPublic
                                        ? 'Public Alert'
                                        : 'Private Alert'
                                    }
                                </p>
                                <p className="text-xs
                                              text-slate-400">
                                    {formData.isPublic
                                        ? 'Visible to all students'
                                        : 'Only target institution'
                                    }
                                </p>
                            </div>
                        </div>
                        <label className="relative inline-flex
                                          items-center
                                          cursor-pointer">
                            <input
                                type="checkbox"
                                name="isPublic"
                                checked={formData.isPublic}
                                onChange={handleChange}
                                className="sr-only peer"
                            />
                            <div className="w-11 h-6
                                            bg-slate-700
                                            rounded-full
                                            peer
                                            peer-checked:bg-cyan-500
                                            after:content-['']
                                            after:absolute
                                            after:top-[2px]
                                            after:left-[2px]
                                            after:bg-white
                                            after:rounded-full
                                            after:h-5
                                            after:w-5
                                            after:transition-all
                                            peer-checked:after:translate-x-5"/>
                        </label>
                    </div>

                    {/* Severity Preview */}
                    <div className={`p-3 rounded-xl
                                     border flex items-center
                                     gap-3
                                     ${formData.severity === 'CRITICAL'
                                         ? 'bg-purple-500/10 border-purple-500/20'
                                         : formData.severity === 'HIGH'
                                         ? 'bg-red-500/10 border-red-500/20'
                                         : formData.severity === 'MEDIUM'
                                         ? 'bg-yellow-500/10 border-yellow-500/20'
                                         : 'bg-green-500/10 border-green-500/20'
                                     }`}>
                        <AlertTriangle className={`w-4 h-4
                                                    flex-shrink-0
                                                    ${formData.severity === 'CRITICAL'
                                                        ? 'text-purple-400'
                                                        : formData.severity === 'HIGH'
                                                        ? 'text-red-400'
                                                        : formData.severity === 'MEDIUM'
                                                        ? 'text-yellow-400'
                                                        : 'text-green-400'
                                                    }`}/>
                        <p className="text-xs text-slate-300">
                            This alert will be marked as
                            <span className="font-semibold
                                             mx-1">
                                {formData.severity}
                            </span>
                            severity and
                            {formData.isPublic
                                ? ' sent to all students'
                                : ` sent to ${formData.targetInstitution || 'target institution'}`
                            }
                        </p>
                    </div>

                    {/* Submit Button */}
                    <button
                        type="submit"
                        disabled={isLoading}
                        className="w-full btn-cyber
                                   py-3 flex items-center
                                   justify-center gap-2
                                   disabled:opacity-50">
                        {isLoading ? (
                            <>
                                <RefreshCw className="w-4 h-4
                                                      animate-spin"/>
                                Publishing...
                            </>
                        ) : (
                            <>
                                <Bell className="w-4 h-4"/>
                                Publish Alert
                            </>
                        )}
                    </button>
                </form>
            </div>
        </div>
    );
};

// ==========================================
// ALERT CARD COMPONENT
// ==========================================
const AlertCard = ({ alert, onWithdraw }) => {

    const severityConfig = {
        LOW:      {
            color: 'text-green-400',
            bg:    'bg-green-500/10',
            border:'border-green-500/20'
        },
        MEDIUM:   {
            color: 'text-yellow-400',
            bg:    'bg-yellow-500/10',
            border:'border-yellow-500/20'
        },
        HIGH:     {
            color: 'text-red-400',
            bg:    'bg-red-500/10',
            border:'border-red-500/20'
        },
        CRITICAL: {
            color: 'text-purple-400',
            bg:    'bg-purple-500/10',
            border:'border-purple-500/20'
        },
    };

    const config = severityConfig[alert.severity]
        || severityConfig.MEDIUM;

    return (
        <div className={`glass-card p-5
                         border ${config.border}
                         hover:scale-[1.01]
                         transition-transform duration-200`}>

            {/* Header */}
            <div className="flex items-start
                            justify-between mb-3">
                <div className="flex items-start gap-3
                                flex-1 min-w-0">
                    <div className={`w-9 h-9 rounded-xl
                                     flex items-center
                                     justify-center
                                     flex-shrink-0
                                     ${config.bg}`}>
                        <Bell className={`w-4 h-4
                                          ${config.color}`}/>
                    </div>
                    <div className="min-w-0">
                        <p className="text-sm font-semibold
                                      text-white truncate">
                            {alert.title}
                        </p>
                        <p className="text-xs text-slate-500
                                      mt-0.5">
                            {timeAgo(alert.createdAt)}
                        </p>
                    </div>
                </div>

                {/* Badges */}
                <div className="flex items-center gap-2
                                flex-shrink-0 ml-3">
                    <span className={getRiskBadgeClass(
                        alert.severity
                    )}>
                        {alert.severity}
                    </span>
                    <span className={getStatusBadgeClass(
                        alert.status
                    )}>
                        {alert.status}
                    </span>
                </div>
            </div>

            {/* Message */}
            <p className="text-xs text-slate-400
                          leading-relaxed mb-4">
                {renderLinkedText(truncateText(alert.message, 120))}
            </p>

            {/* Footer */}
            <div className="flex items-center
                            justify-between">
                <div className="flex items-center gap-3
                                text-xs text-slate-500">
                    {alert.isPublic
                        ? (
                            <div className="flex items-center
                                            gap-1">
                                <Globe className="w-3 h-3"/>
                                <span>Public</span>
                            </div>
                        ) : (
                            <div className="flex items-center
                                            gap-1">
                                <Lock className="w-3 h-3"/>
                                <span>
                                    {alert.targetInstitution}
                                </span>
                            </div>
                        )
                    }
                    {alert.expiresAt && (
                        <div className="flex items-center
                                        gap-1">
                            <Calendar className="w-3 h-3"/>
                            <span>
                                Expires{' '}
                                {timeAgo(alert.expiresAt)}
                            </span>
                        </div>
                    )}
                </div>

                {/* Withdraw Button */}
                {alert.status === 'ACTIVE' && (
                    <button
                        onClick={() => onWithdraw(alert.id)}
                        className="flex items-center gap-1.5
                                   px-3 py-1.5 rounded-lg
                                   bg-red-500/10
                                   border border-red-500/20
                                   text-red-400
                                   hover:bg-red-500/20
                                   transition-all
                                   text-xs font-medium">
                        <XCircle className="w-3 h-3"/>
                        Withdraw
                    </button>
                )}
            </div>
        </div>
    );
};

// ==========================================
// ALERT MANAGEMENT PAGE
// ==========================================
const AlertManagement = () => {

    const [alerts,       setAlerts]
        = useState([]);
    const [filtered,     setFiltered]
        = useState([]);
    const [isLoading,    setIsLoading]
        = useState(true);
    const [searchQuery,  setSearchQuery]
        = useState('');
    const [statusFilter, setStatusFilter]
        = useState('ALL');
    const [severityFilter,setSeverityFilter]
        = useState('ALL');
    const [showModal,    setShowModal]
        = useState(false);
    const [activeTab,    setActiveTab]
        = useState('all');

    // ==========================================
    // FETCH ALERTS
    // ==========================================
    const fetchAlerts = async () => {
        setIsLoading(true);
        try {
            const response = await alertService
                .getAllAlerts();
            if (response.success) {
                setAlerts(response.alerts || []);
                setFiltered(response.alerts || []);
            }
        } catch (error) {
            console.error('Error fetching alerts:', error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchAlerts();
    }, []);

    // ==========================================
    // FILTER ALERTS
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

        if (statusFilter !== 'ALL') {
            result = result.filter(
                a => a.status === statusFilter
            );
        }

        if (severityFilter !== 'ALL') {
            result = result.filter(
                a => a.severity === severityFilter
            );
        }

        if (activeTab === 'active') {
            result = result.filter(
                a => a.status === 'ACTIVE'
            );
        } else if (activeTab === 'expired') {
            result = result.filter(
                a => a.status === 'EXPIRED'
                  || a.status === 'WITHDRAWN'
            );
        }

        setFiltered(result);
    }, [
        searchQuery,
        statusFilter,
        severityFilter,
        alerts,
        activeTab
    ]);

    // ==========================================
    // HANDLE PUBLISH ALERT
    // ==========================================
    const handlePublish = async (alertData) => {
        await alertService.publishAlert(alertData);
        await fetchAlerts();
    };

    // ==========================================
    // HANDLE WITHDRAW ALERT
    // ==========================================
    const handleWithdraw = async (id) => {
        if (!window.confirm(
            'Are you sure you want to withdraw this alert?'
        )) return;
        try {
            await alertService.withdrawAlert(id);
            await fetchAlerts();
        } catch (error) {
            console.error('Withdraw error:', error);
        }
    };

    // ==========================================
    // HANDLE EXPIRE OUTDATED
    // ==========================================
    const handleExpireOutdated = async () => {
        try {
            const response = await alertService
                .expireOutdatedAlerts();
            alert(response.message);
            await fetchAlerts();
        } catch (error) {
            console.error('Expire error:', error);
        }
    };

    // ==========================================
    // STATS
    // ==========================================
    const stats = {
        total:    alerts.length,
        active:   alerts.filter(
                      a => a.status === 'ACTIVE'
                  ).length,
        critical: alerts.filter(
                      a => a.severity === 'CRITICAL'
                  ).length,
        expired:  alerts.filter(
                      a => a.status === 'EXPIRED'
                         || a.status === 'WITHDRAWN'
                  ).length,
    };

    // ==========================================
    // RENDER
    // ==========================================
    return (
        <div className="space-y-6 animate-fade-in">

            {/* Page Header */}
            <div className="flex items-center
                            justify-between">
                <div>
                    <h1 className="text-2xl font-bold
                                   text-white">
                        Alert Management
                    </h1>
                    <p className="text-slate-400
                                  text-sm mt-1">
                        Publish and manage cyber alerts
                    </p>
                </div>
                <div className="flex items-center gap-3">
                    <button
                        onClick={handleExpireOutdated}
                        className="flex items-center gap-2
                                   px-4 py-2 rounded-xl
                                   bg-white/5
                                   border border-white/10
                                   text-slate-300
                                   hover:text-yellow-400
                                   hover:border-yellow-500/30
                                   transition-all text-sm">
                        <RefreshCw className="w-4 h-4"/>
                        Expire Outdated
                    </button>
                    <button
                        onClick={() => setShowModal(true)}
                        className="btn-cyber flex items-center
                                   gap-2 px-4 py-2 text-sm">
                        <BellPlus className="w-4 h-4"/>
                        Publish Alert
                    </button>
                </div>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-2
                            lg:grid-cols-4 gap-4">
                {[
                    {
                        label: 'Total Alerts',
                        value: stats.total,
                        color: 'text-cyan-400',
                        bg:    'bg-cyan-500/10',
                        border:'border-cyan-500/10',
                    },
                    {
                        label: 'Active',
                        value: stats.active,
                        color: 'text-green-400',
                        bg:    'bg-green-500/10',
                        border:'border-green-500/10',
                    },
                    {
                        label: 'Critical',
                        value: stats.critical,
                        color: 'text-purple-400',
                        bg:    'bg-purple-500/10',
                        border:'border-purple-500/10',
                    },
                    {
                        label: 'Expired/Withdrawn',
                        value: stats.expired,
                        color: 'text-slate-400',
                        bg:    'bg-slate-500/10',
                        border:'border-slate-500/10',
                    },
                ].map((stat, i) => (
                    <div key={i}
                         className={`glass-card p-5
                                     border ${stat.border}
                                     text-center`}>
                        <p className={`text-3xl font-bold
                                       ${stat.color}`}>
                            {stat.value}
                        </p>
                        <p className="text-xs
                                      text-slate-400 mt-1">
                            {stat.label}
                        </p>
                    </div>
                ))}
            </div>

            {/* Tabs */}
            <div className="flex items-center gap-1
                            p-1 rounded-xl
                            bg-white/3
                            border border-white/5
                            w-fit">
                {[
                    { key: 'all',     label: 'All Alerts' },
                    { key: 'active',  label: 'Active' },
                    { key: 'expired', label: 'Expired' },
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
                    <Bell className="w-12 h-12
                                     text-slate-600
                                     mx-auto mb-3"/>
                    <p className="text-slate-400 text-sm">
                        No alerts found
                    </p>
                    <button
                        onClick={() => setShowModal(true)}
                        className="btn-cyber px-6 py-2
                                   text-sm mt-4
                                   inline-flex items-center
                                   gap-2">
                        <BellPlus className="w-4 h-4"/>
                        Publish First Alert
                    </button>
                </div>
            ) : (
                <div className="grid grid-cols-1
                                lg:grid-cols-2 gap-4">
                    {filtered.map(alert => (
                        <AlertCard
                            key={alert.id}
                            alert={alert}
                            onWithdraw={handleWithdraw}
                        />
                    ))}
                </div>
            )}

            {/* Publish Modal */}
            {showModal && (
                <PublishAlertModal
                    onClose={() => setShowModal(false)}
                    onPublish={handlePublish}
                />
            )}
        </div>
    );
};

export default AlertManagement;
    const renderLinkedText = (text) => {
        const value = String(text || '');
        const parts = value.split(/((?:https?:\/\/|www\.)[^\s<>"')]+|[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,})/gi);
        return parts.map((part, idx) => {
            const urls = extractUrls(part);
            const emailMatch = String(part || '').trim().match(/^[A-Z0-9._%+-]+@([A-Z0-9.-]+\.[A-Z]{2,})$/i);
            if (urls.length || emailMatch) {
                const url = urls.length ? urls[0] : emailMatch[1];
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
            }
            return <React.Fragment key={idx}>{part}</React.Fragment>;
        });
    };
