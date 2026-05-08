import React, { useEffect, useState } from 'react';
import { useNavigate }      from 'react-router-dom';
import {
    Shield,
    Link,
    Mail,
    FileText,
    Image,
    AlertTriangle,
    CheckCircle,
    Loader2,
    ArrowLeft,
    ArrowRight,
    Upload,
    X,
    Brain,
    Eye,
    TrendingUp,
    Zap,
    Target,
    Clock,
} from 'lucide-react';
import api from '../../services/api';

const ANALYSIS_STEPS = [
    '🔍 Analyzing input...',
    '📊 Extracting features...',
    '🧠 Running ML model...',
    '⚠ Predicting threat...',
];
const MIN_ANALYSIS_DURATION_MS = 10000;

// ==========================================
// INPUT TYPE DEFINITIONS
// ==========================================
const INPUT_TYPES = [
    {
        id:          'url',
        icon:        Link,
        emoji:       '🔗',
        title:       'Suspicious URL',
        description: 'Analyze malicious links',
        color:       'text-cyan-400',
        bg:          'bg-cyan-500/10',
        border:      'border-cyan-500/30',
        activeBg:    'bg-cyan-500/20',
    },
    {
        id:          'email',
        icon:        Mail,
        emoji:       '📧',
        title:       'Phishing Email',
        description: 'Analyze email content',
        color:       'text-red-400',
        bg:          'bg-red-500/10',
        border:      'border-red-500/30',
        activeBg:    'bg-red-500/20',
    },
    {
        id:          'document',
        icon:        FileText,
        emoji:       '📄',
        title:       'Suspicious Document',
        description: 'Analyze PDF or Word files',
        color:       'text-yellow-400',
        bg:          'bg-yellow-500/10',
        border:      'border-yellow-500/30',
        activeBg:    'bg-yellow-500/20',
    },
    {
        id:          'image',
        icon:        Image,
        emoji:       '🖼️',
        title:       'Screenshot / Image',
        description: 'Analyze phishing screenshots',
        color:       'text-purple-400',
        bg:          'bg-purple-500/10',
        border:      'border-purple-500/30',
        activeBg:    'bg-purple-500/20',
    },
];

// ==========================================
// RISK LEVEL HELPER
// ==========================================
const getRiskColor = (level) => {
    switch (level?.toUpperCase()) {
        case 'CRITICAL': return 'text-red-400';
        case 'HIGH':     return 'text-orange-400';
        case 'MEDIUM':   return 'text-yellow-400';
        case 'LOW':      return 'text-green-400';
        default:         return 'text-slate-400';
    }
};

const getRiskBg = (level) => {
    switch (level?.toUpperCase()) {
        case 'CRITICAL':
            return 'bg-red-500/10 border-red-500/30';
        case 'HIGH':
            return 'bg-orange-500/10 border-orange-500/30';
        case 'MEDIUM':
            return 'bg-yellow-500/10 border-yellow-500/30';
        case 'LOW':
            return 'bg-green-500/10 border-green-500/30';
        default:
            return 'bg-slate-500/10 border-slate-500/30';
    }
};

// ==========================================
// STEP 1: INPUT TYPE SELECTOR
// ==========================================
const InputTypeSelector = ({
    selected,
    onSelect,
}) => (
    <div className="space-y-6">
        <div className="text-center">
            <h2 className="text-xl font-bold
                           text-white mb-2">
                What are you reporting?
            </h2>
            <p className="text-slate-400 text-sm">
                Select the type of threat
                you encountered
            </p>
        </div>

        <div className="grid grid-cols-2
                        lg:grid-cols-4 gap-4">
            {INPUT_TYPES.map(type => (
                <button
                    key={type.id}
                    onClick={() =>
                        onSelect(type.id)
                    }
                    className={`p-5 rounded-2xl
                                border-2 text-center
                                transition-all
                                duration-200
                                hover:scale-105
                                ${selected === type.id
                                    ? `${type.activeBg} ${type.border} scale-105`
                                    : 'border-white/10 bg-white/3 hover:border-white/20'
                                }`}>
                    <div className="text-4xl mb-3">
                        {type.emoji}
                    </div>
                    <p className={`text-sm
                                   font-bold mb-1
                                   ${selected === type.id
                                       ? type.color
                                       : 'text-white'
                                   }`}>
                        {type.title}
                    </p>
                    <p className="text-xs
                                  text-slate-500">
                        {type.description}
                    </p>
                </button>
            ))}
        </div>

        {/* Info Banner */}
        <div className="glass-card p-4
                        border border-cyan-500/10
                        flex items-start gap-3">
            <Brain className="w-5 h-5
                              text-cyan-400
                              flex-shrink-0 mt-0.5"/>
            <div>
                <p className="text-sm font-semibold
                              text-white mb-1">
                    AI-Powered Analysis
                </p>
                <p className="text-xs text-slate-400
                              leading-relaxed">
                    Our intelligence pipeline
                    analyzes your submission through
                    9 stages including threat
                    classification, attack stage
                    detection, spread simulation
                    and autonomous response.
                </p>
            </div>
        </div>
    </div>
);

// ==========================================
// STEP 2: INPUT FORMS
// ==========================================

