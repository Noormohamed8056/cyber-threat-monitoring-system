import React, { useState, useEffect } from 'react';
import {
    TrendingUp,
    TrendingDown,
    Users,
    ShieldAlert,
    Bell,
    Activity,
    RefreshCw,
    Calendar,
    Download,
    BarChart3,
} from 'lucide-react';
import {
    AreaChart,
    Area,
    BarChart,
    Bar,
    LineChart,
    Line,
    PieChart,
    Pie,
    Cell,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    Legend,
    ComposedChart,
} from 'recharts';
import adminService  from '../../services/adminService';
import reportService from '../../services/reportService';
import alertService  from '../../services/alertService';

// ==========================================
// DEFAULT ANALYTICS DATA (API WILL OVERRIDE)
// ==========================================
const defaultUserGrowthData = [
    { month: 'Sep', students: 0, admins: 0 },
    { month: 'Oct', students: 0, admins: 0 },
    { month: 'Nov', students: 0, admins: 0 },
    { month: 'Dec', students: 0, admins: 0 },
    { month: 'Jan', students: 0, admins: 0 },
    { month: 'Feb', students: 0, admins: 0 },
    { month: 'Mar', students: 0, admins: 0 },
];

const defaultReportActivityData = [
    { week: 'W1', submitted: 0, reviewed: 0, resolved: 0 },
    { week: 'W2', submitted: 0, reviewed: 0, resolved: 0 },
    { week: 'W3', submitted: 0, reviewed: 0, resolved: 0 },
    { week: 'W4', submitted: 0, reviewed: 0, resolved: 0 },
    { week: 'W5', submitted: 0, reviewed: 0, resolved: 0 },
    { week: 'W6', submitted: 0, reviewed: 0, resolved: 0 },
];

const defaultAlertEffectivenessData = [
    { month: 'Oct', published: 0, withdrawn: 0 },
    { month: 'Nov', published: 0, withdrawn: 0 },
    { month: 'Dec', published: 0, withdrawn: 0 },
    { month: 'Jan', published: 0, withdrawn: 0 },
    { month: 'Feb', published: 0, withdrawn: 0 },
    { month: 'Mar', published: 0, withdrawn: 0 },
];

const defaultInstitutionData = [
    { name: 'Institution A', reports: 0, color: '#00d4ff' },
    { name: 'Institution B', reports: 0, color: '#00ff88' },
    { name: 'Institution C', reports: 0, color: '#ffd32a' },
    { name: 'Institution D', reports: 0, color: '#ff4757' },
    { name: 'Others',        reports: 0, color: '#a55eea' },
];

const defaultSystemHealthData = [
    { time: '00:00', uptime: 99, response: 0 },
    { time: '04:00', uptime: 99, response: 0 },
    { time: '08:00', uptime: 99, response: 0 },
    { time: '12:00', uptime: 99, response: 0 },
    { time: '16:00', uptime: 99, response: 0 },
    { time: '20:00', uptime: 99, response: 0 },
    { time: '23:59', uptime: 99, response: 0 },
];

