import api from './api';

// ==========================================
// ADMIN SERVICE
// ==========================================
const adminService = {

    // ----------------------------------------
    // GET DASHBOARD
    // ----------------------------------------
    getDashboard: async () => {
        const response = await api.get('/admin/dashboard');
        return response.data;
    },

    // ----------------------------------------
    // GET ALL USERS
    // ----------------------------------------
    getAllUsers: async () => {
        const response = await api.get('/admin/users');
        return response.data;
    },

    // ----------------------------------------
    // GET ALL STUDENTS
    // ----------------------------------------
    getAllStudents: async () => {
        const response = await api.get(
            '/admin/users/students'
        );
        return response.data;
    },

    // ----------------------------------------
    // GET USER BY ID
    // ----------------------------------------
    getUserById: async (id) => {
        const response = await api.get(`/admin/users/${id}`);
        return response.data;
    },

    // ----------------------------------------
    // SEARCH USERS
    // ----------------------------------------
    searchUsers: async (keyword) => {
        const response = await api.get(
            `/admin/users/search?keyword=${keyword}`
        );
        return response.data;
    },

    // ----------------------------------------
    // DEACTIVATE USER
    // ----------------------------------------
    deactivateUser: async (id) => {
        const response = await api.put(
            `/admin/users/${id}/deactivate`
        );
        return response.data;
    },

    // ----------------------------------------
    // REACTIVATE USER
    // ----------------------------------------
    reactivateUser: async (id) => {
        const response = await api.put(
            `/admin/users/${id}/reactivate`
        );
        return response.data;
    },

    // ----------------------------------------
    // GET ALL HISTORY
    // ----------------------------------------
    getAllHistory: async () => {
        const response = await api.get('/admin/history');
        return response.data;
    },

    // ----------------------------------------
    // GET INCIDENT TIMELINE
    // ----------------------------------------
    getIncidentTimeline: async (incidentId) => {
        const response = await api.get(
            `/admin/history/incident/${incidentId}`
        );
        return response.data;
    },

    // ----------------------------------------
    // GET ADMIN ACTIONS LOG
    // ----------------------------------------
    getAdminActionsLog: async () => {
        const response = await api.get(
            '/admin/history/admin-actions'
        );
        return response.data;
    },

    // ----------------------------------------
    // GET RECENT ACTIVITY
    // ----------------------------------------
    getRecentActivity: async (days = 7) => {
        const response = await api.get(
            `/admin/history/recent?days=${days}`
        );
        return response.data;
    },

    // ----------------------------------------
    // GET ACTIVITY SUMMARY
    // ----------------------------------------
    getActivitySummary: async () => {
        const response = await api.get(
            '/admin/history/summary'
        );
        return response.data;
    },

    // ----------------------------------------
    // GET HIGH RISK HISTORY
    // ----------------------------------------
    getHighRiskHistory: async (minScore = 70) => {
        const response = await api.get(
            `/admin/history/high-risk?minScore=${minScore}`
        );
        return response.data;
    },

    // ----------------------------------------
    // GET USERS BY INSTITUTION
    // ----------------------------------------
    getUsersByInstitution: async (name) => {
        const response = await api.get(
            `/admin/users/institution?name=${name}`
        );
        return response.data;
    },
};

export default adminService;
