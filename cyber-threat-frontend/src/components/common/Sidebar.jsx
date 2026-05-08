import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
    Shield,
    LayoutDashboard,
    ShieldAlert,
    Bell,
    BellPlus,
    BarChart3,
    History,
    Users,
    TrendingUp,
    FileText,
    BookOpen,
    GraduationCap,
    User,
    Settings,
    ChevronLeft,
    ChevronRight,
    LogOut,
    Menu,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import {
    ADMIN_NAV_ITEMS,
    STUDENT_NAV_ITEMS
} from '../../utils/constants';

// ==========================================
// ICON MAP
// ==========================================
const iconMap = {
    LayoutDashboard,
    ShieldAlert,
    Bell,
    BellPlus,
    BarChart3,
    History,
    Users,
    TrendingUp,
    FileText,
    BookOpen,
    GraduationCap,
    User,
    Settings,
};

// ==========================================
// SIDEBAR COMPONENT
// ==========================================
const Sidebar = ({ isCollapsed, setIsCollapsed }) => {

    const navigate           = useNavigate();
    const location           = useLocation();
    const { user, logout, isAdmin } = useAuth();

    const navItems = isAdmin()
        ? ADMIN_NAV_ITEMS
        : STUDENT_NAV_ITEMS;

    // ==========================================
    // HANDLE NAVIGATION
    // ==========================================
    const handleNavigate = (path) => {
        navigate(path);
    };

    // ==========================================
    // HANDLE LOGOUT
    // ==========================================
    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    // ==========================================
    // CHECK ACTIVE ROUTE
    // ==========================================
    const isActive = (path) => {
        return location.pathname === path;
    };

    // ==========================================
    // RENDER NAV ITEM
    // ==========================================
    const NavItem = ({ item }) => {
        const IconComponent = iconMap[item.icon];
        const active        = isActive(item.path);

        return (
            <button
                onClick={() => handleNavigate(item.path)}
                className={`w-full flex items-center gap-3
                            px-3 py-2.5 rounded-xl
                            transition-all duration-200
                            group relative
                            ${active
                                ? 'bg-cyan-500/15 text-cyan-400 border-l-2 border-cyan-400'
                                : 'text-slate-400 hover:bg-white/5 hover:text-slate-200'
                            }`}>

                {/* Icon */}
                {IconComponent && (
                    <IconComponent
                        className={`flex-shrink-0
                                    transition-colors
                                    ${active
                                        ? 'text-cyan-400'
                                        : 'text-slate-400 group-hover:text-slate-200'
                                    }
                                    ${isCollapsed
                                        ? 'w-5 h-5'
                                        : 'w-4 h-4'
                                    }`}
                    />
                )}

                {/* Label */}
                {!isCollapsed && (
                    <span className="text-sm font-medium
                                     truncate">
                        {item.label}
                    </span>
                )}

                {/* Active Indicator */}
                {active && !isCollapsed && (
                    <div className="ml-auto w-1.5 h-1.5
                                    rounded-full
                                    bg-cyan-400"/>
                )}

                {/* Tooltip when collapsed */}
                {isCollapsed && (
                    <div className="absolute left-full ml-2
                                    px-2 py-1 rounded-md
                                    bg-slate-800
                                    border border-slate-700
                                    text-xs text-white
                                    whitespace-nowrap
                                    opacity-0
                                    group-hover:opacity-100
                                    transition-opacity
                                    pointer-events-none
                                    z-50">
                        {item.label}
                    </div>
                )}
            </button>
        );
    };

    // ==========================================
    // RENDER
    // ==========================================
    return (
        <div className={`sidebar h-screen flex flex-col
                         transition-all duration-300
                         fixed left-0 top-0 z-40
                         ${isCollapsed ? 'w-16' : 'w-64'}`}>

            {/* Header */}
            <div className="flex items-center justify-between
                            p-4 border-b
                            border-white/5 flex-shrink-0">
                {!isCollapsed && (
                    <div className="flex items-center gap-2">
                        <div className="w-8 h-8 rounded-lg
                                        bg-cyan-500/20
                                        border border-cyan-500/30
                                        flex items-center
                                        justify-center">
                            <Shield className="w-4 h-4
                                               text-cyan-400"/>
                        </div>
                        <div>
                            <h1 className="text-sm font-bold
                                           text-white">
                                CyberShield
                            </h1>
                            <p className="text-xs
                                          text-slate-500">
                                {isAdmin()
                                    ? 'Admin Panel'
                                    : 'Student Portal'
                                }
                            </p>
                        </div>
                    </div>
                )}

                {/* Collapse Button */}
                <button
                    onClick={() =>
                        setIsCollapsed(!isCollapsed)
                    }
                    className="w-8 h-8 rounded-lg
                               flex items-center
                               justify-center
                               text-slate-400
                               hover:text-cyan-400
                               hover:bg-white/5
                               transition-all">
                    {isCollapsed
                        ? <ChevronRight className="w-4 h-4"/>
                        : <ChevronLeft  className="w-4 h-4"/>
                    }
                </button>
            </div>

            {/* User Profile */}
            {!isCollapsed && (
                <div className="p-4 border-b border-white/5">
                    <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-xl
                                        bg-gradient-to-br
                                        from-cyan-500/30
                                        to-blue-500/30
                                        border border-cyan-500/20
                                        flex items-center
                                        justify-center
                                        flex-shrink-0">
                            <span className="text-xs
                                             font-bold
                                             text-cyan-400">
                                {user?.fullName
                                    ?.charAt(0)
                                    ?.toUpperCase()
                                    || 'U'
                                }
                            </span>
                        </div>
                        <div className="overflow-hidden">
                            <p className="text-sm font-medium
                                          text-white truncate">
                                {user?.fullName || 'User'}
                            </p>
                            <p className="text-xs
                                          text-slate-500
                                          truncate">
                                {user?.role || 'STUDENT'}
                            </p>
                        </div>
                    </div>
                </div>
            )}

            {/* Navigation Items */}
            <nav className="flex-1 overflow-y-auto
                            py-3 px-2 space-y-0.5">

                {/* Section Label */}
                {!isCollapsed && (
                    <p className="px-3 py-2 text-xs
                                  font-semibold
                                  text-slate-500
                                  uppercase tracking-wider">
                        {isAdmin()
                            ? 'Administration'
                            : 'Navigation'
                        }
                    </p>
                )}

                {navItems.map((item) => (
                    <NavItem key={item.path} item={item}/>
                ))}
            </nav>

            {/* Footer */}
            <div className="p-2 border-t border-white/5
                            flex-shrink-0">

                {/* Institution Badge */}
                {!isCollapsed && user?.institutionName && (
                    <div className="px-3 py-2 mb-2 rounded-lg
                                    bg-white/3
                                    border border-white/5">
                        <p className="text-xs text-slate-500">
                            Institution
                        </p>
                        <p className="text-xs font-medium
                                      text-slate-300 truncate">
                            {user.institutionName}
                        </p>
                    </div>
                )}

                {/* Logout Button */}
                <button
                    onClick={handleLogout}
                    className="w-full flex items-center gap-3
                               px-3 py-2.5 rounded-xl
                               text-slate-400
                               hover:bg-red-500/10
                               hover:text-red-400
                               transition-all duration-200
                               group">
                    <LogOut className="w-4 h-4 flex-shrink-0
                                       group-hover:text-red-400"/>
                    {!isCollapsed && (
                        <span className="text-sm font-medium">
                            Logout
                        </span>
                    )}
                </button>
            </div>
        </div>
    );
};

export default Sidebar;