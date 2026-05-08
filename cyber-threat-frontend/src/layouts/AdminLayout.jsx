import React, { useState, useEffect } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import Sidebar from '../components/common/Sidebar';
import Navbar  from '../components/common/Navbar';
import { useAuth } from '../context/AuthContext';

const AdminLayout = () => {

    const { isAuthenticated,
            isAdmin,
            isLoading } = useAuth();
    const navigate       = useNavigate();
    const [isCollapsed,
           setIsCollapsed] = useState(false);

    // ==========================================
    // PROTECT ADMIN ROUTES
    // ==========================================
    useEffect(() => {
        if (!isLoading) {
            if (!isAuthenticated) {
                navigate('/login');
            } else if (!isAdmin()) {
                navigate('/student/dashboard');
            }
        }
    }, [isAuthenticated, isAdmin, isLoading, navigate]);

    // ==========================================
    // LOADING STATE
    // ==========================================
    if (isLoading) {
        return (
            <div className="min-h-screen
                            bg-dark-500
                            flex items-center
                            justify-center">
                <div className="flex flex-col
                                items-center gap-4">
                    <div className="w-12 h-12
                                    rounded-2xl
                                    bg-cyan-500/20
                                    border
                                    border-cyan-500/30
                                    flex items-center
                                    justify-center
                                    animate-pulse">
                        <div className="w-6 h-6
                                        rounded-lg
                                        bg-cyan-400/50"/>
                    </div>
                    <p className="text-slate-400 text-sm">
                        Loading...
                    </p>
                </div>
            </div>
        );
    }

    // ==========================================
    // RENDER
    // ==========================================
    return (
        <div className="min-h-screen bg-dark-500
                        flex overflow-hidden">

            {/* Sidebar */}
            <Sidebar
                isCollapsed={isCollapsed}
                setIsCollapsed={setIsCollapsed}
            />

            {/* Main Content Area */}
            <div className={`flex-1 flex flex-col
                             min-h-screen
                             transition-all duration-300
                             ${isCollapsed
                                 ? 'ml-16'
                                 : 'ml-64'
                             }`}>

                {/* Navbar */}
                <Navbar
                    isCollapsed={isCollapsed}
                    setIsCollapsed={setIsCollapsed}
                />

                {/* Page Content */}
                <main className="flex-1 overflow-y-auto
                                 pt-16 p-6">
                    <div className="max-w-7xl mx-auto
                                    page-enter">
                        <Outlet/>
                    </div>
                </main>

                {/* Footer */}
                <footer className="py-3 px-6
                                   border-t
                                   border-white/5
                                   flex items-center
                                   justify-between">
                    <p className="text-xs text-slate-600">
                        © 2024 CyberShield —
                        AI-Driven Threat Monitoring
                    </p>
                    <p className="text-xs text-slate-600">
                        Admin Panel v1.0
                    </p>
                </footer>
            </div>
        </div>
    );
};

export default AdminLayout;
