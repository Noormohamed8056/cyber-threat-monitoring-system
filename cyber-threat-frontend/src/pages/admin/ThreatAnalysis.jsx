import React, { useState, useEffect } from 'react';
import {
    BarChart3,
    TrendingUp,
    TrendingDown,
    ShieldAlert,
    RefreshCw,
    AlertTriangle,
    CheckCircle,
    Clock,
    Brain,
    Target,
    Activity,
} from 'lucide-react';
import {
    AreaChart,
    Area,
    BarChart,
    Bar,
    PieChart,
    Pie,
    Cell,
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    Legend,
    RadarChart,
    Radar,
    PolarGrid,
    PolarAngleAxis,
} from 'recharts';
import reportService from '../../services/reportService';
import alertService  from '../../services/alertService';

// ==========================================
// DEFAULT DATA FOR CHARTS (API WILL OVERRIDE)
// ==========================================
const defaultWeeklyTrendData = [
    { day: 'Mon', reports: 0, verified: 0, dismissed: 0 },
    { day: 'Tue', reports: 0, verified: 0, dismissed: 0 },
    { day: 'Wed', reports: 0, verified: 0, dismissed: 0 },
    { day: 'Thu', reports: 0, verified: 0, dismissed: 0 },
    { day: 'Fri', reports: 0, verified: 0, dismissed: 0 },
    { day: 'Sat', reports: 0, verified: 0, dismissed: 0 },
    { day: 'Sun', reports: 0, verified: 0, dismissed: 0 },
];

const defaultIncidentTypeData = [
    { name: 'Phishing',      value: 0, color: '#ff4757' },
    { name: 'Malware',       value: 0, color: '#ffd32a' },
    { name: 'Ransomware',    value: 0, color: '#a55eea' },
    { name: 'Data Breach',   value: 0, color: '#00d4ff' },
    { name: 'Unauth Access', value: 0, color: '#00ff88' },
    { name: 'Other',         value: 0, color: '#ff6b35' },
];

const defaultRiskScoreData = [
    { range: '0-20',  count: 0 },
    { range: '21-40', count: 0 },
    { range: '41-60', count: 0 },
    { range: '61-80', count: 0 },
    { range: '81-100',count: 0 },
];

const defaultMonthlyData = [
    { month: 'Oct', high: 0, medium: 0, low: 0 },
    { month: 'Nov', high: 0, medium: 0, low: 0 },
    { month: 'Dec', high: 0, medium: 0, low: 0 },
    { month: 'Jan', high: 0, medium: 0, low: 0 },
    { month: 'Feb', high: 0, medium: 0, low: 0 },
    { month: 'Mar', high: 0, medium: 0, low: 0 },
];

const defaultRadarData = [
    { subject: 'Phishing',    A: 0 },
    { subject: 'Malware',     A: 0 },
    { subject: 'Ransomware',  A: 0 },
    { subject: 'Social Eng',  A: 0 },
    { subject: 'Data Breach', A: 0 },
    { subject: 'WiFi Threats',A: 0 },
];

const defaultResponseTimeData = [
    { week: 'W1', avgTime: 0 },
    { week: 'W2', avgTime: 0 },
    { week: 'W3', avgTime: 0 },
    { week: 'W4', avgTime: 0 },
    { week: 'W5', avgTime: 0 },
    { week: 'W6', avgTime: 0 },
];

// ==========================================
// CUSTOM TOOLTIP
// ==========================================
const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
        return (
            <div className="glass-card p-3
                            border border-white/10">
                <p className="text-xs text-slate-400 mb-2">
                    {label}
                </p>
                {payload.map((entry, i) => (
                    <p key={i}
                       className="text-xs font-medium"
                       style={{ color: entry.color }}>
                        {entry.name}: {entry.value}
                    </p>
                ))}
            </div>
        );
    }
    return null;
};

