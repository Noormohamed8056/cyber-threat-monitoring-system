import React, { useState, useEffect } from 'react';
import {
    ShieldAlert,
    Search,
    Filter,
    Eye,
    CheckCircle,
    XCircle,
    Clock,
    AlertTriangle,
    RefreshCw,
    ChevronDown,
    X,
    FileText,
    User,
    Calendar,
    Link,
    Mail,
    Brain,
} from 'lucide-react';
import reportService from '../../services/reportService';
import { API_BASE_URL } from '../../utils/constants';
import {
    formatDateTime,
    formatTimeIST,
    getRiskBadgeClass,
    getStatusBadgeClass,
    formatIncidentType,
    timeAgo,
    truncateText,
} from '../../utils/helpers';

// ==========================================
// REPORT DETAIL MODAL
// ==========================================
const ReportDetailModal = ({
    report,
    onClose,
    onStartReview,
    onVerify,
    onMonitor,
    onDismiss,
}) => {
    const baseUrl = API_BASE_URL.replace('/api', '');
    const imageUrl = report.imagePath
        ? `${baseUrl}${report.imagePath}`
        : null;
    const documentUrl = report.documentPath
        ? `${baseUrl}${report.documentPath}`
        : null;

    const [remarks,    setRemarks]    = useState('');
    const [actionType, setActionType] = useState('');
    const [isLoading,  setIsLoading]  = useState(false);
    const mlScore = Math.round(report.mlScore || 0);
    const aiScore = Math.round(report.aiScore || report.riskScore || 0);
    const finalScore = Math.round(report.finalScore || report.riskScore || 0);
    const threatType = report.prediction || report.incidentType || report.type || 'N/A';
    const cleanText = (value, max = 220) => {
        if (!value) return 'N/A';
        const cleaned = String(value).replace(/\s+/g, ' ').trim();
        return cleaned.length > max ? `${cleaned.slice(0, max)}...` : cleaned;
    };
    const getFileName = (pathValue) => {
        if (!pathValue) return 'N/A';
        const parts = String(pathValue).split('/');
        return parts[parts.length - 1] || 'N/A';
    };
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
    const inputContent = (() => {
        const type = String(report.type || '').toUpperCase();
        if (type === 'URL') {
            return cleanText(report.suspiciousUrl || report.inputContent, 220);
        }
        if (type === 'EMAIL') {
            return cleanText(report.inputContent || report.textContent || report.suspiciousEmail, 220);
        }
        if (type === 'DOCUMENT') {
            return cleanText(
                report.inputContent
                || report.textContent
                || `Document Link: ${report.documentPath || getFileName(report.documentPath)}`,
                260
            );
        }
        if (type === 'IMAGE') {
            return cleanText(
                report.inputContent
                || report.textContent
                || `Image Link: ${report.imagePath || getFileName(report.imagePath)}`,
                260
            );
        }
        return cleanText(report.inputContent || report.textContent || report.suspiciousUrl, 220);
    })();
    const renderInputContent = () => {
        const type = String(report.type || '').toUpperCase();
        if (type === 'URL' && report.suspiciousUrl) {
            return (
                <a
                    href={normalizeUrl(report.suspiciousUrl)}
                    target="_blank"
                    rel="noreferrer"
                    onClick={(event) => reportService.handleLinkClick(event, report.suspiciousUrl)}
                    className="text-white underline break-all"
                >
                    {report.suspiciousUrl}
                </a>
            );
        }
        if (type === 'EMAIL') {
            const linkList = extractUrls(report.suspiciousEmail || report.textContent || report.inputContent);
            return (
                <div className="space-y-1">
                    <p className="text-white break-all">{inputContent}</p>
                    {emailDomain && (
                        <a
                            href={`https://${emailDomain}`}
                            target="_blank"
                            rel="noreferrer"
                            onClick={(event) => reportService.handleLinkClick(event, emailDomain)}
                            className="text-yellow-300 underline break-all text-xs inline-block"
                        >
                            Sender Domain: {emailDomain}
                        </a>
                    )}
                    {linkList.map((url, idx) => (
                        <a
                            key={`${url}-${idx}`}
                            href={normalizeUrl(url)}
                            target="_blank"
                            rel="noreferrer"
                            onClick={(event) => reportService.handleLinkClick(event, url)}
                            className="text-yellow-300 underline break-all text-xs block"
                        >
                            {url}
                        </a>
                    ))}
                </div>
            );
        }
        if (type === 'DOCUMENT' || type === 'IMAGE') {
            return (
                <div className="space-y-1">
                    <p className="text-white break-all">{inputContent}</p>
                    {extractedUrls.map((url, idx) => (
                        <a
                            key={`${url}-${idx}`}
                            href={normalizeUrl(url)}
                            target="_blank"
                            rel="noreferrer"
                            onClick={(event) => reportService.handleLinkClick(event, url)}
                            className="text-blue-300 underline break-all text-xs block"
                        >
                            {url}
                        </a>
                    ))}
                </div>
            );
        }
        return <p className="text-white break-all">{inputContent}</p>;
    };
    const adminStatus = report.verificationStatus || report.status || 'N/A';
    const alertStatus = report.alertStatus || 'NONE';
    const scoreColor = finalScore >= 70 ? '#ff4757' : finalScore >= 40 ? '#ffd32a' : '#00ff88';
    const timeline = report.investigationTimeline || [];
    const breakdown = report.riskBreakdown || {};
    const indicators = (report.aiAnalysis || '')
        .split(/[\n.;]+/)
        .map(line => line.trim())
        .filter(line =>
            /keyword|pattern|sandbox|ml|login|domain|phish|malware|anomaly|spread/i
                .test(line)
        )
        .slice(0, 5);

    const handleAction = async () => {
        if (!remarks.trim()) {
            alert('Please add admin remarks');
            return;
        }
        setIsLoading(true);
        try {
            if (actionType === 'verify') {
                await onVerify(report.id, remarks);
            } else if (actionType === 'monitor') {
                await onMonitor(report.id, remarks);
            } else if (actionType === 'dismiss') {
                await onDismiss(report.id, remarks);
            }
            onClose();
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
            <div className="glass-card w-full max-w-2xl
                            max-h-[90vh] overflow-y-auto
                            border border-white/10">

                {/* Modal Header */}
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
                            <h2 className="text-lg font-bold
                                           text-white">
                                Incident Report
                            </h2>
                            <p className="text-xs
                                          text-slate-400">
                                ID: #{report.id}
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

                {/* Modal Body */}
                <div className="p-6 space-y-6">

                    {/* Title and Badges */}
                    <div>
                        <h3 className="text-base font-semibold
                                       text-white mb-3">
                            {report.title}
                        </h3>
                        <div className="flex items-center
                                        gap-2 flex-wrap">
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

                    {/* Info Grid */}
                    <div className="grid grid-cols-2 gap-4">
                        <div className="p-3 rounded-xl
                                        bg-white/3
                                        border border-white/5">
                            <div className="flex items-center
                                            gap-2 mb-1">
                                <User className="w-3 h-3
                                                 text-slate-400"/>
                                <p className="text-xs
                                              text-slate-400">
                                    Reported By
                                </p>
                            </div>
                            <p className="text-sm font-medium
                                          text-white">
                                {report.reportedBy?.fullName
                                    || 'Unknown'}
                            </p>
                            <p className="text-xs
                                          text-slate-500">
                                {report.reportedBy?.email}
                            </p>
                        </div>

                        <div className="p-3 rounded-xl
                                        bg-white/3
                                        border border-white/5">
                            <div className="flex items-center
                                            gap-2 mb-1">
                                <Calendar className="w-3 h-3
                                                     text-slate-400"/>
                                <p className="text-xs
                                              text-slate-400">
                                    Submitted
                                </p>
                            </div>
                            <p className="text-sm font-medium
                                          text-white">
                                {formatDateTime(
                                    report.createdAt
                                )}
                            </p>
                            <p className="text-xs
                                          text-slate-500">
                                {timeAgo(report.createdAt)}
                            </p>
                        </div>
                    </div>

                    {/* Description */}
                    <div className="p-4 rounded-xl
                                    bg-white/3
                                    border border-white/5">
                        <div className="flex items-center
                                        gap-2 mb-2">
                            <FileText className="w-3 h-3
                                                 text-slate-400"/>
                            <p className="text-xs font-medium
                                          text-slate-400
                                          uppercase
                                          tracking-wider">
                                Description
                            </p>
                        </div>
                        <p className="text-sm text-slate-300
                                      leading-relaxed">
                            {report.description}
                        </p>
                    </div>

                    {/* Suspicious URL */}
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
                                className="text-sm text-blue-300 underline">
                                View / Download Document
                            </a>
                        </div>
                    )}

                    {/* Suspicious Email */}
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
                                    Suspicious Email Content
                                </p>
                            </div>
                            <p className="text-sm
                                          text-yellow-300
                                          leading-relaxed">
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

                    {/* AI Analysis */}
                    {(report.aiAnalysis || report.prediction || report.riskScore || report.finalScore) && (
                        <div className="p-4 rounded-xl
                                        bg-purple-500/5
                                        border
                                        border-purple-500/20">
                            <div className="flex items-center
                                            justify-between
                                            mb-3">
                                <div className="flex items-center gap-2">
                                    <Brain className="w-3 h-3
                                                      text-purple-400"/>
                                    <p className="text-xs
                                                  font-medium
                                                  text-purple-400
                                                  uppercase
                                                  tracking-wider">
                                        AI Threat Analysis
                                    </p>
                                </div>
                                <span className="text-lg
                                                 font-bold
                                                 text-white">
                                    {finalScore}/100
                                </span>
                            </div>

                            <div className="w-full h-2
                                            rounded-full
                                            bg-white/10 mb-3">
                                <div
                                    className="h-2 rounded-full
                                               transition-all"
                                    style={{
                                        width: `${finalScore}%`,
                                        background: scoreColor
                                    }}
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-x-3 gap-y-2 text-sm mb-4">
                                <p className="text-slate-400">Prediction</p>
                                <p className="text-white font-medium">{report.prediction || 'N/A'}</p>
                                <p className="text-slate-400">ML Score</p>
                                <p className="text-cyan-300">{mlScore}/100</p>
                                <p className="text-slate-400">AI Rule Score</p>
                                <p className="text-purple-300">{aiScore}/100</p>
                                <p className="text-slate-400">Final Score</p>
                                <p className="text-orange-300 font-semibold">{finalScore}/100</p>
                                <p className="text-slate-400">Risk Level</p>
                                <p className="text-white">{report.riskLevel || 'N/A'}</p>
                                <p className="text-slate-400">Threat Type</p>
                                <p className="text-white">{threatType}</p>
                                <p className="text-slate-400">Input Content</p>
                                <div className="text-white break-all">{renderInputContent()}</div>
                                <p className="text-slate-400">Digital Twin Result</p>
                                <p className="text-white">Impact {report.impactLevel || 'LOW'}, Predicted Spread {report.predictedSpread || 0}</p>
                                <p className="text-slate-400">Spread Analysis</p>
                                <p className="text-white">Current {report.currentSpread || 0}, Predicted {report.predictedSpread || 0}</p>
                                <p className="text-slate-400">Admin Status</p>
                                <p className="text-white">{adminStatus}</p>
                                <p className="text-slate-400">Admin Decision</p>
                                <p className="text-white">{report.adminDecision || 'PENDING'}</p>
                                <p className="text-slate-400">Alert Status</p>
                                <p className="text-white">{alertStatus}</p>
                            </div>

                            <p className="text-xs text-slate-400 uppercase tracking-wider mb-2">
                                Detected Indicators
                            </p>
                            {indicators.length > 0 ? (
                                <div className="space-y-1">
                                    {indicators.map((line, idx) => (
                                        <p key={idx} className="text-xs text-purple-200">- {line}</p>
                                    ))}
                                </div>
                            ) : (
                                <p className="text-xs text-purple-200">
                                    No major indicators extracted
                                </p>
                            )}

                            <div className="mt-4 p-3 rounded-lg bg-white/5 border border-white/10">
                                <p className="text-xs text-slate-400 uppercase tracking-wider mb-2">
                                    Risk Breakdown
                                </p>
                                <p className="text-xs text-slate-300">
                                    ML {Math.round(breakdown.mlScore ?? mlScore)}/100,
                                    AI {Math.round(breakdown.aiScore ?? aiScore)}/100,
                                    Weights ML {(breakdown.indicatorWeights?.mlWeight ?? 0.7) * 100}% + AI {(breakdown.indicatorWeights?.aiWeight ?? 0.3) * 100}%,
                                    Final {Math.round(breakdown.finalScore ?? finalScore)}/100
                                </p>
                            </div>

                            {timeline.length > 0 && (
                                <div className="mt-3">
                                    <p className="text-xs text-slate-400 uppercase tracking-wider mb-2">
                                        Investigation Timeline
                                    </p>
                                    <div className="space-y-1">
                                        {timeline.slice(0, 7).map((item, idx) => (
                                            <p key={`${item.step}-${idx}`} className="text-xs text-slate-300">
                                                - {item.step} ({item.timestamp ? formatTimeIST(item.timestamp) : 'N/A'})
                                            </p>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </div>
                    )}

                    {/* Admin Remarks (if exists) */}
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
                        </div>
                    )}

                    {/* Action Buttons */}
                    {report.status === 'PENDING' && (
                        <button
                            onClick={() =>
                                onStartReview(report.id)
                            }
                            className="w-full py-3 rounded-xl
                                       bg-cyan-500/10
                                       border border-cyan-500/30
                                       text-cyan-400
                                       hover:bg-cyan-500/20
                                       transition-all
                                       text-sm font-medium
                                       flex items-center
                                       justify-center gap-2">
                            <Eye className="w-4 h-4"/>
                            Start Review
                        </button>
                    )}

                    {/* Verify / Dismiss Actions */}
                    {report.status === 'UNDER_REVIEW' && (
                        <div className="space-y-3">
                            <textarea
                                value={remarks}
                                onChange={(e) =>
                                    setRemarks(e.target.value)
                                }
                                placeholder="Add admin remarks (required)"
                                className="cyber-input
                                           resize-none h-24
                                           text-sm"
                            />

                            {/* Action Type Selector */}
                            <div className="grid grid-cols-3
                                            gap-3">
                                <button
                                    onClick={() =>
                                        setActionType('verify')
                                    }
                                    className={`py-2.5 rounded-xl
                                                border text-sm
                                                font-medium
                                                transition-all
                                                flex items-center
                                                justify-center
                                                gap-2
                                                ${actionType === 'verify'
                                                    ? 'bg-green-500/20 border-green-500/40 text-green-400'
                                                    : 'bg-white/5 border-white/10 text-slate-400 hover:border-green-500/30'
                                                }`}>
                                    <CheckCircle className="w-4 h-4"/>
                                    Confirm Threat
                                </button>
                                <button
                                    onClick={() =>
                                        setActionType('monitor')
                                    }
                                    className={`py-2.5 rounded-xl
                                                border text-sm
                                                font-medium
                                                transition-all
                                                flex items-center
                                                justify-center
                                                gap-2
                                                ${actionType === 'monitor'
                                                    ? 'bg-yellow-500/20 border-yellow-500/40 text-yellow-300'
                                                    : 'bg-white/5 border-white/10 text-slate-400 hover:border-yellow-500/30'
                                                }`}>
                                    <Clock className="w-4 h-4"/>
                                    Monitor
                                </button>
                                <button
                                    onClick={() =>
                                        setActionType('dismiss')
                                    }
                                    className={`py-2.5 rounded-xl
                                                border text-sm
                                                font-medium
                                                transition-all
                                                flex items-center
                                                justify-center
                                                gap-2
                                                ${actionType === 'dismiss'
                                                    ? 'bg-red-500/20 border-red-500/40 text-red-400'
                                                    : 'bg-white/5 border-white/10 text-slate-400 hover:border-red-500/30'
                                                }`}>
                                    <XCircle className="w-4 h-4"/>
                                    Mark Safe
                                </button>
                            </div>

                            {actionType && (
                                <button
                                    onClick={handleAction}
                                    disabled={isLoading}
                                    className={`w-full py-3
                                                rounded-xl
                                                text-sm
                                                font-semibold
                                                transition-all
                                                disabled:opacity-50
                                                ${actionType === 'verify'
                                                    ? 'btn-success'
                                                    : actionType === 'monitor'
                                                    ? 'bg-yellow-500/20 border border-yellow-500/40 text-yellow-200 hover:bg-yellow-500/30'
                                                    : 'btn-danger'
                                                }`}>
                                    {isLoading
                                        ? 'Processing...'
                                        : actionType === 'verify'
                                        ? 'Confirm Threat'
                                        : actionType === 'monitor'
                                        ? 'Monitor'
                                        : 'Mark Safe'
                                    }
                                </button>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

// ==========================================
// INCIDENT REVIEW PAGE
// ==========================================
const IncidentReview = () => {

    const [reports,       setReports]
        = useState([]);
    const [filtered,      setFiltered]
        = useState([]);
    const [isLoading,     setIsLoading]
        = useState(true);
    const [searchQuery,   setSearchQuery]
        = useState('');
    const [statusFilter,  setStatusFilter]
        = useState('ALL');
    const [riskFilter,    setRiskFilter]
        = useState('ALL');
    const [selectedReport,setSelectedReport]
        = useState(null);

    // ==========================================
    // FETCH REPORTS
    // ==========================================
    const fetchReports = async () => {
        setIsLoading(true);
        try {
            const response = await reportService
                .getAllReports();
            if (response.success) {
                setReports(response.reports || []);
                setFiltered(response.reports || []);
            }
        } catch (error) {
            console.error('Error fetching reports:', error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchReports();
    }, []);

    // ==========================================
    // FILTER REPORTS
    // ==========================================
    useEffect(() => {
        let result = [...reports];

        if (searchQuery) {
            result = result.filter(r =>
                r.title?.toLowerCase()
                 .includes(searchQuery.toLowerCase()) ||
                r.description?.toLowerCase()
                 .includes(searchQuery.toLowerCase())
            );
        }

        if (statusFilter !== 'ALL') {
            result = result.filter(
                r => r.status === statusFilter
            );
        }

        if (riskFilter !== 'ALL') {
            result = result.filter(
                r => r.riskLevel === riskFilter
            );
        }

        setFiltered(result);
    }, [searchQuery, statusFilter, riskFilter, reports]);

    // ==========================================
    // HANDLE START REVIEW
    // ==========================================
    const handleStartReview = async (id) => {
        try {
            await reportService.startReview(id);
            await fetchReports();
            if (selectedReport?.id === id) {
                const updated = reports.find(
                    r => r.id === id
                );
                setSelectedReport({
                    ...updated,
                    status: 'UNDER_REVIEW'
                });
            }
        } catch (error) {
            console.error('Start review error:', error);
        }
    };

    // ==========================================
    // HANDLE VERIFY
    // ==========================================
    const handleVerify = async (id, remarks) => {
        try {
            await reportService.verifyReport(id, remarks);
            await fetchReports();
        } catch (error) {
            console.error('Verify error:', error);
        }
    };

    // ==========================================
    // HANDLE DISMISS
    // ==========================================
    const handleDismiss = async (id, remarks) => {
        try {
            await reportService.dismissReport(id, remarks);
            await fetchReports();
        } catch (error) {
            console.error('Dismiss error:', error);
        }
    };

    const handleMonitor = async (id, remarks) => {
        try {
            await reportService.monitorReport(id, remarks);
            await fetchReports();
        } catch (error) {
            console.error('Monitor error:', error);
        }
    };

    // ==========================================
    // STATS
    // ==========================================
    const stats = {
        total:       reports.length,
        pending:     reports.filter(
                         r => r.status === 'PENDING'
                     ).length,
        underReview: reports.filter(
                         r => r.status === 'UNDER_REVIEW'
                     ).length,
        verified:    reports.filter(
                         r => r.status === 'VERIFIED'
                     ).length,
        high:        reports.filter(
                         r => r.riskLevel === 'HIGH'
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
                        Incident Review
                    </h1>
                    <p className="text-slate-400
                                  text-sm mt-1">
                        Review and manage reported threats
                    </p>
                </div>
                <button
                    onClick={fetchReports}
                    className="flex items-center gap-2
                               px-4 py-2 rounded-xl
                               bg-white/5
                               border border-white/10
                               text-slate-300
                               hover:text-cyan-400
                               hover:border-cyan-500/30
                               transition-all text-sm">
                    <RefreshCw className="w-4 h-4"/>
                    Refresh
                </button>
            </div>

            {/* Stats Row */}
            <div className="grid grid-cols-2
                            lg:grid-cols-5 gap-4">
                {[
                    {
                        label: 'Total',
                        value: stats.total,
                        color: 'text-cyan-400',
                        bg:    'bg-cyan-500/10',
                    },
                    {
                        label: 'Pending',
                        value: stats.pending,
                        color: 'text-yellow-400',
                        bg:    'bg-yellow-500/10',
                    },
                    {
                        label: 'Under Review',
                        value: stats.underReview,
                        color: 'text-blue-400',
                        bg:    'bg-blue-500/10',
                    },
                    {
                        label: 'Verified',
                        value: stats.verified,
                        color: 'text-green-400',
                        bg:    'bg-green-500/10',
                    },
                    {
                        label: 'High Risk',
                        value: stats.high,
                        color: 'text-red-400',
                        bg:    'bg-red-500/10',
                    },
                ].map((stat, i) => (
                    <div key={i}
                         className="glass-card p-4
                                    text-center">
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

                    {/* Search */}
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
                            placeholder="Search reports..."
                            className="cyber-input pl-9
                                       h-9 text-sm"
                        />
                    </div>

                    {/* Status Filter */}
                    <select
                        value={statusFilter}
                        onChange={(e) =>
                            setStatusFilter(e.target.value)
                        }
                        className="cyber-input h-9
                                   text-sm w-40
                                   cursor-pointer">
                        <option value="ALL">All Status</option>
                        <option value="PENDING">Pending</option>
                        <option value="UNDER_REVIEW">
                            Under Review
                        </option>
                        <option value="VERIFIED">Verified</option>
                        <option value="DISMISSED">
                            Dismissed
                        </option>
                    </select>

                    {/* Risk Filter */}
                    <select
                        value={riskFilter}
                        onChange={(e) =>
                            setRiskFilter(e.target.value)
                        }
                        className="cyber-input h-9
                                   text-sm w-36
                                   cursor-pointer">
                        <option value="ALL">All Risk</option>
                        <option value="LOW">Low</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="HIGH">High</option>
                        <option value="CRITICAL">Critical</option>
                    </select>

                    <p className="text-xs text-slate-400
                                  ml-auto">
                        {filtered.length} reports
                    </p>
                </div>
            </div>

            {/* Reports Table */}
            <div className="glass-card overflow-hidden">
                {isLoading ? (
                    <div className="flex items-center
                                    justify-center py-16">
                        <RefreshCw className="w-6 h-6
                                              text-cyan-400
                                              animate-spin"/>
                    </div>
                ) : filtered.length === 0 ? (
                    <div className="text-center py-16">
                        <ShieldAlert className="w-12 h-12
                                                text-slate-600
                                                mx-auto mb-3"/>
                        <p className="text-slate-400
                                      text-sm">
                            No reports found
                        </p>
                    </div>
                ) : (
                    <table className="cyber-table">
                        <thead>
                            <tr>
                                <th>Report</th>
                                <th>Reporter</th>
                                <th>Type</th>
                                <th>Risk</th>
                                <th>Score</th>
                                <th>Status</th>
                                <th>Date</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filtered.map((report) => (
                                <tr key={report.id}>
                                    <td>
                                        <p className="font-medium
                                                      text-white
                                                      text-sm">
                                            {truncateText(
                                                report.title,
                                                35
                                            )}
                                        </p>
                                        <p className="text-xs
                                                      text-slate-500
                                                      mt-0.5">
                                            #{report.id}
                                        </p>
                                    </td>
                                    <td>
                                        <p className="text-sm
                                                      text-slate-300">
                                            {report.reportedBy
                                                ?.fullName
                                                || 'Unknown'}
                                        </p>
                                        <p className="text-xs
                                                      text-slate-500">
                                            {report.reportedBy
                                                ?.institutionName}
                                        </p>
                                    </td>
                                    <td>
                                        <span className="text-xs
                                                         text-slate-400">
                                            {formatIncidentType(
                                                report.incidentType
                                            )}
                                        </span>
                                    </td>
                                    <td>
                                        <span className={
                                            getRiskBadgeClass(
                                                report.riskLevel
                                            )
                                        }>
                                            {report.riskLevel}
                                        </span>
                                    </td>
                                    <td>
                                        <div className="flex
                                                        items-center
                                                        gap-2">
                                            <div className="w-16
                                                            h-1.5
                                                            rounded-full
                                                            bg-white/10">
                                                <div
                                                    className="h-1.5
                                                               rounded-full"
                                                    style={{
                                                        width: `${report.riskScore}%`,
                                                        background:
                                                            report.riskScore >= 70
                                                            ? '#ff4757'
                                                            : report.riskScore >= 40
                                                            ? '#ffd32a'
                                                            : '#00ff88'
                                                    }}
                                                />
                                            </div>
                                            <span className="text-xs
                                                             text-slate-400">
                                                {report.riskScore}
                                            </span>
                                        </div>
                                    </td>
                                    <td>
                                        <span className={
                                            getStatusBadgeClass(
                                                report.status
                                            )
                                        }>
                                            {report.status}
                                        </span>
                                    </td>
                                    <td>
                                        <span className="text-xs
                                                         text-slate-400">
                                            {timeAgo(
                                                report.createdAt
                                            )}
                                        </span>
                                    </td>
                                    <td>
                                        <button
                                            onClick={() =>
                                                setSelectedReport(
                                                    report
                                                )
                                            }
                                            className="flex
                                                       items-center
                                                       gap-1.5
                                                       px-3 py-1.5
                                                       rounded-lg
                                                       bg-cyan-500/10
                                                       border
                                                       border-cyan-500/20
                                                       text-cyan-400
                                                       hover:bg-cyan-500/20
                                                       transition-all
                                                       text-xs
                                                       font-medium">
                                            <Eye className="w-3 h-3"/>
                                            Review
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>

            {/* Report Detail Modal */}
            {selectedReport && (
                <ReportDetailModal
                    report={selectedReport}
                    onClose={() => setSelectedReport(null)}
                    onStartReview={handleStartReview}
                    onVerify={handleVerify}
                    onMonitor={handleMonitor}
                    onDismiss={handleDismiss}
                />
            )}
        </div>
    );
};

export default IncidentReview;
