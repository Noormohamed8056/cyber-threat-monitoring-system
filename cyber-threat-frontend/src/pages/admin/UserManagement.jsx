import React, { useState, useEffect } from 'react';
import {
    Users,
    Search,
    UserCheck,
    UserX,
    Shield,
    GraduationCap,
    RefreshCw,
    Building,
    Mail,
    Calendar,
    MoreVertical,
    Eye,
    X,
} from 'lucide-react';
import adminService from '../../services/adminService';
import {
    formatDateTime,
    getInitials,
    timeAgo,
} from '../../utils/helpers';

// ==========================================
// USER DETAIL MODAL
// ==========================================
const UserDetailModal = ({
    user,
    onClose,
    onDeactivate,
    onReactivate,
}) => {
    const [isLoading, setIsLoading] = useState(false);

    const handleAction = async (action) => {
        setIsLoading(true);
        try {
            if (action === 'deactivate') {
                await onDeactivate(user.id);
            } else {
                await onReactivate(user.id);
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
            <div className="glass-card w-full max-w-md
                            border border-white/10">

                {/* Header */}
                <div className="flex items-center
                                justify-between p-6
                                border-b border-white/5">
                    <h2 className="text-lg font-bold
                                   text-white">
                        User Details
                    </h2>
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
                <div className="p-6 space-y-5">

                    {/* Avatar and Name */}
                    <div className="flex items-center
                                    gap-4">
                        <div className="w-16 h-16
                                        rounded-2xl
                                        bg-gradient-to-br
                                        from-cyan-500/30
                                        to-blue-500/30
                                        border
                                        border-cyan-500/20
                                        flex items-center
                                        justify-center">
                            <span className="text-xl
                                             font-bold
                                             text-cyan-400">
                                {getInitials(user.fullName)}
                            </span>
                        </div>
                        <div>
                            <h3 className="text-lg
                                           font-bold
                                           text-white">
                                {user.fullName}
                            </h3>
                            <p className="text-sm
                                          text-slate-400">
                                {user.email}
                            </p>
                            <div className="flex items-center
                                            gap-2 mt-2">
                                <span className={`inline-flex
                                                  items-center
                                                  gap-1 px-2
                                                  py-0.5
                                                  rounded-full
                                                  text-xs
                                                  font-medium
                                                  ${user.role === 'ADMIN'
                                                      ? 'bg-purple-500/10 text-purple-400 border border-purple-500/20'
                                                      : 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/20'
                                                  }`}>
                                    {user.role === 'ADMIN'
                                        ? <Shield className="w-3 h-3"/>
                                        : <GraduationCap className="w-3 h-3"/>
                                    }
                                    {user.role}
                                </span>
                                <span className={`px-2 py-0.5
                                                  rounded-full
                                                  text-xs
                                                  font-medium
                                                  ${user.isActive
                                                      ? 'bg-green-500/10 text-green-400 border border-green-500/20'
                                                      : 'bg-red-500/10 text-red-400 border border-red-500/20'
                                                  }`}>
                                    {user.isActive
                                        ? 'Active'
                                        : 'Inactive'
                                    }
                                </span>
                            </div>
                        </div>
                    </div>

                    {/* Details */}
                    <div className="space-y-3">
                        <div className="flex items-center
                                        gap-3 p-3
                                        rounded-xl
                                        bg-white/3
                                        border border-white/5">
                            <Building className="w-4 h-4
                                                  text-slate-400
                                                  flex-shrink-0"/>
                            <div>
                                <p className="text-xs
                                              text-slate-500">
                                    Institution
                                </p>
                                <p className="text-sm
                                              text-white
                                              font-medium">
                                    {user.institutionName
                                        || 'Not specified'}
                                </p>
                            </div>
                        </div>

                        <div className="flex items-center
                                        gap-3 p-3
                                        rounded-xl
                                        bg-white/3
                                        border border-white/5">
                            <Calendar className="w-4 h-4
                                                  text-slate-400
                                                  flex-shrink-0"/>
                            <div>
                                <p className="text-xs
                                              text-slate-500">
                                    Joined
                                </p>
                                <p className="text-sm
                                              text-white
                                              font-medium">
                                    {formatDateTime(
                                        user.createdAt
                                    )}
                                </p>
                            </div>
                        </div>

                        <div className="flex items-center
                                        gap-3 p-3
                                        rounded-xl
                                        bg-white/3
                                        border border-white/5">
                            <Mail className="w-4 h-4
                                             text-slate-400
                                             flex-shrink-0"/>
                            <div>
                                <p className="text-xs
                                              text-slate-500">
                                    Email
                                </p>
                                <p className="text-sm
                                              text-white
                                              font-medium">
                                    {user.email}
                                </p>
                            </div>
                        </div>
                    </div>

                    {/* Action Buttons */}
                    <div className="flex gap-3">
                        {user.isActive ? (
                            <button
                                onClick={() =>
                                    handleAction('deactivate')
                                }
                                disabled={isLoading}
                                className="flex-1 btn-danger
                                           py-2.5 text-sm
                                           flex items-center
                                           justify-center
                                           gap-2
                                           disabled:opacity-50">
                                <UserX className="w-4 h-4"/>
                                {isLoading
                                    ? 'Processing...'
                                    : 'Deactivate User'
                                }
                            </button>
                        ) : (
                            <button
                                onClick={() =>
                                    handleAction('reactivate')
                                }
                                disabled={isLoading}
                                className="flex-1 btn-success
                                           py-2.5 text-sm
                                           flex items-center
                                           justify-center
                                           gap-2
                                           disabled:opacity-50">
                                <UserCheck className="w-4 h-4"/>
                                {isLoading
                                    ? 'Processing...'
                                    : 'Reactivate User'
                                }
                            </button>
                        )}
                        <button
                            onClick={onClose}
                            className="flex-1 py-2.5
                                       rounded-xl
                                       bg-white/5
                                       border border-white/10
                                       text-slate-300
                                       hover:bg-white/10
                                       transition-all
                                       text-sm font-medium">
                            Close
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

// ==========================================
// USER CARD COMPONENT
// ==========================================
const UserCard = ({ user, onViewDetails }) => (
    <div className="glass-card p-5
                    hover:scale-[1.01]
                    transition-transform duration-200
                    border border-white/5
                    hover:border-cyan-500/20">

        {/* Header */}
        <div className="flex items-start
                        justify-between mb-4">
            <div className="flex items-center gap-3">
                <div className="w-11 h-11 rounded-xl
                                bg-gradient-to-br
                                from-cyan-500/20
                                to-blue-500/20
                                border border-cyan-500/20
                                flex items-center
                                justify-center">
                    <span className="text-sm font-bold
                                     text-cyan-400">
                        {getInitials(user.fullName)}
                    </span>
                </div>
                <div>
                    <p className="text-sm font-semibold
                                  text-white">
                        {user.fullName}
                    </p>
                    <p className="text-xs text-slate-500
                                  truncate max-w-32">
                        {user.email}
                    </p>
                </div>
            </div>

            {/* Status dot */}
            <div className={`w-2.5 h-2.5 rounded-full
                             mt-1 flex-shrink-0
                             ${user.isActive
                                 ? 'bg-green-400'
                                 : 'bg-red-400'
                             }`}/>
        </div>

        {/* Info */}
        <div className="space-y-2 mb-4">
            <div className="flex items-center gap-2
                            text-xs text-slate-400">
                <Building className="w-3 h-3 flex-shrink-0"/>
                <span className="truncate">
                    {user.institutionName || 'No institution'}
                </span>
            </div>
            <div className="flex items-center gap-2
                            text-xs text-slate-400">
                <Calendar className="w-3 h-3 flex-shrink-0"/>
                <span>
                    Joined {timeAgo(user.createdAt)}
                </span>
            </div>
        </div>

        {/* Footer */}
        <div className="flex items-center
                        justify-between">
            <span className={`inline-flex items-center
                              gap-1 px-2.5 py-1
                              rounded-full text-xs
                              font-medium
                              ${user.role === 'ADMIN'
                                  ? 'bg-purple-500/10 text-purple-400 border border-purple-500/20'
                                  : 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/20'
                              }`}>
                {user.role === 'ADMIN'
                    ? <Shield className="w-3 h-3"/>
                    : <GraduationCap className="w-3 h-3"/>
                }
                {user.role}
            </span>

            <button
                onClick={() => onViewDetails(user)}
                className="flex items-center gap-1.5
                           px-3 py-1.5 rounded-lg
                           bg-white/5
                           border border-white/10
                           text-slate-300
                           hover:text-cyan-400
                           hover:border-cyan-500/20
                           transition-all text-xs">
                <Eye className="w-3 h-3"/>
                Details
            </button>
        </div>
    </div>
);

// ==========================================
// USER MANAGEMENT PAGE
// ==========================================
const UserManagement = () => {

    const [users,         setUsers]
        = useState([]);
    const [filtered,      setFiltered]
        = useState([]);
    const [isLoading,     setIsLoading]
        = useState(true);
    const [searchQuery,   setSearchQuery]
        = useState('');
    const [roleFilter,    setRoleFilter]
        = useState('ALL');
    const [statusFilter,  setStatusFilter]
        = useState('ALL');
    const [selectedUser,  setSelectedUser]
        = useState(null);
    const [viewMode,      setViewMode]
        = useState('grid');

    // ==========================================
    // FETCH USERS
    // ==========================================
    const fetchUsers = async () => {
        setIsLoading(true);
        try {
            const response = await adminService
                .getAllUsers();
            if (response.success) {
                setUsers(response.users || []);
                setFiltered(response.users || []);
            }
        } catch (error) {
            console.error('Error fetching users:', error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    // ==========================================
    // FILTER USERS
    // ==========================================
    useEffect(() => {
        let result = [...users];

        if (searchQuery) {
            result = result.filter(u =>
                u.fullName?.toLowerCase()
                 .includes(searchQuery.toLowerCase()) ||
                u.email?.toLowerCase()
                 .includes(searchQuery.toLowerCase()) ||
                u.institutionName?.toLowerCase()
                 .includes(searchQuery.toLowerCase())
            );
        }

        if (roleFilter !== 'ALL') {
            result = result.filter(
                u => u.role === roleFilter
            );
        }

        if (statusFilter === 'ACTIVE') {
            result = result.filter(u => u.isActive);
        } else if (statusFilter === 'INACTIVE') {
            result = result.filter(u => !u.isActive);
        }

        setFiltered(result);
    }, [searchQuery, roleFilter, statusFilter, users]);

    // ==========================================
    // HANDLE DEACTIVATE
    // ==========================================
    const handleDeactivate = async (id) => {
        try {
            await adminService.deactivateUser(id);
            await fetchUsers();
        } catch (error) {
            console.error('Deactivate error:', error);
            alert(
                error.response?.data?.message
                || 'Failed to deactivate user'
            );
        }
    };

    // ==========================================
    // HANDLE REACTIVATE
    // ==========================================
    const handleReactivate = async (id) => {
        try {
            await adminService.reactivateUser(id);
            await fetchUsers();
        } catch (error) {
            console.error('Reactivate error:', error);
        }
    };

    // ==========================================
    // STATS
    // ==========================================
    const stats = {
        total:    users.length,
        students: users.filter(
                      u => u.role === 'STUDENT'
                  ).length,
        admins:   users.filter(
                      u => u.role === 'ADMIN'
                  ).length,
        inactive: users.filter(
                      u => !u.isActive
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
                        User Management
                    </h1>
                    <p className="text-slate-400
                                  text-sm mt-1">
                        Manage students and administrators
                    </p>
                </div>
                <button
                    onClick={fetchUsers}
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

            {/* Stats */}
            <div className="grid grid-cols-2
                            lg:grid-cols-4 gap-4">
                {[
                    {
                        label: 'Total Users',
                        value: stats.total,
                        icon:  Users,
                        color: 'text-cyan-400',
                        bg:    'bg-cyan-500/10',
                        border:'border-cyan-500/10',
                    },
                    {
                        label: 'Students',
                        value: stats.students,
                        icon:  GraduationCap,
                        color: 'text-blue-400',
                        bg:    'bg-blue-500/10',
                        border:'border-blue-500/10',
                    },
                    {
                        label: 'Admins',
                        value: stats.admins,
                        icon:  Shield,
                        color: 'text-purple-400',
                        bg:    'bg-purple-500/10',
                        border:'border-purple-500/10',
                    },
                    {
                        label: 'Inactive',
                        value: stats.inactive,
                        icon:  UserX,
                        color: 'text-red-400',
                        bg:    'bg-red-500/10',
                        border:'border-red-500/10',
                    },
                ].map((stat, i) => (
                    <div key={i}
                         className={`glass-card p-5
                                     border ${stat.border}`}>
                        <div className="flex items-center
                                        gap-3">
                            <div className={`w-10 h-10
                                             rounded-xl
                                             flex items-center
                                             justify-center
                                             ${stat.bg}`}>
                                <stat.icon
                                    className={`w-5 h-5
                                                ${stat.color}`}
                                />
                            </div>
                            <div>
                                <p className={`text-2xl
                                               font-bold
                                               ${stat.color}`}>
                                    {stat.value}
                                </p>
                                <p className="text-xs
                                              text-slate-400">
                                    {stat.label}
                                </p>
                            </div>
                        </div>
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
                            placeholder="Search by name,
                                         email, institution..."
                            className="cyber-input pl-9
                                       h-9 text-sm"
                        />
                    </div>
                    <select
                        value={roleFilter}
                        onChange={(e) =>
                            setRoleFilter(e.target.value)
                        }
                        className="cyber-input h-9
                                   text-sm w-32
                                   cursor-pointer">
                        <option value="ALL">All Roles</option>
                        <option value="STUDENT">Student</option>
                        <option value="ADMIN">Admin</option>
                    </select>
                    <select
                        value={statusFilter}
                        onChange={(e) =>
                            setStatusFilter(e.target.value)
                        }
                        className="cyber-input h-9
                                   text-sm w-32
                                   cursor-pointer">
                        <option value="ALL">All Status</option>
                        <option value="ACTIVE">Active</option>
                        <option value="INACTIVE">
                            Inactive
                        </option>
                    </select>

                    {/* View Mode Toggle */}
                    <div className="ml-auto flex items-center
                                    gap-1 p-1 rounded-lg
                                    bg-white/3
                                    border border-white/5">
                        <button
                            onClick={() =>
                                setViewMode('grid')
                            }
                            className={`px-3 py-1.5
                                        rounded-md text-xs
                                        transition-all
                                        ${viewMode === 'grid'
                                            ? 'bg-cyan-500/20 text-cyan-400'
                                            : 'text-slate-400 hover:text-slate-200'
                                        }`}>
                            Grid
                        </button>
                        <button
                            onClick={() =>
                                setViewMode('table')
                            }
                            className={`px-3 py-1.5
                                        rounded-md text-xs
                                        transition-all
                                        ${viewMode === 'table'
                                            ? 'bg-cyan-500/20 text-cyan-400'
                                            : 'text-slate-400 hover:text-slate-200'
                                        }`}>
                            Table
                        </button>
                    </div>

                    <p className="text-xs text-slate-400">
                        {filtered.length} users
                    </p>
                </div>
            </div>

            {/* Users Display */}
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
                    <Users className="w-12 h-12
                                      text-slate-600
                                      mx-auto mb-3"/>
                    <p className="text-slate-400 text-sm">
                        No users found
                    </p>
                </div>
            ) : viewMode === 'grid' ? (
                <div className="grid grid-cols-1
                                md:grid-cols-2
                                lg:grid-cols-3 gap-4">
                    {filtered.map(user => (
                        <UserCard
                            key={user.id}
                            user={user}
                            onViewDetails={setSelectedUser}
                        />
                    ))}
                </div>
            ) : (
                <div className="glass-card overflow-hidden">
                    <table className="cyber-table">
                        <thead>
                            <tr>
                                <th>User</th>
                                <th>Role</th>
                                <th>Institution</th>
                                <th>Status</th>
                                <th>Joined</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filtered.map(user => (
                                <tr key={user.id}>
                                    <td>
                                        <div className="flex
                                                        items-center
                                                        gap-3">
                                            <div className="w-8
                                                            h-8
                                                            rounded-lg
                                                            bg-cyan-500/10
                                                            border
                                                            border-cyan-500/20
                                                            flex
                                                            items-center
                                                            justify-center
                                                            flex-shrink-0">
                                                <span className="text-xs
                                                                 font-bold
                                                                 text-cyan-400">
                                                    {getInitials(
                                                        user.fullName
                                                    )}
                                                </span>
                                            </div>
                                            <div>
                                                <p className="text-sm
                                                              font-medium
                                                              text-white">
                                                    {user.fullName}
                                                </p>
                                                <p className="text-xs
                                                              text-slate-500">
                                                    {user.email}
                                                </p>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <span className={`inline-flex
                                                          items-center
                                                          gap-1 px-2
                                                          py-0.5
                                                          rounded-full
                                                          text-xs
                                                          font-medium
                                                          ${user.role === 'ADMIN'
                                                              ? 'bg-purple-500/10 text-purple-400 border border-purple-500/20'
                                                              : 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/20'
                                                          }`}>
                                            {user.role}
                                        </span>
                                    </td>
                                    <td>
                                        <span className="text-xs
                                                         text-slate-400">
                                            {user.institutionName
                                                || 'N/A'}
                                        </span>
                                    </td>
                                    <td>
                                        <span className={`inline-flex
                                                          items-center
                                                          gap-1 px-2
                                                          py-0.5
                                                          rounded-full
                                                          text-xs
                                                          font-medium
                                                          ${user.isActive
                                                              ? 'bg-green-500/10 text-green-400 border border-green-500/20'
                                                              : 'bg-red-500/10 text-red-400 border border-red-500/20'
                                                          }`}>
                                            {user.isActive
                                                ? 'Active'
                                                : 'Inactive'
                                            }
                                        </span>
                                    </td>
                                    <td>
                                        <span className="text-xs
                                                         text-slate-400">
                                            {timeAgo(
                                                user.createdAt
                                            )}
                                        </span>
                                    </td>
                                    <td>
                                        <button
                                            onClick={() =>
                                                setSelectedUser(
                                                    user
                                                )
                                            }
                                            className="flex
                                                       items-center
                                                       gap-1.5
                                                       px-3 py-1.5
                                                       rounded-lg
                                                       bg-white/5
                                                       border
                                                       border-white/10
                                                       text-slate-300
                                                       hover:text-cyan-400
                                                       hover:border-cyan-500/20
                                                       transition-all
                                                       text-xs">
                                            <Eye className="w-3 h-3"/>
                                            View
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {/* User Detail Modal */}
            {selectedUser && (
                <UserDetailModal
                    user={selectedUser}
                    onClose={() => setSelectedUser(null)}
                    onDeactivate={handleDeactivate}
                    onReactivate={handleReactivate}
                />
            )}
        </div>
    );
};

export default UserManagement;