// ==========================================
// METRIC CARD COMPONENT
// ==========================================
const MetricCard = ({
    title,
    value,
    subtitle,
    icon: Icon,
    color,
    bg,
    border,
    trend,
    trendValue,
}) => (
    <div className={`glass-card p-5 border ${border}`}>
        <div className="flex items-start
                        justify-between mb-3">
            <div className={`w-10 h-10 rounded-xl
                             flex items-center
                             justify-center ${bg}`}>
                <Icon className={`w-5 h-5 ${color}`}/>
            </div>
            {trend && (
                <div className={`flex items-center gap-1
                                 text-xs font-medium
                                 ${trend === 'up'
                                     ? 'text-green-400'
                                     : 'text-red-400'
                                 }`}>
                    {trend === 'up'
                        ? <TrendingUp   className="w-3 h-3"/>
                        : <TrendingDown className="w-3 h-3"/>
                    }
                    {trendValue}
                </div>
            )}
        </div>
        <p className="text-2xl font-bold text-white mb-1">
            {value}
        </p>
        <p className="text-xs font-medium
                      text-slate-300">
            {title}
        </p>
        {subtitle && (
            <p className="text-xs text-slate-500 mt-0.5">
                {subtitle}
            </p>
        )}
    </div>
);

// ==========================================
// THREAT ANALYSIS PAGE
// ==========================================
const ThreatAnalysis = () => {

    const [reportStats, setReportStats] = useState(null);
    const [alertStats,  setAlertStats]  = useState(null);
    const [isLoading,   setIsLoading]   = useState(true);
    const [activeTab,   setActiveTab]   = useState('overview');
    const [weeklyTrendData, setWeeklyTrendData]
        = useState(defaultWeeklyTrendData);
    const [incidentTypeData, setIncidentTypeData]
        = useState(defaultIncidentTypeData);
    const [riskScoreData, setRiskScoreData]
        = useState(defaultRiskScoreData);
    const [monthlyData, setMonthlyData]
        = useState(defaultMonthlyData);
    const [radarData, setRadarData]
        = useState(defaultRadarData);
    const [responseTimeData, setResponseTimeData]
        = useState(defaultResponseTimeData);

    const buildThreatAnalysisFromReports = (reports = []) => {
        const parsed = reports.map(r => ({
            ...r,
            createdAtDate: r.createdAt ? new Date(r.createdAt) : null,
            updatedAtDate: r.updatedAt ? new Date(r.updatedAt) : null,
            verifiedAtDate: r.verifiedAt ? new Date(r.verifiedAt) : null,
        }));

        const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
        const today = new Date();
        const weekData = [];
        for (let i = 6; i >= 0; i--) {
            const d = new Date(today);
            d.setDate(today.getDate() - i);
            const key = d.toDateString();

            const reportsCount = parsed.filter(r =>
                r.createdAtDate
                && r.createdAtDate.toDateString() === key
            ).length;

            const verifiedCount = parsed.filter(r => {
                const t = r.verifiedAtDate || r.updatedAtDate;
                return r.status === 'VERIFIED'
                    && t
                    && t.toDateString() === key;
            }).length;

            const dismissedCount = parsed.filter(r => {
                const t = r.verifiedAtDate || r.updatedAtDate;
                return r.status === 'DISMISSED'
                    && t
                    && t.toDateString() === key;
            }).length;

            weekData.push({
                day: dayNames[d.getDay()],
                reports: reportsCount,
                verified: verifiedCount,
                dismissed: dismissedCount,
            });
        }

        const typeMap = {
            PHISHING_EMAIL: 'Phishing',
            MALWARE: 'Malware',
            RANSOMWARE: 'Ransomware',
            DATA_BREACH: 'Data Breach',
            UNAUTHORIZED_ACCESS: 'Unauth Access',
        };
        const typeCounts = {};
        parsed.forEach(r => {
            const key = typeMap[r.incidentType] || 'Other';
            typeCounts[key] = (typeCounts[key] || 0) + 1;
        });
        const typeTotal = parsed.length || 1;
        const incidentData = [
            { name: 'Phishing', value: Math.round(((typeCounts['Phishing'] || 0) * 100) / typeTotal), color: '#ff4757' },
            { name: 'Malware', value: Math.round(((typeCounts['Malware'] || 0) * 100) / typeTotal), color: '#ffd32a' },
            { name: 'Ransomware', value: Math.round(((typeCounts['Ransomware'] || 0) * 100) / typeTotal), color: '#a55eea' },
            { name: 'Data Breach', value: Math.round(((typeCounts['Data Breach'] || 0) * 100) / typeTotal), color: '#00d4ff' },
            { name: 'Unauth Access', value: Math.round(((typeCounts['Unauth Access'] || 0) * 100) / typeTotal), color: '#00ff88' },
            { name: 'Other', value: Math.round(((typeCounts['Other'] || 0) * 100) / typeTotal), color: '#ff6b35' },
        ];

        const scoreBuckets = {
            '0-20': 0,
            '21-40': 0,
            '41-60': 0,
            '61-80': 0,
            '81-100': 0,
        };
        parsed.forEach(r => {
            const s = Number(r.riskScore || 0);
            if (s <= 20) scoreBuckets['0-20']++;
            else if (s <= 40) scoreBuckets['21-40']++;
            else if (s <= 60) scoreBuckets['41-60']++;
            else if (s <= 80) scoreBuckets['61-80']++;
            else scoreBuckets['81-100']++;
        });
        const riskData = Object.entries(scoreBuckets).map(([range, count]) => ({
            range,
            count,
        }));

        const monthLabels = [];
        const riskMonthData = [];
        for (let i = 5; i >= 0; i--) {
            const d = new Date(today.getFullYear(), today.getMonth() - i, 1);
            const label = d.toLocaleString('en-US', { month: 'short' });
            monthLabels.push({
                year: d.getFullYear(),
                month: d.getMonth(),
                label,
            });
        }
        monthLabels.forEach(({ year, month, label }) => {
            let high = 0;
            let medium = 0;
            let low = 0;
            parsed.forEach(r => {
                if (!r.createdAtDate) return;
                if (r.createdAtDate.getFullYear() !== year) return;
                if (r.createdAtDate.getMonth() !== month) return;
                if (r.riskLevel === 'HIGH' || r.riskLevel === 'CRITICAL') high++;
                else if (r.riskLevel === 'MEDIUM') medium++;
                else low++;
            });
            riskMonthData.push({ month: label, high, medium, low });
        });

        const radarDataLocal = [
            { subject: 'Phishing', A: incidentData[0].value },
            { subject: 'Malware', A: incidentData[1].value },
            { subject: 'Ransomware', A: incidentData[2].value },
            { subject: 'Social Eng', A: Math.round(((typeCounts['Other'] || 0) * 100) / typeTotal) },
            { subject: 'Data Breach', A: incidentData[3].value },
            { subject: 'WiFi Threats', A: incidentData[4].value },
        ];

        const weekStart = new Date(today);
        weekStart.setDate(today.getDate() - today.getDay());
        const responseWeeks = [];
        for (let i = 5; i >= 0; i--) {
            const start = new Date(weekStart);
            start.setDate(weekStart.getDate() - (i * 7));
            const end = new Date(start);
            end.setDate(start.getDate() + 6);

            const durations = parsed
                .filter(r => r.verifiedAtDate && r.createdAtDate)
                .filter(r => r.verifiedAtDate >= start && r.verifiedAtDate <= end)
                .map(r => Math.max(0, (r.verifiedAtDate - r.createdAtDate) / (1000 * 60 * 60)));

            const avgTime = durations.length
                ? Number((durations.reduce((a, b) => a + b, 0) / durations.length).toFixed(1))
                : 0;

            responseWeeks.push({
                week: `W${6 - i}`,
                avgTime,
            });
        }

        return {
            weeklyTrendData: weekData,
            incidentTypeData: incidentData,
            riskScoreData: riskData,
            monthlyData: riskMonthData,
            radarData: radarDataLocal,
            responseTimeData: responseWeeks,
        };
    };

    // ==========================================
    // FETCH DATA
    // ==========================================
    const fetchData = async () => {
        setIsLoading(true);
        try {
            const [rStats, aStats, allReportsRes] = await Promise.all([
                reportService.getReportStats(),
                alertService.getAlertStats(),
                reportService.getAllReports(),
            ]);
            if (rStats.success) {
                setReportStats(rStats.stats);
            }
            if (aStats.success) {
                setAlertStats(aStats.stats);
            }
            if (allReportsRes?.success) {
                const fallback = buildThreatAnalysisFromReports(
                    allReportsRes.reports || []
                );
                setWeeklyTrendData(fallback.weeklyTrendData);
                setIncidentTypeData(fallback.incidentTypeData);
                setRiskScoreData(fallback.riskScoreData);
                setMonthlyData(fallback.monthlyData);
                setRadarData(fallback.radarData);
                setResponseTimeData(fallback.responseTimeData);
            }
        } catch (error) {
            console.error('Error:', error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

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
                        Threat Analysis
                    </h1>
                    <p className="text-slate-400
                                  text-sm mt-1">
                        Deep insights into threat patterns
                        and trends
                    </p>
                </div>
                <button
                    onClick={fetchData}
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

            {/* Tabs */}
            <div className="flex items-center gap-1
                            p-1 rounded-xl bg-white/3
                            border border-white/5 w-fit">
                {[
                    { key: 'overview',  label: '📊 Overview'  },
                    { key: 'trends',    label: '📈 Trends'    },
                    { key: 'breakdown', label: '🎯 Breakdown' },
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

            {/* ============================
                OVERVIEW TAB
                ============================ */}
            {activeTab === 'overview' && (
                <div className="space-y-6">

                    {/* Metric Cards */}
                    <div className="grid grid-cols-2
                                    lg:grid-cols-4 gap-4">
                        <MetricCard
                            title="Total Reports"
                            value={
                                reportStats?.totalReports
                                ?? 0
                            }
                            subtitle="All time"
                            icon={ShieldAlert}
                            color="text-cyan-400"
                            bg="bg-cyan-500/10"
                            border="border-cyan-500/10"
                            trend="up"
                            trendValue="+12%"
                        />
                        <MetricCard
                            title="High Risk"
                            value={
                                reportStats?.highRiskReports
                                ?? 0
                            }
                            subtitle="Needs attention"
                            icon={AlertTriangle}
                            color="text-red-400"
                            bg="bg-red-500/10"
                            border="border-red-500/10"
                            trend="down"
                            trendValue="-5%"
                        />
                        <MetricCard
                            title="Verified"
                            value={
                                reportStats?.verifiedReports
                                ?? 0
                            }
                            subtitle="Confirmed threats"
                            icon={CheckCircle}
                            color="text-green-400"
                            bg="bg-green-500/10"
                            border="border-green-500/10"
                            trend="up"
                            trendValue="+8%"
                        />
                        <MetricCard
                            title="Avg Response"
                            value="3.2h"
                            subtitle="Time to verify"
                            icon={Clock}
                            color="text-purple-400"
                            bg="bg-purple-500/10"
                            border="border-purple-500/10"
                            trend="down"
                            trendValue="-15%"
                        />
                    </div>

                    {/* Charts Row 1 */}
                    <div className="grid grid-cols-1
                                    lg:grid-cols-3 gap-6">

                        {/* Weekly Trend */}
                        <div className="lg:col-span-2
                                        glass-card p-6">
                            <h3 className="text-sm
                                           font-semibold
                                           text-white mb-1">
                                Weekly Report Activity
                            </h3>
                            <p className="text-xs
                                          text-slate-400 mb-6">
                                Reports submitted vs resolved
                            </p>
                            <ResponsiveContainer
                                width="100%"
                                height={220}>
                                <AreaChart
                                    data={weeklyTrendData}>
                                    <defs>
                                        <linearGradient
                                            id="repGrad"
                                            x1="0" y1="0"
                                            x2="0" y2="1">
                                            <stop
                                                offset="5%"
                                                stopColor="#ff4757"
                                                stopOpacity={0.3}
                                            />
                                            <stop
                                                offset="95%"
                                                stopColor="#ff4757"
                                                stopOpacity={0}
                                            />
                                        </linearGradient>
                                        <linearGradient
                                            id="verGrad"
                                            x1="0" y1="0"
                                            x2="0" y2="1">
                                            <stop
                                                offset="5%"
                                                stopColor="#00ff88"
                                                stopOpacity={0.3}
                                            />
                                            <stop
                                                offset="95%"
                                                stopColor="#00ff88"
                                                stopOpacity={0}
                                            />
                                        </linearGradient>
                                    </defs>
                                    <CartesianGrid
                                        strokeDasharray="3 3"
                                        stroke="rgba(255,255,255,0.05)"
                                    />
                                    <XAxis
                                        dataKey="day"
                                        tick={{
                                            fill: '#64748b',
                                            fontSize: 11
                                        }}
                                        axisLine={false}
                                        tickLine={false}
                                    />
                                    <YAxis
                                        tick={{
                                            fill: '#64748b',
                                            fontSize: 11
                                        }}
                                        axisLine={false}
                                        tickLine={false}
                                    />
                                    <Tooltip
                                        content={
                                            <CustomTooltip/>
                                        }
                                    />
                                    <Area
                                        type="monotone"
                                        dataKey="reports"
                                        name="Reports"
                                        stroke="#ff4757"
                                        strokeWidth={2}
                                        fill="url(#repGrad)"
                                    />
                                    <Area
                                        type="monotone"
                                        dataKey="verified"
                                        name="Verified"
                                        stroke="#00ff88"
                                        strokeWidth={2}
                                        fill="url(#verGrad)"
                                    />
                                </AreaChart>
                            </ResponsiveContainer>
                        </div>

                        {/* Incident Type Pie */}
                        <div className="glass-card p-6">
                            <h3 className="text-sm
                                           font-semibold
                                           text-white mb-1">
                                Incident Types
                            </h3>
                            <p className="text-xs
                                          text-slate-400 mb-4">
                                Distribution by category
                            </p>
                            <ResponsiveContainer
                                width="100%"
                                height={160}>
                                <PieChart>
                                    <Pie
                                        data={incidentTypeData}
                                        cx="50%"
                                        cy="50%"
                                        innerRadius={40}
                                        outerRadius={65}
                                        paddingAngle={3}
                                        dataKey="value">
                                        {incidentTypeData
                                            .map((entry, i) => (
                                            <Cell
                                                key={i}
                                                fill={entry.color}
                                                opacity={0.85}
                                            />
                                        ))}
                                    </Pie>
                                    <Tooltip
                                        content={
                                            <CustomTooltip/>
                                        }
                                    />
                                </PieChart>
                            </ResponsiveContainer>
                            <div className="space-y-1.5
                                            mt-2">
                                {incidentTypeData
                                    .slice(0, 4)
                                    .map((item, i) => (
                                    <div key={i}
                                         className="flex items-center
                                                    justify-between">
                                        <div className="flex items-center
                                                        gap-2">
                                            <div className="w-2 h-2
                                                            rounded-full"
                                                 style={{
                                                     background:
                                                         item.color
                                                 }}/>
                                            <span className="text-xs
                                                             text-slate-400">
                                                {item.name}
                                            </span>
                                        </div>
                                        <span className="text-xs
                                                         font-medium
                                                         text-white">
                                            {item.value}%
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* ============================
                TRENDS TAB
                ============================ */}
            {activeTab === 'trends' && (
                <div className="space-y-6">

                    {/* Monthly Risk Level Trend */}
                    <div className="glass-card p-6">
                        <h3 className="text-sm font-semibold
                                       text-white mb-1">
                            Monthly Risk Level Trends
                        </h3>
                        <p className="text-xs text-slate-400
                                      mb-6">
                            Distribution of risk levels
                            over 6 months
                        </p>
                        <ResponsiveContainer
                            width="100%"
                            height={280}>
                            <BarChart data={monthlyData}
                                      barSize={20}>
                                <CartesianGrid
                                    strokeDasharray="3 3"
                                    stroke="rgba(255,255,255,0.05)"
                                    vertical={false}
                                />
                                <XAxis
                                    dataKey="month"
                                    tick={{
                                        fill: '#64748b',
                                        fontSize: 11
                                    }}
                                    axisLine={false}
                                    tickLine={false}
                                />
                                <YAxis
                                    tick={{
                                        fill: '#64748b',
                                        fontSize: 11
                                    }}
                                    axisLine={false}
                                    tickLine={false}
                                />
                                <Tooltip
                                    content={
                                        <CustomTooltip/>
                                    }
                                />
                                <Legend
                                    wrapperStyle={{
                                        fontSize: '11px',
                                        color: '#64748b'
                                    }}
                                />
                                <Bar
                                    dataKey="high"
                                    name="High Risk"
                                    fill="#ff4757"
                                    radius={[2, 2, 0, 0]}
                                    opacity={0.85}
                                />
                                <Bar
                                    dataKey="medium"
                                    name="Medium Risk"
                                    fill="#ffd32a"
                                    radius={[2, 2, 0, 0]}
                                    opacity={0.85}
                                />
                                <Bar
                                    dataKey="low"
                                    name="Low Risk"
                                    fill="#00ff88"
                                    radius={[2, 2, 0, 0]}
                                    opacity={0.85}
                                />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>

                    {/* Response Time Trend */}
                    <div className="glass-card p-6">
                        <h3 className="text-sm font-semibold
                                       text-white mb-1">
                            Average Response Time (Hours)
                        </h3>
                        <p className="text-xs text-slate-400
                                      mb-6">
                            Time from report to admin review
                        </p>
                        <ResponsiveContainer
                            width="100%"
                            height={200}>
                            <LineChart
                                data={responseTimeData}>
                                <CartesianGrid
                                    strokeDasharray="3 3"
                                    stroke="rgba(255,255,255,0.05)"
                                />
                                <XAxis
                                    dataKey="week"
                                    tick={{
                                        fill: '#64748b',
                                        fontSize: 11
                                    }}
                                    axisLine={false}
                                    tickLine={false}
                                />
                                <YAxis
                                    tick={{
                                        fill: '#64748b',
                                        fontSize: 11
                                    }}
                                    axisLine={false}
                                    tickLine={false}
                                />
                                <Tooltip
                                    content={
                                        <CustomTooltip/>
                                    }
                                />
                                <Line
                                    type="monotone"
                                    dataKey="avgTime"
                                    name="Avg Hours"
                                    stroke="#00d4ff"
                                    strokeWidth={2.5}
                                    dot={{
                                        fill: '#00d4ff',
                                        r: 4
                                    }}
                                    activeDot={{
                                        r: 6,
                                        fill: '#00d4ff'
                                    }}
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            )}

            {/* ============================
                BREAKDOWN TAB
                ============================ */}
            {activeTab === 'breakdown' && (
                <div className="space-y-6">

                    {/* Risk Score Distribution */}
                    <div className="glass-card p-6">
                        <h3 className="text-sm font-semibold
                                       text-white mb-1">
                            Risk Score Distribution
                        </h3>
                        <p className="text-xs text-slate-400
                                      mb-6">
                            How reports are distributed
                            across risk scores
                        </p>
                        <ResponsiveContainer
                            width="100%"
                            height={220}>
                            <BarChart data={riskScoreData}
                                      barSize={40}>
                                <CartesianGrid
                                    strokeDasharray="3 3"
                                    stroke="rgba(255,255,255,0.05)"
                                    vertical={false}
                                />
                                <XAxis
                                    dataKey="range"
                                    tick={{
                                        fill: '#64748b',
                                        fontSize: 11
                                    }}
                                    axisLine={false}
                                    tickLine={false}
                                />
                                <YAxis
                                    tick={{
                                        fill: '#64748b',
                                        fontSize: 11
                                    }}
                                    axisLine={false}
                                    tickLine={false}
                                />
                                <Tooltip
                                    content={
                                        <CustomTooltip/>
                                    }
                                />
                                <Bar
                                    dataKey="count"
                                    name="Reports"
                                    radius={[4, 4, 0, 0]}>
                                    {riskScoreData.map(
                                        (_, i) => (
                                        <Cell
                                            key={i}
                                            fill={
                                                i < 2
                                                ? '#00ff88'
                                                : i < 3
                                                ? '#ffd32a'
                                                : '#ff4757'
                                            }
                                            opacity={0.85}
                                        />
                                    ))}
                                </Bar>
                            </BarChart>
                        </ResponsiveContainer>
                    </div>

                    {/* Threat Radar + Type Table */}
                    <div className="grid grid-cols-1
                                    lg:grid-cols-2 gap-6">

                        {/* Radar Chart */}
                        <div className="glass-card p-6">
                            <h3 className="text-sm
                                           font-semibold
                                           text-white mb-1">
                                Threat Category Radar
                            </h3>
                            <p className="text-xs
                                          text-slate-400 mb-4">
                                Frequency by threat type
                            </p>
                            <ResponsiveContainer
                                width="100%"
                                height={250}>
                                <RadarChart
                                    data={radarData}
                                    cx="50%"
                                    cy="50%"
                                    outerRadius="70%">
                                    <PolarGrid
                                        stroke="rgba(255,255,255,0.1)"
                                    />
                                    <PolarAngleAxis
                                        dataKey="subject"
                                        tick={{
                                            fill: '#64748b',
                                            fontSize: 10
                                        }}
                                    />
                                    <Radar
                                        name="Threats"
                                        dataKey="A"
                                        stroke="#00d4ff"
                                        fill="#00d4ff"
                                        fillOpacity={0.15}
                                        strokeWidth={2}
                                    />
                                    <Tooltip
                                        content={
                                            <CustomTooltip/>
                                        }
                                    />
                                </RadarChart>
                            </ResponsiveContainer>
                        </div>

                        {/* Incident Type Breakdown */}
                        <div className="glass-card p-6">
                            <h3 className="text-sm
                                           font-semibold
                                           text-white mb-1">
                                Incident Type Breakdown
                            </h3>
                            <p className="text-xs
                                          text-slate-400 mb-4">
                                Detailed statistics
                            </p>
                            <div className="space-y-3">
                                {incidentTypeData.map(
                                    (item, i) => (
                                    <div key={i}>
                                        <div className="flex
                                                        items-center
                                                        justify-between
                                                        mb-1.5">
                                            <div className="flex
                                                            items-center
                                                            gap-2">
                                                <div className="w-2.5
                                                                h-2.5
                                                                rounded-full"
                                                     style={{
                                                         background:
                                                             item.color
                                                     }}/>
                                                <span className="text-xs
                                                                 text-slate-300">
                                                    {item.name}
                                                </span>
                                            </div>
                                            <span className="text-xs
                                                             font-bold
                                                             text-white">
                                                {item.value}%
                                            </span>
                                        </div>
                                        <div className="w-full
                                                        h-1.5
                                                        rounded-full
                                                        bg-white/10">
                                            <div
                                                className="h-1.5
                                                           rounded-full
                                                           transition-all
                                                           duration-500"
                                                style={{
                                                    width: `${item.value}%`,
                                                    background:
                                                        item.color,
                                                }}
                                            />
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ThreatAnalysis;
