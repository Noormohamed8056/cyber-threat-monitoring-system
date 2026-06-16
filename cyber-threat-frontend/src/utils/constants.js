// ==========================================
// API BASE URL
// ==========================================
 export const API_BASE_URL = "https://cyber-threat-monitoring-system.onrender.com/api";

// ==========================================
// LOCAL STORAGE KEYS
// ==========================================
export const TOKEN_KEY = 'cyber_token';
export const USER_KEY  = 'cyber_user';

// ==========================================
// USER ROLES
// ==========================================
export const ROLES = {
    ADMIN:   'ADMIN',
    STUDENT: 'STUDENT',
};

// ==========================================
// INCIDENT TYPES
// ==========================================
export const INCIDENT_TYPES = [
    {
        value: 'PHISHING_EMAIL',
        label: 'Phishing Email'
    },
    {
        value: 'SUSPICIOUS_URL',
        label: 'Suspicious URL'
    },
    {
        value: 'MALWARE',
        label: 'Malware'
    },
    {
        value: 'RANSOMWARE',
        label: 'Ransomware'
    },
    {
        value: 'UNAUTHORIZED_ACCESS',
        label: 'Unauthorized Access'
    },
    {
        value: 'DATA_BREACH',
        label: 'Data Breach'
    },
    {
        value: 'SOCIAL_ENGINEERING',
        label: 'Social Engineering'
    },
    {
        value: 'OTHER',
        label: 'Other'
    },
];

// ==========================================
// RISK LEVELS
// ==========================================
export const RISK_LEVELS = [
    { value: 'LOW',    label: 'Low',    color: '#00ff88' },
    { value: 'MEDIUM', label: 'Medium', color: '#ffd32a' },
    { value: 'HIGH',   label: 'High',   color: '#ff4757' },
    { value: 'CRITICAL', label: 'Critical', color: '#a55eea' },
];

// ==========================================
// REPORT STATUSES
// ==========================================
export const REPORT_STATUSES = [
    {
        value: 'PENDING',
        label: 'Pending',
        color: '#ffd32a'
    },
    {
        value: 'UNDER_REVIEW',
        label: 'Under Review',
        color: '#00d4ff'
    },
    {
        value: 'VERIFIED',
        label: 'Verified',
        color: '#00ff88'
    },
    {
        value: 'DISMISSED',
        label: 'Dismissed',
        color: '#94a3b8'
    },
];

// ==========================================
// ALERT TYPES
// ==========================================
export const ALERT_TYPES = [
    {
        value: 'THREAT_WARNING',
        label: 'Threat Warning'
    },
    {
        value: 'PHISHING_ALERT',
        label: 'Phishing Alert'
    },
    {
        value: 'MALWARE_ALERT',
        label: 'Malware Alert'
    },
    {
        value: 'RANSOMWARE_ALERT',
        label: 'Ransomware Alert'
    },
    {
        value: 'DATA_BREACH_ALERT',
        label: 'Data Breach Alert'
    },
    {
        value: 'AWARENESS_TIP',
        label: 'Awareness Tip'
    },
    {
        value: 'SYSTEM_MAINTENANCE',
        label: 'System Maintenance'
    },
    {
        value: 'GENERAL_NOTICE',
        label: 'General Notice'
    },
];

// ==========================================
// ALERT SEVERITIES
// ==========================================
export const ALERT_SEVERITIES = [
    {
        value: 'LOW',
        label: 'Low',
        color: '#00ff88'
    },
    {
        value: 'MEDIUM',
        label: 'Medium',
        color: '#ffd32a'
    },
    {
        value: 'HIGH',
        label: 'High',
        color: '#ff4757'
    },
    {
        value: 'CRITICAL',
        label: 'Critical',
        color: '#a55eea'
    },
];

// ==========================================
// SIDEBAR NAVIGATION - ADMIN
// ==========================================
export const ADMIN_NAV_ITEMS = [
    {
        label: 'Dashboard',
        path:  '/admin/dashboard',
        icon:  'LayoutDashboard'
    },
    {
        label: 'Incident Review',
        path:  '/admin/incidents',
        icon:  'ShieldAlert'
    },
    {
        label: 'Cyber Alerts',
        path:  '/admin/alerts',
        icon:  'Bell'
    },
    {
        label: 'Alert Management',
        path:  '/admin/alert-management',
        icon:  'BellPlus'
    },
    {
        label: 'Threat Analysis',
        path:  '/admin/threat-analysis',
        icon:  'BarChart3'
    },
    {
        label: 'Threat History',
        path:  '/admin/threat-history',
        icon:  'History'
    },
    {
        label: 'User Management',
        path:  '/admin/users',
        icon:  'Users'
    },
    {
        label: 'Analytics',
        path:  '/admin/analytics',
        icon:  'TrendingUp'
    },
    {
        label: 'System Logs',
        path:  '/admin/logs',
        icon:  'FileText'
    },
];

// ==========================================
// SIDEBAR NAVIGATION - STUDENT
// ==========================================
export const STUDENT_NAV_ITEMS = [
    {
        label: 'Dashboard',
        path:  '/student/dashboard',
        icon:  'LayoutDashboard'
    },
    {
        label: 'Report Threat',
        path:  '/student/report-threat',
        icon:  'ShieldAlert'
    },
    {
        label: 'My Reports',
        path:  '/student/my-reports',
        icon:  'FileText'
    },
    {
        label: 'Cyber Alerts',
        path:  '/student/alerts',
        icon:  'Bell'
    },
    {
        label: 'Threat History',
        path:  '/student/threat-history',
        icon:  'History'
    },
    {
        label: 'Threat Awareness',
        path:  '/student/awareness',
        icon:  'BookOpen'
    },
    {
        label: 'Learning Center',
        path:  '/student/learning',
        icon:  'GraduationCap'
    },
    {
        label: 'Profile',
        path:  '/student/profile',
        icon:  'User'
    },
    {
        label: 'Settings',
        path:  '/student/settings',
        icon:  'Settings'
    },
];
