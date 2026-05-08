import React, { useState, useEffect } from 'react';
import {
    Shield,
    ShieldAlert,
    Users,
    Bell,
    TrendingUp,
    TrendingDown,
    AlertTriangle,
    CheckCircle,
    Clock,
    Activity,
    Eye,
    FileText,
    RefreshCw,
} from 'lucide-react';
import {
    LineChart,
    Line,
    AreaChart,
    Area,
    BarChart,
    Bar,
    PieChart,
    Pie,
    Cell,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    Legend,
} from 'recharts';
import adminService  from '../../services/adminService';
import reportService from '../../services/reportService';
import alertService  from '../../services/alertService';
import api from '../../services/api';
import {
    formatDateTime,
    getRiskBadgeClass,
    getStatusBadgeClass,
    timeAgo,
    extractUrls,
    normalizeUrl,
} from '../../utils/helpers';

// ==========================================
// DEFAULT CHART DATA (REPLACED BY API DATA)
// ==========================================
const defaultMonthlyData = [
    { month: 'Sep', threats: 0, alerts: 0, resolved: 0 },
    { month: 'Oct', threats: 0, alerts: 0, resolved: 0 },
    { month: 'Nov', threats: 0, alerts: 0, resolved: 0 },
    { month: 'Dec', threats: 0, alerts: 0, resolved: 0 },
    { month: 'Jan', threats: 0, alerts: 0, resolved: 0 },
    { month: 'Feb', threats: 0, alerts: 0, resolved: 0 },
    { month: 'Mar', threats: 0, alerts: 0, resolved: 0 },
];

const defaultRiskData = [
    { name: 'Low',    value: 0, color: '#00ff88' },
    { name: 'Medium', value: 0, color: '#ffd32a' },
    { name: 'High',   value: 0, color: '#ff4757' },
];

const defaultThreatTypeData = [
    { type: 'Phishing',    count: 0 },
    { type: 'Malware',     count: 0 },
    { type: 'Ransomware',  count: 0 },
    { type: 'Data Breach', count: 0 },
    { type: 'Unauth',      count: 0 },
    { type: 'Other',       count: 0 },
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
    trend,
    trendValue,
    subtitle,
}) => (
    <div className={`glass-card p-6 border
                     ${borderColor}
                     hover:scale-105
                     transition-transform duration-200`}>
        <div className="flex items-start
                        justify-between mb-4">
            <div className={`w-12 h-12 rounded-xl
                             flex items-center
                             justify-center
                             ${bgColor}`}>
                <Icon className={`w-6 h-6 ${color}`}/>
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
        <div>
            <p className="text-3xl font-bold text-white mb-1">
                {value}
            </p>
            <p className="text-sm font-medium text-slate-300">
                {title}
            </p>
            {subtitle && (
                <p className="text-xs text-slate-500 mt-1">
                    {subtitle}
                </p>
            )}
        </div>
    </div>
);

