import api from './api';

// ==========================================
// REPORT SERVICE
// ==========================================
const reportService = {

    // ----------------------------------------
    // SUBMIT REPORT (STUDENT)
    // ----------------------------------------
    submitReport: async (reportData) => {
        const response = await api.post(
            '/reports/submit',
            reportData
        );
        return response.data;
    },

    // ----------------------------------------
    // GET MY REPORTS (STUDENT)
    // ----------------------------------------
    getMyReports: async () => {
        const response = await api.get('/reports/my-reports');
        return response.data;
    },

    // ----------------------------------------
    // GET REPORT BY ID
    // ----------------------------------------
    getReportById: async (id) => {
        const response = await api.get(`/reports/${id}`);
        return response.data;
    },

    // ----------------------------------------
    // GET ALL REPORTS (ADMIN)
    // ----------------------------------------
    getAllReports: async () => {
        const response = await api.get('/reports/all');
        return response.data;
    },

    // ----------------------------------------
    // GET REPORTS BY STATUS (ADMIN)
    // ----------------------------------------
    getReportsByStatus: async (status) => {
        const response = await api.get(
            `/reports/status/${status}`
        );
        return response.data;
    },

    // ----------------------------------------
    // GET REPORTS BY RISK LEVEL (ADMIN)
    // ----------------------------------------
    getReportsByRiskLevel: async (riskLevel) => {
        const response = await api.get(
            `/reports/risk/${riskLevel}`
        );
        return response.data;
    },

    // ----------------------------------------
    // GET HIGH RISK PENDING (ADMIN)
    // ----------------------------------------
    getHighRiskPending: async () => {
        const response = await api.get(
            '/reports/high-risk-pending'
        );
        return response.data;
    },

    // ----------------------------------------
    // GET RECENT REPORTS (ADMIN)
    // ----------------------------------------
    getRecentReports: async () => {
        const response = await api.get('/reports/recent');
        return response.data;
    },

    // ----------------------------------------
    // START REVIEW (ADMIN)
    // ----------------------------------------
    startReview: async (id) => {
        const response = await api.put(
            `/reports/${id}/start-review`
        );
        return response.data;
    },

    // ----------------------------------------
    // VERIFY REPORT (ADMIN)
    // ----------------------------------------
    verifyReport: async (id, adminRemarks) => {
        const response = await api.put(
            `/reports/${id}/verify`,
            { adminRemarks }
        );
        return response.data;
    },

    // ----------------------------------------
    // MONITOR REPORT (ADMIN)
    // ----------------------------------------
    monitorReport: async (id, adminRemarks) => {
        const response = await api.put(
            `/reports/${id}/monitor`,
            { adminRemarks }
        );
        return response.data;
    },

    // ----------------------------------------
    // DISMISS REPORT (ADMIN)
    // ----------------------------------------
    dismissReport: async (id, adminRemarks) => {
        const response = await api.put(
            `/reports/${id}/dismiss`,
            { adminRemarks }
        );
        return response.data;
    },

    // ----------------------------------------
    // SEARCH REPORTS (ADMIN)
    // ----------------------------------------
    searchReports: async (keyword) => {
        const response = await api.get(
            `/reports/search?keyword=${keyword}`
        );
        return response.data;
    },

    // ----------------------------------------
    // GET REPORT STATS (ADMIN)
    // ----------------------------------------
    getReportStats: async () => {
        const response = await api.get('/reports/stats');
        return response.data;
    },

    // ----------------------------------------
    // CHECK DOMAIN BLOCK STATUS
    // ----------------------------------------
    checkDomainStatus: async (url) => {
        const response = await api.get(
            `/reports/domain-check?url=${encodeURIComponent(url)}`
        );
        return response.data;
    },

    // ----------------------------------------
    // EXTRACT DOMAIN FROM URL
    // ----------------------------------------
    extractDomainFromUrl: (value) => {
        if (!value) return '';
        try {
            const raw = String(value).trim();
            const normalized = /^https?:\/\//i.test(raw)
                ? raw
                : `https://${raw}`;
            return new URL(normalized).hostname.toLowerCase();
        } catch {
            return String(value).trim().toLowerCase();
        }
    },

    // ----------------------------------------
    // SHARED SECURE LINK CLICK HANDLER
    // ----------------------------------------
    handleLinkClick: async (event, url) => {
        if (event?.preventDefault) {
            event.preventDefault();
        }
        if (event?.stopPropagation) {
            event.stopPropagation();
        }
        if (!url) return;

        const raw = String(url).trim();
        const normalizedUrl = /^https?:\/\//i.test(raw)
            ? raw
            : `https://${raw}`;
        const domain = reportService.extractDomainFromUrl(normalizedUrl);
        if (!domain) return;

        try {
            const result = await reportService.checkDomainStatus(normalizedUrl);
            if (result?.blocked) {
                window.dispatchEvent(new CustomEvent('security-toast', {
                    detail: {
                        message: 'Access blocked for security',
                        type: 'error',
                    },
                }));
                return;
            }
            window.open(normalizedUrl, '_blank', 'noopener,noreferrer');
        } catch (error) {
            console.error('Domain check failed', error);
            window.open(normalizedUrl, '_blank', 'noopener,noreferrer');
        }
    },

    // ----------------------------------------
    // GET BLOCKED DOMAINS (ADMIN)
    // ----------------------------------------
    getBlockedDomains: async () => {
        const response = await api.get('/reports/blocked-domains');
        return response.data;
    },
};

export default reportService;
