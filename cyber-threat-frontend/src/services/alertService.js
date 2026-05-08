import api from './api';

// ==========================================
// ALERT SERVICE
// ==========================================
const alertService = {

    // ----------------------------------------
    // GET ACTIVE ALERTS (STUDENT)
    // ----------------------------------------
    getActiveAlerts: async () => {
        const response = await api.get('/alerts/active');
        return response.data;
    },

    // ----------------------------------------
    // GET MY INSTITUTION ALERTS (STUDENT)
    // ----------------------------------------
    getMyInstitutionAlerts: async () => {
        const response = await api.get(
            '/alerts/my-institution'
        );
        return response.data;
    },

    // ----------------------------------------
    // GET CRITICAL ALERTS
    // ----------------------------------------
    getCriticalAlerts: async () => {
        const response = await api.get('/alerts/critical');
        return response.data;
    },

    // ----------------------------------------
    // GET RECENT ALERTS
    // ----------------------------------------
    getRecentAlerts: async () => {
        const response = await api.get('/alerts/recent');
        return response.data;
    },

    // ----------------------------------------
    // GET ALERT BY ID
    // ----------------------------------------
    getAlertById: async (id) => {
        const response = await api.get(`/alerts/${id}`);
        return response.data;
    },

    // ----------------------------------------
    // GET ALL ALERTS (ADMIN)
    // ----------------------------------------
    getAllAlerts: async () => {
        const response = await api.get('/alerts/all');
        return response.data;
    },

    // ----------------------------------------
    // GET ALERTS BY SEVERITY
    // ----------------------------------------
    getAlertsBySeverity: async (severity) => {
        const response = await api.get(
            `/alerts/severity/${severity}`
        );
        return response.data;
    },

    // ----------------------------------------
    // PUBLISH ALERT (ADMIN)
    // ----------------------------------------
    publishAlert: async (alertData) => {
        const response = await api.post(
            '/alerts/publish',
            alertData
        );
        return response.data;
    },

    // ----------------------------------------
    // PUBLISH FROM INCIDENT (ADMIN)
    // ----------------------------------------
    publishFromIncident: async (incidentId, data) => {
        const response = await api.post(
            `/alerts/publish-from-incident/${incidentId}`,
            data
        );
        return response.data;
    },

    // ----------------------------------------
    // UPDATE ALERT (ADMIN)
    // ----------------------------------------
    updateAlert: async (id, alertData) => {
        const response = await api.put(
            `/alerts/${id}/update`,
            alertData
        );
        return response.data;
    },

    // ----------------------------------------
    // WITHDRAW ALERT (ADMIN)
    // ----------------------------------------
    withdrawAlert: async (id) => {
        const response = await api.put(
            `/alerts/${id}/withdraw`
        );
        return response.data;
    },

    // ----------------------------------------
    // SEARCH ALERTS
    // ----------------------------------------
    searchAlerts: async (keyword) => {
        const response = await api.get(
            `/alerts/search?keyword=${keyword}`
        );
        return response.data;
    },

    // ----------------------------------------
    // GET ALERT STATS (ADMIN)
    // ----------------------------------------
    getAlertStats: async () => {
        const response = await api.get('/alerts/stats');
        return response.data;
    },

    // ----------------------------------------
    // EXPIRE OUTDATED ALERTS (ADMIN)
    // ----------------------------------------
    expireOutdatedAlerts: async () => {
        const response = await api.put(
            '/alerts/expire-outdated'
        );
        return response.data;
    },
};

export default alertService;