const defaultTopThreatsData = [
    {
        rank:    1,
        type:    'Threat 1',
        count:   0,
        trend:   'up',
        change:  '+0%',
        color:   '#ff4757',
    },
    {
        rank:    2,
        type:    'Threat 2',
        count:   0,
        trend:   'down',
        change:  '+0%',
        color:   '#ffd32a',
    },
    {
        rank:    3,
        type:    'Threat 3',
        count:   0,
        trend:   'up',
        change:  '+0%',
        color:   '#a55eea',
    },
    {
        rank:    4,
        type:    'Threat 4',
        count:   0,
        trend:   'up',
        change:  '+0%',
        color:   '#00d4ff',
    },
    {
        rank:    5,
        type:    'Threat 5',
        count:   0,
        trend:   'down',
        change:  '+0%',
        color:   '#00ff88',
    },
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
// KPI CARD COMPONENT
// ==========================================
const KpiCard = ({
    title,
    value,
    change,
    changeType,
    icon: Icon,
    color,
    bg,
    border,
    suffix = '',
}) => (
    <div className={`glass-card p-5 border ${border}
                     hover:scale-[1.02]
                     transition-transform duration-200`}>
        <div className="flex items-start
                        justify-between mb-4">
            <div className={`w-10 h-10 rounded-xl
                             flex items-center
                             justify-center ${bg}`}>
                <Icon className={`w-5 h-5 ${color}`}/>
            </div>
            <div className={`flex items-center gap-1
                             text-xs font-medium px-2
                             py-1 rounded-full
                             ${changeType === 'positive'
                                 ? 'bg-green-500/10 text-green-400'
                                 : 'bg-red-500/10 text-red-400'
                             }`}>
                {changeType === 'positive'
                    ? <TrendingUp   className="w-3 h-3"/>
                    : <TrendingDown className="w-3 h-3"/>
                }
                {change}
            </div>
        </div>
        <p className="text-3xl font-bold text-white mb-1">
            {value}{suffix}
        </p>
        <p className="text-xs text-slate-400">
            {title}
        </p>
    </div>
);

// ==========================================
// ANALYTICS DASHBOARD PAGE
// ==========================================
const AnalyticsDashboard = () => {

    const [dashboard,  setDashboard]
        = useState(null);
    const [userGrowthData, setUserGrowthData]
        = useState(defaultUserGrowthData);
    const [reportActivityData, setReportActivityData]
        = useState(defaultReportActivityData);
    const [alertEffectivenessData, setAlertEffectivenessData]
        = useState(defaultAlertEffectivenessData);
    const [institutionData, setInstitutionData]
        = useState(defaultInstitutionData);
    const [systemHealthData, setSystemHealthData]
        = useState(defaultSystemHealthData);
    const [topThreatsData, setTopThreatsData]
        = useState(defaultTopThreatsData);
    const [isLoading,  setIsLoading]
        = useState(true);
    const [dateRange,  setDateRange]
        = useState('30d');
    const [activeTab,  setActiveTab]
        = useState('overview');

    const buildAnalyticsFromData = (
        users = [],
        reports = [],
        alerts = []
    ) => {
        const parsedUsers = users.map(u => ({
            ...u,
            createdAtDate: u.createdAt ? new Date(u.createdAt) : null,
        }));
        const parsedReports = reports.map(r => ({
            ...r,
            createdAtDate: r.createdAt ? new Date(r.createdAt) : null,
            updatedAtDate: r.updatedAt ? new Date(r.updatedAt) : null,
            verifiedAtDate: r.verifiedAt ? new Date(r.verifiedAt) : null,
        }));
        const parsedAlerts = alerts.map(a => ({
            ...a,
            createdAtDate: a.createdAt ? new Date(a.createdAt) : null,
            updatedAtDate: a.updatedAt ? new Date(a.updatedAt) : null,
        }));

        const now = new Date();

        const userGrowthData = [];
        for (let i = 6; i >= 0; i--) {
            const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
            const endOfMonth = new Date(
                d.getFullYear(),
                d.getMonth() + 1,
                0,
                23, 59, 59
            );
            const students = parsedUsers.filter(u =>
                u.role === 'STUDENT'
                && u.createdAtDate
                && u.createdAtDate <= endOfMonth
            ).length;
            const admins = parsedUsers.filter(u =>
                u.role === 'ADMIN'
                && u.createdAtDate
                && u.createdAtDate <= endOfMonth
            ).length;
            userGrowthData.push({
                month: d.toLocaleString('en-US', { month: 'short' }),
                students,
                admins,
            });
        }

        const weekStart = new Date(now);
        weekStart.setDate(now.getDate() - now.getDay());
        const reportActivityData = [];
        for (let i = 5; i >= 0; i--) {
            const start = new Date(weekStart);
            start.setDate(weekStart.getDate() - (i * 7));
            const end = new Date(start);
            end.setDate(start.getDate() + 6);

            const submitted = parsedReports.filter(r =>
                r.createdAtDate
                && r.createdAtDate >= start
                && r.createdAtDate <= end
            ).length;

            const reviewed = parsedReports.filter(r => {
                const t = r.updatedAtDate || r.verifiedAtDate;
                return r.status !== 'PENDING'
                    && t
                    && t >= start
                    && t <= end;
            }).length;

            const resolved = parsedReports.filter(r => {
                const t = r.verifiedAtDate || r.updatedAtDate;
                return (r.status === 'VERIFIED' || r.status === 'DISMISSED')
                    && t
                    && t >= start
                    && t <= end;
            }).length;

            reportActivityData.push({
                week: `W${6 - i}`,
                submitted,
                reviewed,
                resolved,
            });
        }

        const alertEffectivenessData = [];
        for (let i = 5; i >= 0; i--) {
            const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
            const y = d.getFullYear();
            const m = d.getMonth();
            const published = parsedAlerts.filter(a =>
                a.createdAtDate
                && a.createdAtDate.getFullYear() === y
                && a.createdAtDate.getMonth() === m
            ).length;
            const withdrawn = parsedAlerts.filter(a => {
                const t = a.updatedAtDate || a.createdAtDate;
                return a.status === 'WITHDRAWN'
                    && t
                    && t.getFullYear() === y
                    && t.getMonth() === m;
            }).length;
            alertEffectivenessData.push({
                month: d.toLocaleString('en-US', { month: 'short' }),
                published,
                withdrawn,
            });
        }

        const institutionCounts = {};
        parsedReports.forEach(r => {
            const name = r.reportedBy?.institutionName || 'Others';
            institutionCounts[name] = (institutionCounts[name] || 0) + 1;
        });
        const institutionSorted = Object.entries(institutionCounts)
            .sort((a, b) => b[1] - a[1]);
        const colors = ['#00d4ff', '#00ff88', '#ffd32a', '#ff4757', '#a55eea'];
        const institutionData = [];
        let others = 0;
        institutionSorted.forEach(([name, reportsCount], idx) => {
            if (idx < 4) {
                institutionData.push({
                    name,
                    reports: reportsCount,
                    color: colors[idx],
                });
            } else {
                others += reportsCount;
            }
        });
        if (others > 0 || institutionData.length === 0) {
            institutionData.push({
                name: 'Others',
                reports: others,
                color: colors[4],
            });
        }

        const timeBuckets = ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '23:59'];
        const bucketStart = [0, 4, 8, 12, 16, 20, 23];
        const todayStr = now.toDateString();
        const systemHealthData = timeBuckets.map((label, i) => {
            const s = bucketStart[i];
            const e = i < bucketStart.length - 1 ? bucketStart[i + 1] : 24;

            const todayReports = parsedReports.filter(r =>
                r.createdAtDate
                && r.createdAtDate.toDateString() === todayStr
                && r.createdAtDate.getHours() >= s
                && r.createdAtDate.getHours() < e
            ).length;
            const todayAlerts = parsedAlerts.filter(a =>
                a.createdAtDate
                && a.createdAtDate.toDateString() === todayStr
                && a.createdAtDate.getHours() >= s
                && a.createdAtDate.getHours() < e
            ).length;
            const durations = parsedReports
                .filter(r => r.createdAtDate && r.verifiedAtDate)
                .filter(r =>
                    r.verifiedAtDate.toDateString() === todayStr
                    && r.verifiedAtDate.getHours() >= s
                    && r.verifiedAtDate.getHours() < e
                )
                .map(r => Math.max(0, (r.verifiedAtDate - r.createdAtDate) / (1000 * 60)));

            const response = durations.length
                ? Math.round(durations.reduce((a, b) => a + b, 0) / durations.length)
                : 0;
            const uptime = (todayReports + todayAlerts) > 0 ? 100 : 99;
            return { time: label, uptime, response };
        });

        const typeMap = {
            PHISHING_EMAIL: 'Phishing Email',
            SUSPICIOUS_URL: 'Suspicious URL',
            MALWARE: 'Malware',
            RANSOMWARE: 'Ransomware',
            DATA_BREACH: 'Data Breach',
            UNAUTHORIZED_ACCESS: 'Unauthorized Access',
            SOCIAL_ENGINEERING: 'Social Engineering',
            OTHER: 'Other',
        };
        const typeCounts = {};
        parsedReports.forEach(r => {
            const t = typeMap[r.incidentType] || 'Other';
            typeCounts[t] = (typeCounts[t] || 0) + 1;
        });
        const colorsByType = {
            'Phishing Email': '#ff4757',
            'Suspicious URL': '#ffd32a',
            Malware: '#a55eea',
            Ransomware: '#00d4ff',
            'Data Breach': '#00ff88',
            'Unauthorized Access': '#ff6b35',
            'Social Engineering': '#f368e0',
            Other: '#94a3b8',
        };
        const sortedTypes = Object.entries(typeCounts)
            .sort((a, b) => b[1] - a[1])
            .slice(0, 5);

        const topThreatsData = sortedTypes.map(([type, count], i) => ({
            rank: i + 1,
            type,
            count,
            trend: 'up',
            change: '+0%',
            color: colorsByType[type] || '#94a3b8',
        }));

        return {
            userGrowthData,
            reportActivityData,
            alertEffectivenessData,
            institutionData,
            systemHealthData,
            topThreatsData: topThreatsData.length
                ? topThreatsData
                : defaultTopThreatsData,
        };
    };

    // ==========================================
    // FETCH DATA
    // ==========================================
    const fetchData = async () => {
        setIsLoading(true);
        try {
            const [dashboardRes, usersRes, reportsRes, alertsRes] = await Promise.all([
                adminService.getDashboard(),
                adminService.getAllUsers(),
                reportService.getAllReports(),
                alertService.getAllAlerts(),
            ]);
            if (dashboardRes.success) {
                setDashboard(dashboardRes.dashboard);
            }
            if (usersRes?.success && reportsRes?.success && alertsRes?.success) {
                const data = buildAnalyticsFromData(
                    usersRes.users || [],
                    reportsRes.reports || [],
                    alertsRes.alerts || []
                );
                setUserGrowthData(data.userGrowthData);
                setReportActivityData(data.reportActivityData);
                setAlertEffectivenessData(data.alertEffectivenessData);
                setInstitutionData(data.institutionData);
                setSystemHealthData(data.systemHealthData);
                setTopThreatsData(data.topThreatsData);
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

    const maxInstitutionReports = Math.max(
        ...institutionData.map(inst => inst.reports || 0),
        1
    );
    const maxThreatCount = Math.max(
        ...topThreatsData.map(threat => threat.count || 0),
        1
    );

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
                        Analytics Dashboard
                    </h1>
                    <p className="text-slate-400
                                  text-sm mt-1">
                        Comprehensive system performance
                        and security metrics
                    </p>
                </div>
                <div className="flex items-center gap-3">

                    {/* Date Range Selector */}
                    <select
                        value={dateRange}
                        onChange={(e) =>
                            setDateRange(e.target.value)
                        }
                        className="cyber-input h-9
                                   text-sm w-32
                                   cursor-pointer">
                        <option value="7d">
                            Last 7 days
                        </option>
                        <option value="30d">
                            Last 30 days
                        </option>
                        <option value="90d">
                            Last 90 days
                        </option>
                        <option value="1y">
                            Last year
                        </option>
                    </select>

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

                    <button
                        onClick={() =>
                            alert(
                                'Export feature coming soon!'
                            )
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

            {/* Tabs */}
            <div className="flex items-center gap-1
                            p-1 rounded-xl bg-white/3
                            border border-white/5 w-fit">
                {[
                    { key: 'overview',     label: '📊 Overview'     },
                    { key: 'users',        label: '👥 Users'        },
                    { key: 'threats',      label: '🛡️ Threats'     },
                    { key: 'system',       label: '⚙️ System'      },
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

                    {/* KPI Cards */}
                    <div className="grid grid-cols-2
                                    lg:grid-cols-4 gap-4">
                        <KpiCard
                            title="Total Users"
                            value={
                                dashboard?.userStats
                                    ?.totalUsers ?? 0
                            }
                            change="+18%"
                            changeType="positive"
                            icon={Users}
                            color="text-cyan-400"
                            bg="bg-cyan-500/10"
                            border="border-cyan-500/10"
                        />
                        <KpiCard
                            title="Total Reports"
                            value={
                                dashboard?.reportStats
                                    ?.totalReports ?? 0
                            }
                            change="+12%"
                            changeType="positive"
                            icon={ShieldAlert}
                            color="text-red-400"
                            bg="bg-red-500/10"
                            border="border-red-500/10"
                        />
                        <KpiCard
                            title="Active Alerts"
                            value={
                                dashboard?.alertStats
                                    ?.activeAlerts ?? 0
                            }
                            change="-5%"
                            changeType="negative"
                            icon={Bell}
                            color="text-yellow-400"
                            bg="bg-yellow-500/10"
                            border="border-yellow-500/10"
                        />
                        <KpiCard
                            title="System Uptime"
                            value="99.9"
                            suffix="%"
                            change="+0.1%"
                            changeType="positive"
                            icon={Activity}
                            color="text-green-400"
                            bg="bg-green-500/10"
                            border="border-green-500/10"
                        />
                    </div>

                    {/* Charts Row */}
                    <div className="grid grid-cols-1
                                    lg:grid-cols-2 gap-6">

                        {/* Report Activity */}
                        <div className="glass-card p-6">
                            <h3 className="text-sm
                                           font-semibold
                                           text-white mb-1">
                                Report Activity
                            </h3>
                            <p className="text-xs
                                          text-slate-400 mb-6">
                                Weekly submitted vs resolved
                            </p>
                            <ResponsiveContainer
                                width="100%"
                                height={220}>
                                <ComposedChart
                                    data={reportActivityData}>
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
                                    <Bar
                                        dataKey="submitted"
                                        name="Submitted"
                                        fill="#ff4757"
                                        opacity={0.7}
                                        radius={[2, 2, 0, 0]}
                                        barSize={16}
                                    />
                                    <Bar
                                        dataKey="resolved"
                                        name="Resolved"
                                        fill="#00ff88"
                                        opacity={0.7}
                                        radius={[2, 2, 0, 0]}
                                        barSize={16}
                                    />
                                    <Line
                                        type="monotone"
                                        dataKey="reviewed"
                                        name="Reviewed"
                                        stroke="#00d4ff"
                                        strokeWidth={2}
                                        dot={false}
                                    />
                                </ComposedChart>
                            </ResponsiveContainer>
                        </div>

                        {/* Alert Effectiveness */}
                        <div className="glass-card p-6">
                            <h3 className="text-sm
                                           font-semibold
                                           text-white mb-1">
                                Alert Effectiveness
                            </h3>
                            <p className="text-xs
                                          text-slate-400 mb-6">
                                Published vs withdrawn alerts
                            </p>
                            <ResponsiveContainer
                                width="100%"
                                height={220}>
                                <AreaChart
                                    data={
                                        alertEffectivenessData
                                    }>
                                    <defs>
                                        <linearGradient
                                            id="pubGrad"
                                            x1="0" y1="0"
                                            x2="0" y2="1">
                                            <stop
                                                offset="5%"
                                                stopColor="#00d4ff"
                                                stopOpacity={0.3}
                                            />
                                            <stop
                                                offset="95%"
                                                stopColor="#00d4ff"
                                                stopOpacity={0}
                                            />
                                        </linearGradient>
                                    </defs>
                                    <CartesianGrid
                                        strokeDasharray="3 3"
                                        stroke="rgba(255,255,255,0.05)"
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
                                    <Area
                                        type="monotone"
                                        dataKey="published"
                                        name="Published"
                                        stroke="#00d4ff"
                                        strokeWidth={2}
                                        fill="url(#pubGrad)"
                                    />
                                    <Line
                                        type="monotone"
                                        dataKey="withdrawn"
                                        name="Withdrawn"
                                        stroke="#ff4757"
                                        strokeWidth={2}
                                        dot={false}
                                    />
                                </AreaChart>
                            </ResponsiveContainer>
                        </div>
                    </div>
                </div>
            )}

            {/* ============================
                USERS TAB
                ============================ */}
            {activeTab === 'users' && (
                <div className="space-y-6">

                    {/* User Growth Chart */}
                    <div className="glass-card p-6">
                        <h3 className="text-sm font-semibold
                                       text-white mb-1">
                            User Growth Over Time
                        </h3>
                        <p className="text-xs text-slate-400
                                      mb-6">
                            Cumulative student and admin
                            registrations
                        </p>
                        <ResponsiveContainer
                            width="100%"
                            height={280}>
                            <AreaChart data={userGrowthData}>
                                <defs>
                                    <linearGradient
                                        id="stuGrad"
                                        x1="0" y1="0"
                                        x2="0" y2="1">
                                        <stop
                                            offset="5%"
                                            stopColor="#00d4ff"
                                            stopOpacity={0.3}
                                        />
                                        <stop
                                            offset="95%"
                                            stopColor="#00d4ff"
                                            stopOpacity={0}
                                        />
                                    </linearGradient>
                                </defs>
                                <CartesianGrid
                                    strokeDasharray="3 3"
                                    stroke="rgba(255,255,255,0.05)"
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
                                <Area
                                    type="monotone"
                                    dataKey="students"
                                    name="Students"
                                    stroke="#00d4ff"
                                    strokeWidth={2}
                                    fill="url(#stuGrad)"
                                />
                                <Line
                                    type="monotone"
                                    dataKey="admins"
                                    name="Admins"
                                    stroke="#a55eea"
                                    strokeWidth={2}
                                    dot={false}
                                />
                            </AreaChart>
                        </ResponsiveContainer>
                    </div>

                    {/* Institution Distribution */}
                    <div className="grid grid-cols-1
                                    lg:grid-cols-2 gap-6">
                        <div className="glass-card p-6">
                            <h3 className="text-sm
                                           font-semibold
                                           text-white mb-1">
                                Reports by Institution
                            </h3>
                            <p className="text-xs
                                          text-slate-400 mb-4">
                                Top reporting institutions
                            </p>
                            <ResponsiveContainer
                                width="100%"
                                height={200}>
                                <PieChart>
                                    <Pie
                                        data={institutionData}
                                        cx="50%"
                                        cy="50%"
                                        outerRadius={75}
                                        dataKey="reports">
                                        {institutionData
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
                        </div>

                        <div className="glass-card p-6">
                            <h3 className="text-sm
                                           font-semibold
                                           text-white mb-4">
                                Institution Breakdown
                            </h3>
                            <div className="space-y-3">
                                {institutionData.map(
                                    (inst, i) => (
                                    <div key={i}>
                                        <div className="flex
                                                        items-center
                                                        justify-between
                                                        mb-1">
                                            <div className="flex
                                                            items-center
                                                            gap-2">
                                                <div className="w-2.5
                                                                h-2.5
                                                                rounded-full"
                                                     style={{
                                                         background:
                                                             inst.color
                                                     }}/>
                                                <span className="text-xs
                                                                 text-slate-300">
                                                    {inst.name}
                                                </span>
                                            </div>
                                            <span className="text-xs
                                                             font-bold
                                                             text-white">
                                                {inst.reports}
                                                {' '}reports
                                            </span>
                                        </div>
                                        <div className="w-full
                                                        h-1.5
                                                        rounded-full
                                                        bg-white/10">
                                            <div
                                                className="h-1.5
                                                           rounded-full"
                                                style={{
                                                    width: `${(inst.reports / maxInstitutionReports) * 100}%`,
                                                    background:
                                                        inst.color,
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

            {/* ============================
                THREATS TAB
                ============================ */}
            {activeTab === 'threats' && (
                <div className="space-y-6">

                    {/* Top Threats Table */}
                    <div className="glass-card p-6">
                        <h3 className="text-sm font-semibold
                                       text-white mb-1">
                            Top Threat Types
                        </h3>
                        <p className="text-xs text-slate-400
                                      mb-5">
                            Most reported threat categories
                        </p>
                        <div className="space-y-3">
                            {topThreatsData.map(
                                (threat, i) => (
                                <div key={i}
                                     className="flex items-center
                                                gap-4 p-4
                                                rounded-xl
                                                bg-white/3
                                                border border-white/5
                                                hover:border-white/10
                                                transition-all">

                                    {/* Rank */}
                                    <div className="w-8 h-8
                                                    rounded-lg
                                                    bg-white/5
                                                    flex items-center
                                                    justify-center
                                                    flex-shrink-0">
                                        <span className="text-xs
                                                         font-bold
                                                         text-slate-400">
                                            #{threat.rank}
                                        </span>
                                    </div>

                                    {/* Type */}
                                    <div className="flex-1">
                                        <p className="text-sm
                                                      font-medium
                                                      text-white mb-1">
                                            {threat.type}
                                        </p>
                                        <div className="w-full
                                                        h-1.5
                                                        rounded-full
                                                        bg-white/10">
                                            <div
                                                className="h-1.5
                                                           rounded-full
                                                           transition-all"
                                                style={{
                                                    width: `${(threat.count / maxThreatCount) * 100}%`,
                                                    background:
                                                        threat.color,
                                                }}
                                            />
                                        </div>
                                    </div>

                                    {/* Count */}
                                    <div className="text-right
                                                    flex-shrink-0">
                                        <p className="text-sm
                                                      font-bold
                                                      text-white">
                                            {threat.count}
                                        </p>
                                        <div className={`flex items-center
                                                         gap-1 text-xs
                                                         justify-end
                                                         ${threat.trend === 'up'
                                                             ? 'text-red-400'
                                                             : 'text-green-400'
                                                         }`}>
                                            {threat.trend === 'up'
                                                ? <TrendingUp   className="w-3 h-3"/>
                                                : <TrendingDown className="w-3 h-3"/>
                                            }
                                            {threat.change}
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            )}

            {/* ============================
                SYSTEM TAB
                ============================ */}
            {activeTab === 'system' && (
                <div className="space-y-6">

                    {/* System Health Cards */}
                    <div className="grid grid-cols-2
                                    lg:grid-cols-4 gap-4">
                        {[
                            {
                                label: 'API Uptime',
                                value: '99.9%',
                                color: 'text-green-400',
                                bg:    'bg-green-500/10',
                                border:'border-green-500/10',
                            },
                            {
                                label: 'Avg Response',
                                value: '128ms',
                                color: 'text-cyan-400',
                                bg:    'bg-cyan-500/10',
                                border:'border-cyan-500/10',
                            },
                            {
                                label: 'DB Queries',
                                value: '2.4k',
                                color: 'text-blue-400',
                                bg:    'bg-blue-500/10',
                                border:'border-blue-500/10',
                            },
                            {
                                label: 'Active Users',
                                value: '48',
                                color: 'text-purple-400',
                                bg:    'bg-purple-500/10',
                                border:'border-purple-500/10',
                            },
                        ].map((stat, i) => (
                            <div key={i}
                                 className={`glass-card p-5
                                             border
                                             ${stat.border}
                                             text-center`}>
                                <p className={`text-2xl
                                               font-bold
                                               ${stat.color}`}>
                                    {stat.value}
                                </p>
                                <p className="text-xs
                                              text-slate-400
                                              mt-1">
                                    {stat.label}
                                </p>
                            </div>
                        ))}
                    </div>

                    {/* System Health Chart */}
                    <div className="glass-card p-6">
                        <h3 className="text-sm font-semibold
                                       text-white mb-1">
                            System Health (Today)
                        </h3>
                        <p className="text-xs text-slate-400
                                      mb-6">
                            Uptime percentage and response
                            time throughout the day
                        </p>
                        <ResponsiveContainer
                            width="100%"
                            height={250}>
                            <ComposedChart
                                data={systemHealthData}>
                                <CartesianGrid
                                    strokeDasharray="3 3"
                                    stroke="rgba(255,255,255,0.05)"
                                />
                                <XAxis
                                    dataKey="time"
                                    tick={{
                                        fill: '#64748b',
                                        fontSize: 11
                                    }}
                                    axisLine={false}
                                    tickLine={false}
                                />
                                <YAxis
                                    yAxisId="left"
                                    tick={{
                                        fill: '#64748b',
                                        fontSize: 11
                                    }}
                                    axisLine={false}
                                    tickLine={false}
                                    domain={[95, 100]}
                                />
                                <YAxis
                                    yAxisId="right"
                                    orientation="right"
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
                                <Area
                                    yAxisId="left"
                                    type="monotone"
                                    dataKey="uptime"
                                    name="Uptime %"
                                    stroke="#00ff88"
                                    fill="#00ff8820"
                                    strokeWidth={2}
                                />
                                <Line
                                    yAxisId="right"
                                    type="monotone"
                                    dataKey="response"
                                    name="Response (ms)"
                                    stroke="#00d4ff"
                                    strokeWidth={2}
                                    dot={false}
                                />
                            </ComposedChart>
                        </ResponsiveContainer>
                    </div>

                    {/* System Status Items */}
                    <div className="glass-card p-6">
                        <h3 className="text-sm font-semibold
                                       text-white mb-5">
                            Service Status
                        </h3>
                        <div className="space-y-3">
                            {[
                                {
                                    service: 'Spring Boot API',
                                    status:  'Operational',
                                    uptime:  '99.9%',
                                    color:   'text-green-400',
                                    dot:     'bg-green-400',
                                },
                                {
                                    service: 'MySQL Database',
                                    status:  'Operational',
                                    uptime:  '99.8%',
                                    color:   'text-green-400',
                                    dot:     'bg-green-400',
                                },
                                {
                                    service: 'JWT Auth Service',
                                    status:  'Operational',
                                    uptime:  '100%',
                                    color:   'text-green-400',
                                    dot:     'bg-green-400',
                                },
                                {
                                    service: 'AI Risk Engine',
                                    status:  'Operational',
                                    uptime:  '99.9%',
                                    color:   'text-green-400',
                                    dot:     'bg-green-400',
                                },
                                {
                                    service: 'Alert Service',
                                    status:  'Operational',
                                    uptime:  '100%',
                                    color:   'text-green-400',
                                    dot:     'bg-green-400',
                                },
                            ].map((item, i) => (
                                <div key={i}
                                     className="flex items-center
                                                justify-between p-3
                                                rounded-xl
                                                bg-white/3
                                                border border-white/5">
                                    <div className="flex items-center
                                                    gap-3">
                                        <div className={`w-2 h-2
                                                         rounded-full
                                                         ${item.dot}
                                                         animate-pulse`}/>
                                        <span className="text-sm
                                                         text-white">
                                            {item.service}
                                        </span>
                                    </div>
                                    <div className="flex items-center
                                                    gap-4">
                                        <span className={`text-xs
                                                          font-medium
                                                          ${item.color}`}>
                                            {item.status}
                                        </span>
                                        <span className="text-xs
                                                         text-slate-500">
                                            {item.uptime}
                                        </span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AnalyticsDashboard;
