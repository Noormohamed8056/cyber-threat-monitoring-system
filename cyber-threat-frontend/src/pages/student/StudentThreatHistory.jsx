import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    History,
    Search,
    RefreshCw,
    ShieldAlert,
    CheckCircle,
    XCircle,
    Eye,
    Clock,
    ArrowLeft,
    FileText,
    Brain,
    X,
    Link,
    Mail,
    Calendar,
} from 'lucide-react';
import reportService from '../../services/reportService';
import { API_BASE_URL } from '../../utils/constants';
import {
    formatDateTime,
    getRiskBadgeClass,
    getStatusBadgeClass,
    formatIncidentType,
    timeAgo,
} from '../../utils/helpers';

// ==========================================
// TIMELINE ITEM COMPONENT
// ==========================================
const TimelineItem = ({ report, onClick }) => {

    const getStatusIcon = (status) => {
        switch (status) {
            case 'VERIFIED':
                return (
                    <CheckCircle className="w-4 h-4
                                             text-green-400"/>
                );
            case 'DISMISSED':
                return (
                    <XCircle className="w-4 h-4
                                        text-slate-400"/>
                );
            case 'UNDER_REVIEW':
                return (
                    <Eye className="w-4 h-4
                                    text-blue-400"/>
                );
            default:
                return (
                    <Clock className="w-4 h-4
                                      text-yellow-400"/>
                );
        }
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'VERIFIED':    return 'border-green-500/30';
            case 'DISMISSED':   return 'border-slate-500/30';
            case 'UNDER_REVIEW':return 'border-blue-500/30';
            default:            return 'border-yellow-500/30';
        }
    };

    return (
        <div className="flex gap-4">

            {/* Timeline Line */}
            <div className="flex flex-col items-center">
                <div className={`w-9 h-9 rounded-full
                                 flex items-center
                                 justify-center
                                 flex-shrink-0
                                 border bg-dark-300
                                 ${getStatusColor(
                                     report.status
                                 )}`}>
                    {getStatusIcon(report.status)}
                </div>
                <div className="w-0.5 bg-white/5
                                flex-1 min-h-4 mt-2"/>
            </div>

            {/* Content */}
            <div className="flex-1 pb-6">
                <div
                    onClick={() => onClick(report)}
                    className="glass-card p-4
                               border border-white/5
                               hover:border-cyan-500/20
                               transition-all cursor-pointer
                               hover:bg-white/3">

                    {/* Header */}
                    <div className="flex items-start
                                    justify-between mb-2">
                        <p className="text-sm font-semibold
                                      text-white flex-1
                                      mr-3">
                            {report.title}
                        </p>
                        <div className="flex items-center
                                        gap-2 flex-shrink-0">
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

                    {/* Type and Date */}
                    <div className="flex items-center
                                    gap-3 text-xs
                                    text-slate-500 mb-3">
                        <span>
                            {formatIncidentType(
                                report.incidentType
                            )}
                        </span>
                        <span>•</span>
                        <span>
                            {timeAgo(report.createdAt)}
                        </span>
                        <span>•</span>
                        <span>
                            Score: {report.riskScore}/100
                        </span>
                    </div>

                    {/* Risk Score Bar */}
                    <div className="w-full h-1.5
                                    rounded-full
                                    bg-white/10">
                        <div
                            className="h-1.5 rounded-full
                                       transition-all"
                            style={{
                                width: `${report.riskScore}%`,
                                background:
                                    report.riskScore >= 70
                                    ? '#ff4757'
                                    : report.riskScore >= 40
                                    ? '#ffd32a'
                                    : '#00ff88',
                            }}
                        />
                    </div>

                    {/* Admin Remarks */}
                    {report.adminRemarks && (
                        <div className="mt-3 pt-3
                                        border-t
                                        border-white/5">
                            <p className="text-xs
                                          text-cyan-400">
                                <span className="text-slate-500
                                                 mr-1">
                                    Admin:
                                </span>
                                {report.adminRemarks}
                            </p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

// ==========================================
// DETAIL MODAL
// ==========================================
const DetailModal = ({ report, onClose }) => {
    const baseUrl = API_BASE_URL.replace('/api', '');
    const imageUrl = report.imagePath
        ? `${baseUrl}${report.imagePath}`
        : null;
    const documentUrl = report.documentPath
        ? `${baseUrl}${report.documentPath}`
        : null;
    const extractUrls = (value) => {
        if (!value) return [];
        const matches = String(value).match(/((https?:\/\/|www\.)[^\s<>"')]+)/gi) || [];
        const unique = [];
        const seen = new Set();
        matches.forEach((item) => {
            const trimmed = item.trim();
            if (!trimmed) return;
            const key = trimmed.toLowerCase();
            if (seen.has(key)) return;
            seen.add(key);
            unique.push(trimmed);
        });
        return unique;
    };
    const normalizeUrl = (value) => (
        /^https?:\/\//i.test(String(value || ''))
            ? String(value)
            : `https://${String(value || '')}`
    );
    const emailDomain = (() => {
        const sender = report.suspiciousEmail || '';
        const match = String(sender).match(/@([a-z0-9.-]+\.[a-z]{2,})/i);
        return match ? match[1].toLowerCase() : '';
    })();
    const extractedUrls = extractUrls(
        [
            report.suspiciousUrl,
            report.suspiciousEmail,
            report.textContent,
            report.inputContent,
            report.aiAnalysis,
        ].filter(Boolean).join(' ')
    );

    const getRiskColor = (level) => {
        switch (level) {
            case 'HIGH':   return '#ff4757';
            case 'MEDIUM': return '#ffd32a';
            default:       return '#00ff88';
        }
    };

    const color = getRiskColor(report.riskLevel);

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
                                        bg-red-500/10
                                        border border-red-500/20
                                        flex items-center
                                        justify-center">
                            <ShieldAlert className="w-5 h-5
                                                    text-red-400"/>
                        </div>
                        <div>
                            <h2 className="text-base font-bold
                                           text-white">
                                Report #{report.id}
                            </h2>
                            <p className="text-xs
                                          text-slate-400">
                                {formatDateTime(
                                    report.createdAt
                                )}
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

                    {/* Title + Badges */}
                    <div>
                        <h3 className="text-base font-bold
                                       text-white mb-3">
                            {report.title}
                        </h3>
                        <div className="flex gap-2
                                        flex-wrap">
                            <span className={
                                getRiskBadgeClass(
                                    report.riskLevel
                                )
                            }>
                                {report.riskLevel} RISK
                            </span>
                            <span className={
                                getStatusBadgeClass(
                                    report.status
                                )
                            }>
                                {report.status}
                            </span>
                            <span className="badge-active">
                                {formatIncidentType(
                                    report.incidentType
                                )}
                            </span>
                        </div>
                    </div>

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
                            {report.description}
                        </p>
                    </div>

                    {/* URL */}
                    {report.suspiciousUrl && (
                        <div className="p-4 rounded-xl
                                        bg-red-500/5
                                        border
                                        border-red-500/20">
                            <div className="flex items-center
                                            gap-2 mb-2">
                                <Link className="w-3 h-3
                                                 text-red-400"/>
                                <p className="text-xs
                                              font-medium
                                              text-red-400
                                              uppercase
                                              tracking-wider">
                                    Suspicious URL
                                </p>
                            </div>
                            <a
                                href={normalizeUrl(report.suspiciousUrl)}
                                target="_blank"
                                rel="noreferrer"
                                onClick={(event) => reportService.handleLinkClick(event, report.suspiciousUrl)}
                                className="text-sm text-red-300
                                           font-mono break-all
                                           underline"
                            >
                                {report.suspiciousUrl}
                            </a>
                        </div>
                    )}

                    {/* Email */}
                    {report.suspiciousEmail && (
                        <div className="p-4 rounded-xl
                                        bg-yellow-500/5
                                        border
                                        border-yellow-500/20">
                            <div className="flex items-center
                                            gap-2 mb-2">
                                <Mail className="w-3 h-3
                                                 text-yellow-400"/>
                                <p className="text-xs
                                              font-medium
                                              text-yellow-400
                                              uppercase
                                              tracking-wider">
                                    Email Content
                                </p>
                            </div>
                            <p className="text-sm
                                          text-yellow-300">
                                {report.suspiciousEmail}
                            </p>
                            {emailDomain && (
                                <a
                                    href={`https://${emailDomain}`}
                                    target="_blank"
                                    rel="noreferrer"
                                    onClick={(event) => reportService.handleLinkClick(event, emailDomain)}
                                    className="text-xs text-yellow-300
                                               underline break-all
                                               inline-block mt-2"
                                >
                                    Sender Domain: {emailDomain}
                                </a>
                            )}
                            {extractUrls(report.suspiciousEmail).map((url, idx) => (
                                <a
                                    key={`${url}-${idx}`}
                                    href={normalizeUrl(url)}
                                    target="_blank"
                                    rel="noreferrer"
                                    onClick={(event) => reportService.handleLinkClick(event, url)}
                                    className="text-xs text-yellow-300
                                               underline break-all
                                               block mt-1"
                                >
                                    {url}
                                </a>
                            ))}
                        </div>
                    )}

                    {imageUrl && (
                        <div className="p-4 rounded-xl
                                        bg-cyan-500/5
                                        border border-cyan-500/20">
                            <p className="text-xs font-medium
                                          text-cyan-400
                                          uppercase tracking-wider mb-2">
                                Screenshot Preview
                            </p>
                            <img
                                src={imageUrl}
                                alt="Threat screenshot"
                                className="w-full max-h-64 object-contain
                                           rounded-lg border border-white/10"
                            />
                        </div>
                    )}

                    {documentUrl && (
                        <div className="p-4 rounded-xl
                                        bg-blue-500/5
                                        border border-blue-500/20">
                            <p className="text-xs font-medium
                                          text-blue-400
                                          uppercase tracking-wider mb-2">
                                Document
                            </p>
                            <a
                                href={documentUrl}
                                target="_blank"
                                rel="noreferrer"
                                onClick={(event) => reportService.handleLinkClick(event, documentUrl)}
                                className="text-sm text-blue-300 underline break-all">
                                View / Download Document
                            </a>
                        </div>
                    )}

                    {(String(report.type || '').toUpperCase() === 'DOCUMENT'
                        || String(report.type || '').toUpperCase() === 'IMAGE') && extractedUrls.length > 0 && (
                        <div className="p-4 rounded-xl
                                        bg-blue-500/5
                                        border border-blue-500/20">
                            <p className="text-xs font-medium
                                          text-blue-400
                                          uppercase tracking-wider mb-2">
                                Extracted Links
                            </p>
                            <div className="space-y-1">
                                {extractedUrls.map((url, idx) => (
                                    <a
                                        key={`${url}-${idx}`}
                                        href={normalizeUrl(url)}
                                        target="_blank"
                                        rel="noreferrer"
                                        onClick={(event) => reportService.handleLinkClick(event, url)}
                                        className="text-xs text-blue-300
                                                   underline break-all block"
                                    >
                                        {url}
                                    </a>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* AI Analysis */}
                    {report.aiAnalysis && (
                        <div className="p-4 rounded-xl
                                        bg-purple-500/5
                                        border
                                        border-purple-500/20">
                            <div className="flex items-center
                                            justify-between
                                            mb-3">
                                <div className="flex items-center
                                                gap-2">
                                    <Brain className="w-3 h-3
                                                      text-purple-400"/>
                                    <p className="text-xs
                                                  font-medium
                                                  text-purple-400
                                                  uppercase
                                                  tracking-wider">
                                        AI Analysis
                                    </p>
                                </div>
                                <span className="text-lg
                                                 font-bold
                                                 text-white">
                                    {report.riskScore}/100
                                </span>
                            </div>
                            <div className="w-full h-2
                                            rounded-full
                                            bg-white/10 mb-3">
                                <div
                                    className="h-2 rounded-full"
                                    style={{
                                        width: `${report.riskScore}%`,
                                        background: color,
                                    }}
                                />
                            </div>
                            <p className="text-xs
                                          text-purple-300
                                          leading-relaxed">
                                {report.aiAnalysis}
                            </p>
                        </div>
                    )}

                    {/* Admin Remarks */}
                    {report.adminRemarks && (
                        <div className="p-4 rounded-xl
                                        bg-cyan-500/5
                                        border
                                        border-cyan-500/20">
                            <p className="text-xs font-medium
                                          text-cyan-400
                                          uppercase
                                          tracking-wider mb-2">
                                Admin Remarks
                            </p>
                            <p className="text-sm
                                          text-cyan-300">
                                {report.adminRemarks}
                            </p>
                            {report.verifiedAt && (
                                <p className="text-xs
                                              text-slate-500
                                              mt-2">
                                    Reviewed:{' '}
                                    {formatDateTime(
                                        report.verifiedAt
                                    )}
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
// STUDENT THREAT HISTORY PAGE
// ==========================================
const StudentThreatHistory = () => {

    const navigate = useNavigate();

    const [reports,      setReports]
        = useState([]);
    const [filtered,     setFiltered]
        = useState([]);
    const [isLoading,    setIsLoading]
        = useState(true);
    const [searchQuery,  setSearchQuery]
        = useState('');
    const [statusFilter, setStatusFilter]
        = useState('ALL');
    const [selectedReport,
           setSelectedReport] = useState(null);

    // ==========================================
    // FETCH
    // ==========================================
    const fetchReports = async () => {
        setIsLoading(true);
        try {
            const response = await reportService
                .getMyReports();
            if (response.success) {
                setReports(response.reports || []);
                setFiltered(response.reports || []);
            }
        } catch (error) {
            console.error('Error:', error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchReports();
    }, []);

    // ==========================================
    // FILTER
    // ==========================================
    useEffect(() => {
        let result = [...reports];

        if (searchQuery) {
            result = result.filter(r =>
                r.title?.toLowerCase()
                 .includes(searchQuery.toLowerCase())
            );
        }

        if (statusFilter !== 'ALL') {
            result = result.filter(
                r => r.status === statusFilter
            );
        }

        setFiltered(result);
    }, [searchQuery, statusFilter, reports]);

    // ==========================================
    // STATS
    // ==========================================
    const stats = {
        total:    reports.length,
        pending:  reports.filter(
                      r => r.status === 'PENDING'
                  ).length,
        verified: reports.filter(
                      r => r.status === 'VERIFIED'
                  ).length,
        high:     reports.filter(
                      r => r.riskLevel === 'HIGH'
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
                            My Threat History
                        </h1>
                        <p className="text-slate-400
                                      text-sm mt-1">
                            Timeline of your submitted reports
                        </p>
                    </div>
                </div>
                <button
                    onClick={fetchReports}
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
                        label: 'Total Submitted',
                        value: stats.total,
                        color: 'text-cyan-400',
                        border:'border-cyan-500/10',
                    },
                    {
                        label: 'Pending',
                        value: stats.pending,
                        color: 'text-yellow-400',
                        border:'border-yellow-500/10',
                    },
                    {
                        label: 'Verified',
                        value: stats.verified,
                        color: 'text-green-400',
                        border:'border-green-500/10',
                    },
                    {
                        label: 'High Risk',
                        value: stats.high,
                        color: 'text-red-400',
                        border:'border-red-500/10',
                    },
                ].map((stat, i) => (
                    <div key={i}
                         className={`glass-card p-5
                                     border ${stat.border}
                                     text-center`}>
                        <p className={`text-2xl font-bold
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
                            placeholder="Search history..."
                            className="cyber-input pl-9
                                       h-9 text-sm"
                        />
                    </div>
                    <select
                        value={statusFilter}
                        onChange={(e) =>
                            setStatusFilter(e.target.value)
                        }
                        className="cyber-input h-9 text-sm
                                   w-40 cursor-pointer">
                        <option value="ALL">All Status</option>
                        <option value="PENDING">
                            Pending
                        </option>
                        <option value="UNDER_REVIEW">
                            Under Review
                        </option>
                        <option value="VERIFIED">
                            Verified
                        </option>
                        <option value="DISMISSED">
                            Dismissed
                        </option>
                    </select>
                    <p className="text-xs text-slate-400
                                  ml-auto">
                        {filtered.length} records
                    </p>
                </div>
            </div>

            {/* Timeline */}
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
                        No history found
                    </p>
                </div>
            ) : (
                <div className="glass-card p-6">
                    <div className="space-y-0">
                        {filtered.map(report => (
                            <TimelineItem
                                key={report.id}
                                report={report}
                                onClick={setSelectedReport}
                            />
                        ))}
                    </div>
                </div>
            )}

            {/* Detail Modal */}
            {selectedReport && (
                <DetailModal
                    report={selectedReport}
                    onClose={() =>
                        setSelectedReport(null)
                    }
                />
            )}
        </div>
    );
};

export default StudentThreatHistory;