// URL FORM
const UrlForm = ({ data, onChange }) => (
    <div className="space-y-4">
        <div>
            <label className="block text-sm
                              font-medium
                              text-slate-300 mb-2">
                Report Title *
            </label>
            <input
                type="text"
                value={data.title}
                onChange={e =>
                    onChange('title',
                        e.target.value)
                }
                placeholder="e.g. Suspicious PayPal link"
                className="cyber-input text-sm"
            />
        </div>
        <div>
            <label className="block text-sm
                              font-medium
                              text-slate-300 mb-2">
                Suspicious URL *
            </label>
            <div className="relative">
                <Link className="absolute left-3
                                 top-1/2
                                 -translate-y-1/2
                                 w-4 h-4
                                 text-slate-400"/>
                <input
                    type="text"
                    value={data.url}
                    onChange={e =>
                        onChange('url',
                            e.target.value)
                    }
                    placeholder="https://suspicious-site.com"
                    className="cyber-input text-sm
                               pl-10"
                />
            </div>
            <p className="text-xs text-slate-500
                          mt-1">
                Paste the full URL exactly
                as you received it
            </p>
        </div>
        <div>
            <label className="block text-sm
                              font-medium
                              text-slate-300 mb-2">
                Description
            </label>
            <textarea
                value={data.description}
                onChange={e =>
                    onChange('description',
                        e.target.value)
                }
                placeholder="How did you encounter this URL? Any context..."
                rows={3}
                className="cyber-input text-sm
                           resize-none"
            />
        </div>
    </div>
);

// EMAIL FORM
const EmailForm = ({ data, onChange }) => (
    <div className="space-y-4">
        <div>
            <label className="block text-sm
                              font-medium
                              text-slate-300 mb-2">
                Report Title *
            </label>
            <input
                type="text"
                value={data.title}
                onChange={e =>
                    onChange('title',
                        e.target.value)
                }
                placeholder="e.g. Phishing email from fake PayPal"
                className="cyber-input text-sm"
            />
        </div>
        <div>
            <label className="block text-sm
                              font-medium
                              text-slate-300 mb-2">
                Sender Email Address
            </label>
            <div className="relative">
                <Mail className="absolute left-3
                                 top-1/2
                                 -translate-y-1/2
                                 w-4 h-4
                                 text-slate-400"/>
                <input
                    type="email"
                    value={data.emailSender}
                    onChange={e =>
                        onChange('emailSender',
                            e.target.value)
                    }
                    placeholder="suspicious@sender.com"
                    className="cyber-input text-sm
                               pl-10"
                />
            </div>
        </div>
        <div>
            <label className="block text-sm
                              font-medium
                              text-slate-300 mb-2">
                Email Subject
            </label>
            <input
                type="text"
                value={data.emailSubject}
                onChange={e =>
                    onChange('emailSubject',
                        e.target.value)
                }
                placeholder="URGENT: Verify your account now!"
                className="cyber-input text-sm"
            />
        </div>
        <div>
            <label className="block text-sm
                              font-medium
                              text-slate-300 mb-2">
                Email Body *
            </label>
            <textarea
                value={data.emailBody}
                onChange={e =>
                    onChange('emailBody',
                        e.target.value)
                }
                placeholder="Paste the full email content here..."
                rows={5}
                className="cyber-input text-sm
                           resize-none"
            />
        </div>
    </div>
);

// DOCUMENT FORM
const DocumentForm = ({ data, onChange }) => {

    const handleFileDrop = (e) => {
        e.preventDefault();
        const file = e.dataTransfer.files[0];
        if (file) onChange('file', file);
    };

    const handleFileSelect = (e) => {
        const file = e.target.files[0];
        if (file) onChange('file', file);
    };

    return (
        <div className="space-y-4">
            <div>
                <label className="block text-sm
                                  font-medium
                                  text-slate-300 mb-2">
                    Report Title *
                </label>
                <input
                    type="text"
                    value={data.title}
                    onChange={e =>
                        onChange('title',
                            e.target.value)
                    }
                    placeholder="e.g. Suspicious invoice document"
                    className="cyber-input text-sm"
                />
            </div>

            {/* Drop Zone */}
            <div>
                <label className="block text-sm
                                  font-medium
                                  text-slate-300 mb-2">
                    Document File *
                </label>
                <div
                    onDrop={handleFileDrop}
                    onDragOver={e =>
                        e.preventDefault()
                    }
                    className={`border-2 border-dashed
                                rounded-xl p-8
                                text-center
                                transition-all
                                cursor-pointer
                                ${data.file
                                    ? 'border-yellow-500/40 bg-yellow-500/5'
                                    : 'border-white/10 hover:border-yellow-500/30 hover:bg-yellow-500/3'
                                }`}
                    onClick={() =>
                        document.getElementById(
                            'doc-file-input'
                        ).click()
                    }>
                    <input
                        id="doc-file-input"
                        type="file"
                        className="hidden"
                        accept=".pdf,.doc,.docx,
                                 .xls,.xlsx,.txt"
                        onChange={handleFileSelect}
                    />
                    {data.file ? (
                        <div className="space-y-2">
                            <FileText className="w-10
                                                 h-10
                                                 text-yellow-400
                                                 mx-auto"/>
                            <p className="text-sm
                                          font-medium
                                          text-yellow-400">
                                {data.file.name}
                            </p>
                            <p className="text-xs
                                          text-slate-500">
                                {(data.file.size / 1024)
                                    .toFixed(1)} KB
                            </p>
                            <button
                                type="button"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onChange(
                                        'file', null
                                    );
                                }}
                                className="flex items-center
                                           gap-1 text-xs
                                           text-red-400
                                           hover:text-red-300
                                           mx-auto
                                           transition-colors">
                                <X className="w-3 h-3"/>
                                Remove
                            </button>
                        </div>
                    ) : (
                        <div className="space-y-2">
                            <Upload className="w-10
                                               h-10
                                               text-slate-500
                                               mx-auto"/>
                            <p className="text-sm
                                          text-slate-400">
                                Drop file here or
                                click to browse
                            </p>
                            <p className="text-xs
                                          text-slate-600">
                                PDF, DOC, DOCX,
                                XLS, XLSX, TXT
                                (max 10MB)
                            </p>
                        </div>
                    )}
                </div>
            </div>

            <div>
                <label className="block text-sm
                                  font-medium
                                  text-slate-300 mb-2">
                    Description
                </label>
                <textarea
                    value={data.description}
                    onChange={e =>
                        onChange('description',
                            e.target.value)
                    }
                    placeholder="Where did you receive this document? Any context..."
                    rows={3}
                    className="cyber-input text-sm
                               resize-none"
                />
            </div>
        </div>
    );
};

