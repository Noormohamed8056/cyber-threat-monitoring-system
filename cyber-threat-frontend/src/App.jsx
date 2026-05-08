import React, { useEffect, useState } from 'react';
import { XCircle } from 'lucide-react';
import {
    BrowserRouter as Router,
    Routes,
    Route,
    Navigate,
} from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';

// ==========================================
// LAYOUTS
// ==========================================
import AdminLayout   from './layouts/AdminLayout';
import StudentLayout from './layouts/StudentLayout';

// ==========================================
// AUTH PAGES
// ==========================================
import Login       from './pages/auth/Login';
import Register    from './pages/auth/Register';

// ==========================================
// LANDING PAGE
// ==========================================
import LandingPage from './pages/LandingPage';

// ==========================================
// ADMIN PAGES
// ==========================================
import AdminDashboard     from './pages/admin/AdminDashboard';
import IncidentReview     from './pages/admin/IncidentReview';
import AlertManagement    from './pages/admin/AlertManagement';
import AdminCyberAlerts   from './pages/admin/AdminCyberAlerts';
import UserManagement     from './pages/admin/UserManagement';
import ThreatHistory      from './pages/admin/ThreatHistory';
import ThreatAnalysis     from './pages/admin/ThreatAnalysis';
import AnalyticsDashboard from './pages/admin/AnalyticsDashboard';
import SystemLogs         from './pages/admin/SystemLogs';

// ==========================================
// STUDENT PAGES
// ==========================================
import StudentDashboard     from './pages/student/StudentDashboard';
import ReportThreat         from './pages/student/ReportThreat';
import MyReports            from './pages/student/MyReports';
import CyberAlerts          from './pages/student/CyberAlerts';
import StudentThreatHistory from './pages/student/StudentThreatHistory';
import ThreatAwareness      from './pages/student/ThreatAwareness';
import LearningCenter       from './pages/student/LearningCenter';
import Profile              from './pages/student/Profile';
import Settings             from './pages/student/Settings';
import reportService from './services/reportService';

// ==========================================
// APP COMPONENT
// ==========================================
const App = () => {
    const [securityToast, setSecurityToast] = useState(null);

    useEffect(() => {
        const handleExternalLinkClick = async (event) => {
            const target = event.target instanceof Element
                ? event.target.closest('a[href]')
                : null;
            if (!target) return;

            const href = target.getAttribute('href');
            if (!href || href.startsWith('/') || href.startsWith('#')) {
                return;
            }
            if (!/^https?:\/\//i.test(href)) {
                return;
            }
            await reportService.handleLinkClick(event, href);
        };

        document.addEventListener('click', handleExternalLinkClick);
        return () => {
            document.removeEventListener('click', handleExternalLinkClick);
        };
    }, []);

    useEffect(() => {
        const handleToast = (event) => {
            const message = event?.detail?.message || 'Access blocked for security';
            setSecurityToast({
                message,
                visibleAt: Date.now(),
            });
            setTimeout(() => {
                setSecurityToast((prev) => (
                    prev && Date.now() - prev.visibleAt >= 4900 ? null : prev
                ));
            }, 5000);
        };

        window.addEventListener('security-toast', handleToast);
        return () => {
            window.removeEventListener('security-toast', handleToast);
        };
    }, []);

    return (
        <AuthProvider>
            <Router>
                {securityToast && (
                    <div className="fixed top-20 right-6 z-[100]
                                    pointer-events-none">
                        <div className="px-5 py-4 min-w-[380px]
                                        max-w-[440px] shadow-2xl
                                        rounded-xl border"
                             style={{
                                 background: 'rgba(8, 12, 20, 0.96)',
                                 borderColor: 'rgba(239, 68, 68, 0.35)',
                                 boxShadow: '0 20px 45px rgba(0,0,0,0.55)',
                             }}>
                            <div className="flex items-start gap-3">
                                <div className="w-9 h-9 rounded-xl
                                                bg-red-500/15
                                                border border-red-500/35
                                                flex items-center justify-center
                                                flex-shrink-0">
                                    <XCircle className="w-5 h-5 text-red-400"/>
                                </div>
                                <div className="min-w-0">
                                    <p className="text-base font-semibold text-red-300">
                                        Access Blocked
                                    </p>
                                    <p className="text-sm text-slate-300 mt-1 leading-relaxed">
                                        {securityToast.message}
                                    </p>
                                    <p className="text-xs text-slate-500 mt-2">
                                        This message will close automatically in 5 seconds.
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
                <Routes>

                    {/* LANDING PAGE */}
                    <Route
                        path="/"
                        element={<LandingPage/>}
                    />

                    {/* AUTH ROUTES */}
                    <Route
                        path="/login"
                        element={<Login/>}
                    />
                    <Route
                        path="/register"
                        element={<Register/>}
                    />

                    {/* ADMIN ROUTES */}
                    <Route
                        path="/admin"
                        element={<AdminLayout/>}>
                        <Route
                            index
                            element={
                                <Navigate
                                    to="/admin/dashboard"
                                    replace
                                />
                            }
                        />
                        <Route
                            path="dashboard"
                            element={<AdminDashboard/>}
                        />
                        <Route
                            path="incidents"
                            element={<IncidentReview/>}
                        />
                        {/* ✅ Cyber Alerts - View Only */}
                        <Route
                            path="alerts"
                            element={<AdminCyberAlerts/>}
                        />
                        {/* ✅ Alert Management - Full Control */}
                        <Route
                            path="alert-management"
                            element={<AlertManagement/>}
                        />
                        <Route
                            path="users"
                            element={<UserManagement/>}
                        />
                        <Route
                            path="threat-history"
                            element={<ThreatHistory/>}
                        />
                        <Route
                            path="threat-analysis"
                            element={<ThreatAnalysis/>}
                        />
                        <Route
                            path="analytics"
                            element={<AnalyticsDashboard/>}
                        />
                        <Route
                            path="logs"
                            element={<SystemLogs/>}
                        />
                    </Route>

                    {/* STUDENT ROUTES */}
                    <Route
                        path="/student"
                        element={<StudentLayout/>}>
                        <Route
                            index
                            element={
                                <Navigate
                                    to="/student/dashboard"
                                    replace
                                />
                            }
                        />
                        <Route
                            path="dashboard"
                            element={<StudentDashboard/>}
                        />
                        <Route
                            path="report-threat"
                            element={<ReportThreat/>}
                        />
                        <Route
                            path="my-reports"
                            element={<MyReports/>}
                        />
                        <Route
                            path="alerts"
                            element={<CyberAlerts/>}
                        />
                        <Route
                            path="threat-history"
                            element={<StudentThreatHistory/>}
                        />
                        <Route
                            path="awareness"
                            element={<ThreatAwareness/>}
                        />
                        <Route
                            path="learning"
                            element={<LearningCenter/>}
                        />
                        <Route
                            path="profile"
                            element={<Profile/>}
                        />
                        <Route
                            path="settings"
                            element={<Settings/>}
                        />
                    </Route>

                    {/* 404 FALLBACK */}
                    <Route
                        path="*"
                        element={
                            <Navigate to="/" replace/>
                        }
                    />
                </Routes>
            </Router>
        </AuthProvider>
    );
};

export default App;