// ==========================================
// CUSTOM TOOLTIP FOR CHARTS
// ==========================================
const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
        return (
            <div className="glass-card p-3
                            border border-white/10">
                <p className="text-xs text-slate-400 mb-2">
                    {label}
                </p>
                {payload.map((entry, index) => (
                    <p key={index}
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
// ADMIN DASHBOARD COMPONENT
// ==========================================
const AdminDashboard = () => {

    const [dashboard,   setDashboard]
        = useState(null);
    const [reports,     setReports]
        = useState([]);
    const [alerts,      setAlerts]
        = useState([]);
    const [monthlyData, setMonthlyData]
        = useState(defaultMonthlyData);
    const [riskData,    setRiskData]
        = useState(defaultRiskData);
    const [threatTypeData, setThreatTypeData]
        = useState(defaultThreatTypeData);
    const [isLoading,   setIsLoading]
        = useState(true);
    const [lastUpdated, setLastUpdated]
        = useState(new Date());

    // ==========================================
    // FETCH DASHBOARD DATA
    // ==========================================
    const fetchDashboardData = async () => {
        setIsLoading(true);
        try {
            const [dashRes, reportsRes, alertsRes, graphRes] =
                await Promise.all([
                    adminService.getDashboard(),
                    reportService.getRecentReports(),
                    alertService.getActiveAlerts(),
                    api.get('/admin/dashboard/graph-stats'),
                ]);

            if (dashRes.success) {
                setDashboard(dashRes.dashboard);
            }
            if (reportsRes.success) {
                setReports(reportsRes.reports || []);
            }
            if (alertsRes.success) {
                setAlerts(alertsRes.alerts || []);
            }
            if (graphRes?.data?.success) {
                const graphStats = graphRes.data.graphStats || {};
                setMonthlyData(
                    graphStats.monthlyTrend || defaultMonthlyData
                );
                setRiskData(
                    graphStats.riskDistribution
                    || defaultRiskData
                );
                setThreatTypeData(
                    graphStats.threatTypeDistribution
                    || defaultThreatTypeData
                );
            }
            setLastUpdated(new Date());
        } catch (error) {
            console.error('Dashboard fetch error:', error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchDashboardData();
    }, []);

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

    const stats = dashboard?.reportStats;
    const users = dashboard?.userStats;
    const alertStats = dashboard?.alertStats;

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
                        Admin Dashboard
                    </h1>
                    <p className="text-slate-400 text-sm mt-1">
                        AI-Driven Threat Monitoring Overview
                    </p>
                </div>
                <div className="flex items-center gap-3">
                    <p className="text-xs text-slate-500">
                        Updated {timeAgo(lastUpdated)}
                    </p>
                    <button
                        onClick={fetchDashboardData}
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
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2
                            lg:grid-cols-4 gap-4">
                <StatCard
                    title="Total Reports"
                    value={stats?.totalReports ?? 0}
                    icon={FileText}
                    color="text-cyan-400"
                    bgColor="bg-cyan-500/10"
                    borderColor="border-cyan-500/10"
                    trend="up"
                    trendValue="+12%"
                    subtitle="All time reports"
                />
                <StatCard
                    title="Pending Review"
                    value={stats?.pendingReports ?? 0}
                    icon={Clock}
                    color="text-yellow-400"
                    bgColor="bg-yellow-500/10"
                    borderColor="border-yellow-500/10"
                    trend="up"
                    trendValue="+3"
                    subtitle="Awaiting admin action"
                />
                <StatCard
                    title="High Risk Threats"
                    value={stats?.highRiskReports ?? 0}
                    icon={ShieldAlert}
                    color="text-red-400"
                    bgColor="bg-red-500/10"
                    borderColor="border-red-500/10"
                    trend="down"
                    trendValue="-2"
                    subtitle="Requires immediate action"
                />
                <StatCard
                    title="Active Alerts"
                    value={alertStats?.activeAlerts ?? 0}
                    icon={Bell}
                    color="text-purple-400"
                    bgColor="bg-purple-500/10"
                    borderColor="border-purple-500/10"
                    trend="up"
                    trendValue="+1"
                    subtitle="Published to students"
                />
            </div>

            {/* Second Stats Row */}
            <div className="grid grid-cols-1 md:grid-cols-2
                            lg:grid-cols-4 gap-4">
                <StatCard
                    title="Verified Reports"
                    value={stats?.verifiedReports ?? 0}
                    icon={CheckCircle}
                    color="text-green-400"
                    bgColor="bg-green-500/10"
                    borderColor="border-green-500/10"
                    subtitle="Confirmed threats"
                />
                <StatCard
                    title="Under Review"
                    value={stats?.underReviewReports ?? 0}
                    icon={Eye}
                    color="text-blue-400"
                    bgColor="bg-blue-500/10"
                    borderColor="border-blue-500/10"
                    subtitle="Currently investigating"
                />
                <StatCard
                    title="Total Students"
                    value={users?.totalStudents ?? 0}
                    icon={Users}
                    color="text-cyan-400"
                    bgColor="bg-cyan-500/10"
                    borderColor="border-cyan-500/10"
                    subtitle="Registered students"
                />
                <StatCard
                    title="System Status"
                    value="Online"
                    icon={Activity}
                    color="text-green-400"
                    bgColor="bg-green-500/10"
                    borderColor="border-green-500/10"
                    subtitle="All systems operational"
                />
            </div>

            {/* Charts Row */}
            <div className="grid grid-cols-1
                            lg:grid-cols-3 gap-6">

                {/* Monthly Trend Chart */}
                <div className="lg:col-span-2
                                glass-card p-6">
                    <div className="flex items-center
                                    justify-between mb-6">
                        <div>
                            <h3 className="text-sm font-semibold
                                           text-white">
                                Threat Trends
                            </h3>
                            <p className="text-xs
                                          text-slate-400 mt-1">
                                Monthly incident overview
                            </p>
                        </div>
                        <div className="flex items-center
                                        gap-4 text-xs">
                            <div className="flex items-center
                                            gap-1.5">
                                <div className="w-2 h-2
                                                rounded-full
                                                bg-red-400"/>
                                <span className="text-slate-400">
                                    Threats
                                </span>
                            </div>
                            <div className="flex items-center
                                            gap-1.5">
                                <div className="w-2 h-2
                                                rounded-full
                                                bg-cyan-400"/>
                                <span className="text-slate-400">
                                    Alerts
                                </span>
                            </div>
                            <div className="flex items-center
                                            gap-1.5">
                                <div className="w-2 h-2
                                                rounded-full
                                                bg-green-400"/>
                                <span className="text-slate-400">
                                    Resolved
                                </span>
                            </div>
                        </div>
                    </div>
                    <ResponsiveContainer width="100%"
                                         height={220}>
                        <AreaChart data={monthlyData}>
                            <defs>
                                <linearGradient
                                    id="threatGradient"
                                    x1="0" y1="0"
                                    x2="0" y2="1">
                                    <stop offset="5%"
                                          stopColor="#ff4757"
                                          stopOpacity={0.3}/>
                                    <stop offset="95%"
                                          stopColor="#ff4757"
                                          stopOpacity={0}/>
                                </linearGradient>
                                <linearGradient
                                    id="alertGradient"
                                    x1="0" y1="0"
                                    x2="0" y2="1">
                                    <stop offset="5%"
                                          stopColor="#00d4ff"
                                          stopOpacity={0.3}/>
                                    <stop offset="95%"
                                          stopColor="#00d4ff"
                                          stopOpacity={0}/>
                                </linearGradient>
                                <linearGradient
                                    id="resolvedGradient"
                                    x1="0" y1="0"
                                    x2="0" y2="1">
                                    <stop offset="5%"
                                          stopColor="#00ff88"
                                          stopOpacity={0.3}/>
                                    <stop offset="95%"
                                          stopColor="#00ff88"
                                          stopOpacity={0}/>
                                </linearGradient>
                            </defs>
                            <CartesianGrid
                                strokeDasharray="3 3"
                                stroke="rgba(255,255,255,0.05)"
                            />
                            <XAxis
                                dataKey="month"
                                tick={{ fill: '#64748b',
                                        fontSize: 11 }}
                                axisLine={false}
                                tickLine={false}
                            />
                            <YAxis
                                tick={{ fill: '#64748b',
                                        fontSize: 11 }}
                                axisLine={false}
                                tickLine={false}
                            />
                            <Tooltip
                                content={<CustomTooltip/>}
                            />
                            <Area
                                type="monotone"
                                dataKey="threats"
                                name="Threats"
                                stroke="#ff4757"
                                strokeWidth={2}
                                fill="url(#threatGradient)"
                            />
                            <Area
                                type="monotone"
                                dataKey="alerts"
                                name="Alerts"
                                stroke="#00d4ff"
                                strokeWidth={2}
                                fill="url(#alertGradient)"
                            />
                            <Area
                                type="monotone"
                                dataKey="resolved"
                                name="Resolved"
                                stroke="#00ff88"
                                strokeWidth={2}
                                fill="url(#resolvedGradient)"
                            />
                        </AreaChart>
                    </ResponsiveContainer>
                </div>

                {/* Risk Distribution Pie Chart */}
                <div className="glass-card p-6">
                    <div className="mb-6">
                        <h3 className="text-sm font-semibold
                                       text-white">
                            Risk Distribution
                        </h3>
                        <p className="text-xs
                                      text-slate-400 mt-1">
                            Current risk levels
                        </p>
                    </div>
                    <ResponsiveContainer width="100%"
                                         height={160}>
                        <PieChart>
                            <Pie
                                data={riskData}
                                cx="50%"
                                cy="50%"
                                innerRadius={45}
                                outerRadius={70}
                                paddingAngle={3}
                                dataKey="value">
                                {riskData.map((entry, i) => (
                                    <Cell
                                        key={i}
                                        fill={entry.color}
                                        opacity={0.85}
                                    />
                                ))}
                            </Pie>
                            <Tooltip
                                content={<CustomTooltip/>}
                            />
                        </PieChart>
                    </ResponsiveContainer>
                    <div className="space-y-2 mt-2">
                        {riskData.map((item, i) => (
                            <div key={i}
                                 className="flex items-center
                                            justify-between">
                                <div className="flex items-center
                                                gap-2">
                                    <div className="w-2.5 h-2.5
                                                    rounded-full"
                                         style={{
                                             backgroundColor:
                                                 item.color
                                         }}/>
                                    <span className="text-xs
                                                     text-slate-400">
                                        {item.name}
                                    </span>
                                </div>
                                <span className="text-xs
                                                 font-semibold
                                                 text-white">
                                    {item.value}%
                                </span>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Threat Types Bar Chart */}
            <div className="glass-card p-6">
                <div className="flex items-center
                                justify-between mb-6">
                    <div>
                        <h3 className="text-sm font-semibold
                                       text-white">
                            Threat Types Distribution
                        </h3>
                        <p className="text-xs
                                      text-slate-400 mt-1">
                            Incidents by category
                        </p>
                    </div>
                </div>
                <ResponsiveContainer width="100%"
                                     height={200}>
                    <BarChart data={threatTypeData}
                              barSize={32}>
                        <CartesianGrid
                            strokeDasharray="3 3"
                            stroke="rgba(255,255,255,0.05)"
                            vertical={false}
                        />
                        <XAxis
                            dataKey="type"
                            tick={{ fill: '#64748b',
                                    fontSize: 11 }}
                            axisLine={false}
                            tickLine={false}
                        />
                        <YAxis
                            tick={{ fill: '#64748b',
                                    fontSize: 11 }}
                            axisLine={false}
                            tickLine={false}
                        />
                        <Tooltip
                            content={<CustomTooltip/>}
                        />
                        <Bar dataKey="count"
                             name="Incidents"
                             radius={[4, 4, 0, 0]}>
                            {threatTypeData.map((_, i) => (
                                <Cell
                                    key={i}
                                    fill={`rgba(0,212,255,
                                           ${0.4 + i * 0.1})`}
                                />
                            ))}
                        </Bar>
                    </BarChart>
                </ResponsiveContainer>
            </div>

            {/* Bottom Row */}
            <div className="grid grid-cols-1
                            lg:grid-cols-2 gap-6">

                {/* Recent Reports Table */}
                <div className="glass-card p-6">
                    <div className="flex items-center
                                    justify-between mb-4">
                        <h3 className="text-sm font-semibold
                                       text-white">
                            Recent Reports
                        </h3>
                        <button
                            onClick={() =>
                                window.location.href =
                                '/admin/incidents'
                            }
                            className="text-xs text-cyan-400
                                       hover:text-cyan-300
                                       transition-colors">
                            View all →
                        </button>
                    </div>

                    {reports.length === 0 ? (
                        <div className="text-center py-8">
                            <FileText className="w-8 h-8
                                                  text-slate-600
                                                  mx-auto mb-2"/>
                            <p className="text-slate-500
                                          text-sm">
                                No recent reports
                            </p>
                        </div>
                    ) : (
                        <div className="space-y-3">
                            {reports
                                .slice(0, 5)
                                .map((report) => (
                                <div key={report.id}
                                     className="flex items-center
                                                justify-between
                                                p-3 rounded-xl
                                                bg-white/3
                                                hover:bg-white/5
                                                transition-colors
                                                border
                                                border-white/5">
                                    <div className="flex-1
                                                    min-w-0 mr-3">
                                        <p className="text-sm
                                                      font-medium
                                                      text-white
                                                      truncate">
                                            {report.title}
                                        </p>
                                        <p className="text-xs
                                                      text-slate-500
                                                      mt-0.5">
                                            {timeAgo(
                                                report.createdAt
                                            )}
                                        </p>
                                    </div>
                                    <div className="flex items-center
                                                    gap-2
                                                    flex-shrink-0">
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

                {/* Active Alerts */}
                <div className="glass-card p-6">
                    <div className="flex items-center
                                    justify-between mb-4">
                        <h3 className="text-sm font-semibold
                                       text-white">
                            Active Alerts
                        </h3>
                        <button
                            onClick={() =>
                                window.location.href =
                                '/admin/alerts'
                            }
                            className="text-xs text-cyan-400
                                       hover:text-cyan-300
                                       transition-colors">
                            View all →
                        </button>
                    </div>

                    {alerts.length === 0 ? (
                        <div className="text-center py-8">
                            <Bell className="w-8 h-8
                                             text-slate-600
                                             mx-auto mb-2"/>
                            <p className="text-slate-500
                                          text-sm">
                                No active alerts
                            </p>
                        </div>
                    ) : (
                        <div className="space-y-3">
                            {alerts
                                .slice(0, 5)
                                .map((alert) => (
                                <div key={alert.id}
                                     className="flex items-start
                                                gap-3 p-3
                                                rounded-xl
                                                bg-white/3
                                                hover:bg-white/5
                                                transition-colors
                                                border
                                                border-white/5">
                                    <div className="w-8 h-8
                                                    rounded-lg
                                                    bg-red-500/10
                                                    border
                                                    border-red-500/20
                                                    flex items-center
                                                    justify-center
                                                    flex-shrink-0">
                                        <AlertTriangle
                                            className="w-4 h-4
                                                       text-red-400"
                                        />
                                    </div>
                                    <div className="flex-1
                                                    min-w-0">
                                        <p className="text-sm
                                                      font-medium
                                                      text-white
                                                      truncate">
                                            {alert.title}
                                        </p>
                                        <p className="text-xs
                                                      text-slate-500
                                                      mt-0.5
                                                      line-clamp-1">
                                            {String(alert.message || '')
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
                                        <div className="flex
                                                        items-center
                                                        gap-2 mt-1">
                                            <span className={
                                                getRiskBadgeClass(
                                                    alert.severity
                                                )
                                            }>
                                                {alert.severity}
                                            </span>
                                            <span className="text-xs
                                                             text-slate-600">
                                                {timeAgo(
                                                    alert.createdAt
                                                )}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;