// IMAGE FORM
const ImageForm = ({ data, onChange }) => {

    const [preview, setPreview] = useState(null);

    const handleFileSelect = (e) => {
        const file = e.target.files[0];
        if (file) {
            onChange('image', file);
            const reader = new FileReader();
            reader.onload = (ev) =>
                setPreview(ev.target.result);
            reader.readAsDataURL(file);
        }
    };

    const handleRemove = (e) => {
        e.stopPropagation();
        onChange('image', null);
        setPreview(null);
    };

    return (
        <div className="space-y-4">
            <div>
                <label className="block text-sm
                                  font-medium
                                  text-slate-300 mb-2">
                    Report Title *
                </label>
                <input
                    type="text"
                    value={data.title}
                    onChange={e =>
                        onChange('title',
                            e.target.value)
                    }
                    placeholder="e.g. Fake bank login page screenshot"
                    className="cyber-input text-sm"
                />
            </div>

            {/* Image Drop Zone */}
            <div>
                <label className="block text-sm
                                  font-medium
                                  text-slate-300 mb-2">
                    Image / Screenshot *
                </label>
                <div
                    className={`border-2 border-dashed
                                rounded-xl
                                transition-all
                                cursor-pointer
                                overflow-hidden
                                ${preview
                                    ? 'border-purple-500/40'
                                    : 'border-white/10 hover:border-purple-500/30'
                                }`}
                    onClick={() =>
                        document.getElementById(
                            'img-file-input'
                        ).click()
                    }>
                    <input
                        id="img-file-input"
                        type="file"
                        className="hidden"
                        accept="image/*"
                        onChange={handleFileSelect}
                    />
                    {preview ? (
                        <div className="relative">
                            <img
                                src={preview}
                                alt="Preview"
                                className="w-full
                                           max-h-48
                                           object-contain
                                           bg-black/20"
                            />
                            <button
                                type="button"
                                onClick={handleRemove}
                                className="absolute
                                           top-2 right-2
                                           w-7 h-7
                                           rounded-full
                                           bg-red-500/80
                                           flex items-center
                                           justify-center
                                           text-white
                                           hover:bg-red-500
                                           transition-all">
                                <X className="w-3 h-3"/>
                            </button>
                            <div className="p-3
                                            border-t
                                            border-white/5">
                                <p className="text-xs
                                              text-purple-400
                                              font-medium">
                                    {data.image?.name}
                                </p>
                            </div>
                        </div>
                    ) : (
                        <div className="p-8 text-center
                                        space-y-2">
                            <Image className="w-10
                                              h-10
                                              text-slate-500
                                              mx-auto"/>
                            <p className="text-sm
                                          text-slate-400">
                                Drop screenshot here
                                or click to browse
                            </p>
                            <p className="text-xs
                                          text-slate-600">
                                JPG, PNG, GIF, BMP,
                                WEBP (max 10MB)
                            </p>
                        </div>
                    )}
                </div>
            </div>

            <div>
                <label className="block text-sm
                                  font-medium
                                  text-slate-300 mb-2">
                    What does this image show? *
                </label>
                <textarea
                    value={data.description}
                    onChange={e =>
                        onChange('description',
                            e.target.value)
                    }
                    placeholder="Describe what you see in this image — fake login page, suspicious popup, etc."
                    rows={3}
                    className="cyber-input text-sm
                               resize-none"
                />
            </div>
        </div>
    );
};

// ==========================================
// STEP 3: AI RESULTS DISPLAY
// ==========================================
const AIResultsDisplay = ({ result }) => {

    const intel = result?.intelligence;
    const report = result?.report;

    if (!intel) return null;

    const riskLevel = report?.riskLevel || 'MEDIUM';
    const mlScore = report?.mlScore ?? report?.confidence ?? 0;
    const aiScore = report?.aiScore ?? report?.riskScore ?? 0;
    const finalScore = report?.finalScore ?? report?.riskScore ?? 0;
    const detectedIndicators = intel?.detectedIndicators
        || intel?.topRedFlags
        || [];
    const timeline = report?.investigationTimeline
        || intel?.investigationTimeline
        || [];
    const breakdown = report?.riskBreakdown
        || intel?.riskBreakdown
        || {};

    return (
        <div className="space-y-5 animate-fade-in">

            {/* Success Header */}
            <div className="text-center py-4">
                <div className="w-16 h-16 rounded-full
                                bg-green-500/20
                                border border-green-500/30
                                flex items-center
                                justify-center
                                mx-auto mb-4">
                    <CheckCircle className="w-8 h-8
                                            text-green-400"/>
                </div>
                <h2 className="text-xl font-bold
                               text-white mb-1">
                    Analysis Complete!
                </h2>
                <p className="text-slate-400 text-sm">
                    Report #{result.reportId} submitted
                    and analyzed
                </p>
            </div>

            {/* Structured Analysis Card */}
            <div className={`glass-card p-6 border ${getRiskBg(riskLevel)}`}>
                <p className="text-sm font-bold text-white mb-4">
                    Structured AI Analysis
                </p>
                <div className="grid grid-cols-2 gap-2 text-sm">
                    <p className="text-slate-400">Prediction</p>
                    <p className="text-white">{report?.prediction || intel?.prediction || 'N/A'}</p>
                    <p className="text-slate-400">ML Score</p>
                    <p className="text-cyan-300">{Math.round(mlScore)}/100</p>
                    <p className="text-slate-400">AI Rule Score</p>
                    <p className="text-purple-300">{Math.round(aiScore)}/100</p>
                    <p className="text-slate-400">Final Score</p>
                    <p className="text-orange-300">{Math.round(finalScore)}/100</p>
                    <p className="text-slate-400">Risk Level</p>
                    <p className={`${getRiskColor(riskLevel)} font-semibold`}>{riskLevel}</p>
                    <p className="text-slate-400">Threat Type</p>
                    <p className="text-white">{intel?.threatCategory?.replace(/_/g, ' ') || 'UNKNOWN'}</p>
                    <p className="text-slate-400">Digital Twin Result</p>
                    <p className="text-white">{intel?.digitalTwinResult || 'No significant anomaly detected'}</p>
                    <p className="text-slate-400">Spread Analysis</p>
                    <p className="text-white">
                        Current {report?.currentSpread || 0}, Predicted {report?.predictedSpread || 0}
                    </p>
                    <p className="text-slate-400">Admin Status</p>
                    <p className="text-white">
                        {report?.adminStatus || report?.verificationStatus || report?.status}
                    </p>
                </div>
                <div className="mt-4">
                    <p className="text-xs text-slate-400 uppercase tracking-wider mb-2">
                        Detected Indicators
                    </p>
                    {detectedIndicators.length > 0 ? (
                        detectedIndicators.slice(0, 6).map((flag, i) => (
                            <p key={i} className="text-xs text-slate-300">• {flag}</p>
                        ))
                    ) : (
                        <p className="text-xs text-slate-500">No strong indicators found</p>
                    )}
                </div>
            </div>

            {/* Investigation Timeline */}
            {timeline.length > 0 && (
                <div className="glass-card p-4 border border-white/10">
                    <p className="text-sm font-bold text-white mb-3">
                        Investigation Timeline
                    </p>
                    <div className="space-y-2">
                        {timeline.map((item, idx) => (
                            <div key={`${item.step}-${idx}`}
                                 className="flex items-start gap-2 text-xs">
                                <span className="text-cyan-300 mt-0.5">•</span>
                                <div>
                                    <p className="text-slate-200">{item.step}</p>
                                    <p className="text-slate-500">
                                        {item.timestamp ? new Date(item.timestamp).toLocaleString() : 'N/A'}
                                        {' '}• {item.detail || 'Completed'}
                                    </p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Risk Breakdown */}
            <div className="glass-card p-4 border border-white/10">
                <p className="text-sm font-bold text-white mb-3">
                    Risk Breakdown
                </p>
                <div className="grid grid-cols-2 gap-2 text-sm">
                    <p className="text-slate-400">ML Score</p>
                    <p className="text-cyan-300">{Math.round(breakdown.mlScore ?? mlScore)}/100</p>
                    <p className="text-slate-400">AI Score</p>
                    <p className="text-purple-300">{Math.round(breakdown.aiScore ?? aiScore)}/100</p>
                    <p className="text-slate-400">Indicator Weights</p>
                    <p className="text-white">
                        ML {(breakdown.indicatorWeights?.mlWeight ?? 0.7) * 100}%,
                        {' '}AI {(breakdown.indicatorWeights?.aiWeight ?? 0.3) * 100}%
                    </p>
                    <p className="text-slate-400">Final Score</p>
                    <p className="text-orange-300">{Math.round(breakdown.finalScore ?? finalScore)}/100</p>
                </div>
            </div>

            {/* Intelligence Grid */}
            <div className="grid grid-cols-2 gap-4">

                {/* Threat Category */}
                <div className="glass-card p-4
                                border border-white/5">
                    <p className="text-xs
                                  text-slate-400
                                  uppercase
                                  tracking-wider mb-2">
                        Threat Type
                    </p>
                    <p className="text-sm font-bold
                                  text-white">
                        {intel.threatCategory
                            ?.replace(/_/g, ' ')
                            || 'UNKNOWN'}
                    </p>
                    <p className="text-xs
                                  text-slate-500 mt-1">
                        {intel.threatSubCategory
                            ?.replace(/_/g, ' ')
                            || ''}
                    </p>
                </div>

                {/* Attack Stage */}
                <div className="glass-card p-4
                                border border-white/5">
                    <p className="text-xs
                                  text-slate-400
                                  uppercase
                                  tracking-wider mb-2">
                        Attack Stage
                    </p>
                    <p className="text-sm font-bold
                                  text-white">
                        {intel.attackStage
                            ?.replace(/_/g, ' ')
                            || 'UNKNOWN'}
                    </p>
                    {intel.isActiveAttack && (
                        <p className="text-xs
                                      text-red-400
                                      mt-1
                                      flex items-center
                                      gap-1">
                            <Zap className="w-3 h-3"/>
                            Active Attack
                        </p>
                    )}
                </div>

                {/* Confidence */}
                <div className="glass-card p-4
                                border border-white/5">
                    <p className="text-xs
                                  text-slate-400
                                  uppercase
                                  tracking-wider mb-2">
                        Confidence
                    </p>
                    <p className="text-sm font-bold
                                  text-cyan-400">
                        {intel.confidenceScore}
                    </p>
                    <p className="text-xs
                                  text-slate-500 mt-1">
                        Detection accuracy
                    </p>
                </div>

                <div className="glass-card p-4
                                border border-white/5">
                    <p className="text-xs
                                  text-slate-400
                                  uppercase
                                  tracking-wider mb-2">
                        ML Prediction
                    </p>
                    <p className="text-sm font-bold
                                  text-white">
                        {report?.prediction || intel.mlPrediction || 'N/A'}
                    </p>
                    <p className="text-xs text-slate-500 mt-1">
                        {report?.confidence
                            ? `${Math.round(report.confidence)}% confidence`
                            : 'Model confidence unavailable'}
                    </p>
                </div>

                {/* Spread */}
                <div className="glass-card p-4
                                border border-white/5">
                    <p className="text-xs
                                  text-slate-400
                                  uppercase
                                  tracking-wider mb-2">
                        Spread Risk
                    </p>
                    <p className="text-sm font-bold
                                  text-orange-400">
                        ~{intel.estimatedAffectedUsers}
                        {' '}users
                    </p>
                    <p className="text-xs
                                  text-slate-500 mt-1">
                        {Math.round(
                            (intel.spreadProbability
                                || 0) * 100
                        )}% probability
                    </p>
                </div>
            </div>

            {/* Pattern Match */}
            {intel.patternMatched && (
                <div className="glass-card p-4
                                border
                                border-orange-500/20
                                bg-orange-500/5
                                flex items-center
                                gap-3">
                    <Target className="w-5 h-5
                                       text-orange-400
                                       flex-shrink-0"/>
                    <div>
                        <p className="text-sm
                                      font-bold
                                      text-orange-400">
                            Known Pattern Matched
                        </p>
                        <p className="text-xs
                                      text-slate-400">
                            {intel.patternName}
                        </p>
                    </div>
                </div>
            )}

            {/* Anomaly Detection */}
            {intel.anomalyDetected && (
                <div className="glass-card p-4
                                border
                                border-purple-500/20
                                bg-purple-500/5
                                flex items-center
                                gap-3">
                    <Brain className="w-5 h-5
                                      text-purple-400
                                      flex-shrink-0"/>
                    <div>
                        <p className="text-sm
                                      font-bold
                                      text-purple-400">
                            Behavioral Anomaly
                            Detected
                        </p>
                        <p className="text-xs
                                      text-slate-400">
                            Your digital twin detected
                            unusual behavior patterns
                        </p>
                    </div>
                </div>
            )}

            {/* Auto Response */}
            {intel.autoResponseTriggered && (
                <div className="glass-card p-4
                                border
                                border-red-500/20
                                bg-red-500/5">
                    <p className="text-sm font-bold
                                  text-red-400 mb-2
                                  flex items-center
                                  gap-2">
                        <Zap className="w-4 h-4"/>
                        Autonomous Response Activated
                    </p>
                    <div className="space-y-1">
                        {intel.autoResponseActions
                            ?.map((action, i) => (
                            <p key={i}
                               className="text-xs
                                          text-slate-400
                                          flex items-center
                                          gap-2">
                                <span className="w-1.5
                                                 h-1.5
                                                 rounded-full
                                                 bg-red-400
                                                 flex-shrink-0"/>
                                {action}
                            </p>
                        ))}
                    </div>
                </div>
            )}

            {/* Top Red Flags */}
            {intel.topRedFlags?.length > 0 && (
                <div className="glass-card p-4
                                border border-white/5">
                    <p className="text-xs font-bold
                                  text-white uppercase
                                  tracking-wider mb-3
                                  flex items-center
                                  gap-2">
                        <AlertTriangle className="w-4
                                                   h-4
                                                   text-yellow-400"/>
                        Red Flags Detected (
                        {intel.redFlagCount})
                    </p>
                    <div className="space-y-2">
                        {intel.topRedFlags
                            .map((flag, i) => (
                            <div key={i}
                                 className="flex items-start
                                            gap-2 text-xs
                                            text-slate-400">
                                <span className="text-red-400
                                                 flex-shrink-0
                                                 mt-0.5">
                                    •
                                </span>
                                {flag}
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Recommendations */}
            {intel.recommendations?.length > 0 && (
                <div className="glass-card p-4
                                border border-white/5">
                    <p className="text-xs font-bold
                                  text-white uppercase
                                  tracking-wider mb-3
                                  flex items-center
                                  gap-2">
                        <Shield className="w-4 h-4
                                           text-cyan-400"/>
                        Recommendations
                    </p>
                    <div className="space-y-2">
                        {intel.recommendations
                            .map((rec, i) => (
                            <p key={i}
                               className="text-xs
                                          text-slate-300
                                          leading-relaxed
                                          flex items-start
                                          gap-2">
                                <CheckCircle
                                    className="w-3.5
                                               h-3.5
                                               text-green-400
                                               flex-shrink-0
                                               mt-0.5"/>
                                {rec}
                            </p>
                        ))}
                    </div>
                </div>
            )}

            {/* Timeline */}
            {intel.timeline?.length > 0 && (
                <div className="glass-card p-4
                                border border-white/5">
                    <p className="text-xs font-bold
                                  text-white uppercase
                                  tracking-wider mb-3
                                  flex items-center
                                  gap-2">
                        <Clock className="w-4 h-4
                                          text-blue-400"/>
                        Attack Timeline
                    </p>
                    <div className="space-y-3">
                        {intel.timeline
                            .slice(0, 4)
                            .map((event, i) => (
                            <div key={i}
                                 className="flex items-start
                                            gap-3">
                                <div className={`w-2 h-2
                                                 rounded-full
                                                 flex-shrink-0
                                                 mt-1.5
                                                 ${event.severity
                                                     === 'HIGH'
                                                     ? 'bg-red-400'
                                                     : event.severity
                                                       === 'MEDIUM'
                                                     ? 'bg-yellow-400'
                                                     : 'bg-cyan-400'
                                                 }`}/>
                                <div>
                                    <p className="text-xs
                                                  font-medium
                                                  text-white">
                                        {event.eventTitle}
                                    </p>
                                    <p className="text-xs
                                                  text-slate-500
                                                  mt-0.5">
                                        {event.eventDescription
                                            ?.substring(0, 80)}
                                        ...
                                    </p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Processing Info */}
            <div className="text-center">
                <p className="text-xs text-slate-600">
                    Processed in{' '}
                    {intel.processingTimeMs}ms
                    through 9-stage AI pipeline
                </p>
            </div>
        </div>
    );
};

// ==========================================
// MAIN REPORT THREAT PAGE
// ==========================================
const ReportThreat = () => {

    const navigate = useNavigate();

    // Step management
    const [currentStep,
           setCurrentStep] = useState(1);

    // Input type selection
    const [inputType,
           setInputType]   = useState(null);

    // Form data per type
    const [urlData, setUrlData] = useState({
        title:       '',
        url:         '',
        description: '',
    });

    const [emailData, setEmailData] = useState({
        title:        '',
        emailSender:  '',
        emailSubject: '',
        emailBody:    '',
        description:  '',
    });

    const [docData, setDocData] = useState({
        title:       '',
        file:        null,
        description: '',
    });

    const [imgData, setImgData] = useState({
        title:       '',
        image:       null,
        description: '',
    });

    // Submission state
    const [isSubmitting,
           setIsSubmitting] = useState(false);
    const [error,
           setError]        = useState('');
    const [result,
           setResult]       = useState(null);
    const [showAnalysisModal,
           setShowAnalysisModal] = useState(false);
    const [analysisStep,
           setAnalysisStep] = useState(0);

    useEffect(() => {
        if (!isSubmitting) return;
        const timer = setInterval(() => {
            setAnalysisStep(prev =>
                prev < ANALYSIS_STEPS.length - 1
                    ? prev + 1
                    : prev
            );
        }, Math.ceil(MIN_ANALYSIS_DURATION_MS / ANALYSIS_STEPS.length));
        return () => clearInterval(timer);
    }, [isSubmitting]);

    // ==========================================
    // FORM CHANGE HANDLERS
    // ==========================================
    const handleUrlChange = (key, val) =>
        setUrlData(prev => ({
            ...prev, [key]: val
        }));

    const handleEmailChange = (key, val) =>
        setEmailData(prev => ({
            ...prev, [key]: val
        }));

    const handleDocChange = (key, val) =>
        setDocData(prev => ({
            ...prev, [key]: val
        }));

    const handleImgChange = (key, val) =>
        setImgData(prev => ({
            ...prev, [key]: val
        }));

    // ==========================================
    // VALIDATION
    // ==========================================
    const validateStep2 = () => {
        switch (inputType) {
            case 'url':
                if (!urlData.url.trim()) {
                    setError(
                        'Please enter a URL'
                    );
                    return false;
                }
                if (!urlData.title.trim()) {
                    setError(
                        'Please enter a title'
                    );
                    return false;
                }
                break;
            case 'email':
                if (!emailData.emailBody.trim()) {
                    setError(
                        'Please paste the email body'
                    );
                    return false;
                }
                if (!emailData.title.trim()) {
                    setError(
                        'Please enter a title'
                    );
                    return false;
                }
                break;
            case 'document':
                if (!docData.file) {
                    setError(
                        'Please select a file'
                    );
                    return false;
                }
                if (!docData.title.trim()) {
                    setError(
                        'Please enter a title'
                    );
                    return false;
                }
                break;
            case 'image':
                if (!imgData.image) {
                    setError(
                        'Please select an image'
                    );
                    return false;
                }
                if (!imgData.title.trim()) {
                    setError(
                        'Please enter a title'
                    );
                    return false;
                }
                break;
            default:
                break;
        }
        setError('');
        return true;
    };

    // ==========================================
    // SUBMIT HANDLER
    // ==========================================
    const handleSubmit = async () => {
        if (!validateStep2()) return;

        setIsSubmitting(true);
        setShowAnalysisModal(true);
        setAnalysisStep(0);
        setError('');

        try {
            let requestPromise;
            const waitForAnalysis = new Promise(resolve => {
                setTimeout(resolve, MIN_ANALYSIS_DURATION_MS);
            });

            switch (inputType) {

                case 'url':
                    requestPromise = api.post(
                        '/reports/intelligent/url',
                        {
                            title:        urlData.title,
                            description:  urlData.description,
                            suspiciousUrl:urlData.url,
                            incidentType: 'PHISHING',
                        }
                    );
                    break;

                case 'email':
                    requestPromise = api.post(
                        '/reports/intelligent/email',
                        {
                            title:        emailData.title,
                            emailSubject: emailData.emailSubject,
                            emailBody:    emailData.emailBody,
                            emailSender:  emailData.emailSender,
                            description:  emailData.description,
                        }
                    );
                    break;

                case 'document': {
                    const formData = new FormData();
                    formData.append(
                        'file', docData.file
                    );
                    formData.append(
                        'title', docData.title
                    );
                    formData.append(
                        'description',
                        docData.description
                    );
                    requestPromise = api.post(
                        '/reports/intelligent/document',
                        formData,
                        {
                            headers: {
                                'Content-Type':
                                    'multipart/form-data'
                            }
                        }
                    );
                    break;
                }

                case 'image': {
                    const formData = new FormData();
                    formData.append(
                        'image', imgData.image
                    );
                    formData.append(
                        'title', imgData.title
                    );
                    formData.append(
                        'description',
                        imgData.description
                    );
                    requestPromise = api.post(
                        '/reports/intelligent/image',
                        formData,
                        {
                            headers: {
                                'Content-Type':
                                    'multipart/form-data'
                            }
                        }
                    );
                    break;
                }

                default:
                    break;
            }

            if (!requestPromise) {
                throw new Error('No submission request built');
            }

            const [response] = await Promise.all([
                requestPromise,
                waitForAnalysis
            ]);

            if (response?.data?.success) {
                setResult(response.data);
                setCurrentStep(3);
            } else {
                setError(
                    response?.data?.message
                    || 'Submission failed'
                );
            }

        } catch (err) {
            setError(
                err.response?.data?.message
                || 'Submission failed. '
                + 'Please try again.'
            );
        } finally {
            setIsSubmitting(false);
            setShowAnalysisModal(false);
        }
    };

    // ==========================================
    // STEP INDICATOR
    // ==========================================
    const steps = [
        { n: 1, label: 'Choose Type' },
        { n: 2, label: 'Enter Details' },
        { n: 3, label: 'AI Results'   },
    ];

    // ==========================================
    // RENDER
    // ==========================================
    return (
        <div className="space-y-6
                        animate-fade-in
                        max-w-2xl mx-auto">
            {showAnalysisModal && (
                <div className="fixed inset-0 z-50
                                flex items-center justify-center
                                p-4"
                     style={{
                         background: 'rgba(2,6,23,0.8)',
                         backdropFilter: 'blur(4px)'
                     }}>
                    <div className="glass-card border border-cyan-500/20"
                         style={{
                             width: '500px',
                             padding: '24px'
                         }}>
                        <p className="text-lg font-bold text-cyan-300 mb-3">
                            🤖 AI Threat Analysis
                        </p>
                        <div className="flex items-center gap-3 mb-4">
                            <Loader2 className="w-5 h-5 text-cyan-400 animate-spin"/>
                            <p className="text-sm text-slate-200">
                                {ANALYSIS_STEPS[analysisStep]}
                            </p>
                        </div>
                        <div className="space-y-2">
                            {ANALYSIS_STEPS.map((step, idx) => (
                                <p key={step}
                                   className={`text-xs ${
                                       idx <= analysisStep
                                           ? 'text-cyan-300'
                                           : 'text-slate-500'
                                   }`}>
                                    {idx <= analysisStep ? '• ' : ''}
                                    {step}
                                </p>
                            ))}
                        </div>
                    </div>
                </div>
            )}

            {/* Header */}
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
                        Report Threat
                    </h1>
                    <p className="text-slate-400
                                  text-sm">
                        AI-powered multi-input
                        threat analysis
                    </p>
                </div>
            </div>

            {/* Step Indicator */}
            <div className="flex items-center
                            justify-center gap-0">
                {steps.map((step, i) => (
                    <React.Fragment key={step.n}>
                        <div className="flex flex-col
                                        items-center">
                            <div className={`w-9 h-9
                                             rounded-full
                                             flex items-center
                                             justify-center
                                             text-sm font-bold
                                             border-2
                                             transition-all
                                             ${currentStep
                                               >= step.n
                                                 ? 'bg-cyan-500 border-cyan-500 text-dark-500'
                                                 : 'border-white/20 text-slate-500'
                                             }`}>
                                {currentStep > step.n
                                    ? <CheckCircle
                                        className="w-4 h-4"/>
                                    : step.n
                                }
                            </div>
                            <p className={`text-xs mt-1
                                           ${currentStep
                                             >= step.n
                                               ? 'text-cyan-400'
                                               : 'text-slate-600'
                                           }`}>
                                {step.label}
                            </p>
                        </div>
                        {i < steps.length - 1 && (
                            <div className={`w-16 h-0.5
                                             mb-5
                                             transition-all
                                             ${currentStep
                                               > step.n
                                                 ? 'bg-cyan-500'
                                                 : 'bg-white/10'
                                             }`}/>
                        )}
                    </React.Fragment>
                ))}
            </div>

            {/* Main Card */}
            <div className="glass-card p-6
                            border border-white/5">

                {/* STEP 1: Type Selection */}
                {currentStep === 1 && (
                    <InputTypeSelector
                        selected={inputType}
                        onSelect={setInputType}
                    />
                )}

                {/* STEP 2: Input Form */}
                {currentStep === 2 && (
                    <div>
                        {/* Type Badge */}
                        <div className="flex items-center
                                        gap-2 mb-6">
                            {(() => {
                                const type =
                                    INPUT_TYPES.find(
                                        t =>
                                        t.id === inputType
                                    );
                                return type ? (
                                    <div className={`flex items-center
                                                     gap-2 px-3 py-1.5
                                                     rounded-full
                                                     border text-xs
                                                     font-medium
                                                     ${type.bg}
                                                     ${type.border}
                                                     ${type.color}`}>
                                        <span>
                                            {type.emoji}
                                        </span>
                                        {type.title}
                                    </div>
                                ) : null;
                            })()}
                        </div>

                        {/* Form */}
                        {inputType === 'url' && (
                            <UrlForm
                                data={urlData}
                                onChange={
                                    handleUrlChange
                                }
                            />
                        )}
                        {inputType === 'email' && (
                            <EmailForm
                                data={emailData}
                                onChange={
                                    handleEmailChange
                                }
                            />
                        )}
                        {inputType === 'document'
                            && (
                            <DocumentForm
                                data={docData}
                                onChange={
                                    handleDocChange
                                }
                            />
                        )}
                        {inputType === 'image' && (
                            <ImageForm
                                data={imgData}
                                onChange={
                                    handleImgChange
                                }
                            />
                        )}

                        {/* Error */}
                        {error && (
                            <div className="mt-4 p-3
                                            rounded-xl
                                            bg-red-500/10
                                            border
                                            border-red-500/30
                                            flex items-center
                                            gap-2">
                                <AlertTriangle
                                    className="w-4 h-4
                                               text-red-400
                                               flex-shrink-0"/>
                                <p className="text-red-400
                                              text-sm">
                                    {error}
                                </p>
                            </div>
                        )}
                    </div>
                )}

                {/* STEP 3: Results */}
                {currentStep === 3 && result && (
                    <AIResultsDisplay
                        result={result}
                    />
                )}
            </div>

            {/* Navigation Buttons */}
            <div className="flex items-center
                            justify-between gap-4">

                {/* Back Button */}
                {currentStep > 1
                 && currentStep < 3 && (
                    <button
                        onClick={() => {
                            setCurrentStep(
                                currentStep - 1
                            );
                            setError('');
                        }}
                        className="flex items-center
                                   gap-2 px-5 py-2.5
                                   rounded-xl
                                   bg-white/5
                                   border border-white/10
                                   text-slate-300
                                   hover:bg-white/10
                                   transition-all
                                   text-sm">
                        <ArrowLeft className="w-4 h-4"/>
                        Back
                    </button>
                )}

                {/* Next / Submit Button */}
                {currentStep === 1 && (
                    <button
                        onClick={() => {
                            if (!inputType) {
                                setError(
                                    'Please select '
                                    + 'an input type'
                                );
                                return;
                            }
                            setCurrentStep(2);
                        }}
                        disabled={!inputType}
                        className="ml-auto btn-cyber
                                   flex items-center
                                   gap-2 px-6 py-2.5
                                   text-sm
                                   disabled:opacity-40
                                   disabled:cursor-not-allowed">
                        Continue
                        <ArrowRight className="w-4 h-4"/>
                    </button>
                )}

                {currentStep === 2 && (
                    <button
                        onClick={handleSubmit}
                        disabled={isSubmitting}
                        className="ml-auto btn-cyber
                                   flex items-center
                                   gap-2 px-6 py-2.5
                                   text-sm
                                   disabled:opacity-50">
                        {isSubmitting ? (
                            <>
                                <Loader2
                                    className="w-4 h-4
                                               animate-spin"/>
                                Analyzing...
                            </>
                        ) : (
                            <>
                                <Brain
                                    className="w-4 h-4"/>
                                Analyze Threat
                            </>
                        )}
                    </button>
                )}

                {currentStep === 3 && (
                    <div className="flex gap-3
                                    w-full">
                        <button
                            onClick={() => {
                                setCurrentStep(1);
                                setInputType(null);
                                setResult(null);
                                setError('');
                                setUrlData({
                                    title: '',
                                    url: '',
                                    description: '',
                                });
                                setEmailData({
                                    title: '',
                                    emailSender: '',
                                    emailSubject: '',
                                    emailBody: '',
                                    description: '',
                                });
                                setDocData({
                                    title: '',
                                    file: null,
                                    description: '',
                                });
                                setImgData({
                                    title: '',
                                    image: null,
                                    description: '',
                                });
                            }}
                            className="flex-1
                                       flex items-center
                                       justify-center
                                       gap-2 py-2.5
                                       rounded-xl
                                       bg-white/5
                                       border border-white/10
                                       text-slate-300
                                       hover:bg-white/10
                                       transition-all
                                       text-sm">
                            Report Another
                        </button>
                        <button
                            onClick={() =>
                                navigate(
                                    '/student/my-reports'
                                )
                            }
                            className="flex-1 btn-cyber
                                       flex items-center
                                       justify-center
                                       gap-2 py-2.5
                                       text-sm">
                            <Eye className="w-4 h-4"/>
                            View My Reports
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ReportThreat;
