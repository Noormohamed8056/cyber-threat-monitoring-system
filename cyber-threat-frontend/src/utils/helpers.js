// ==========================================
// FORMAT DATE
// ==========================================
export const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year:  'numeric',
        month: 'short',
        day:   'numeric',
    });
};

// ==========================================
// FORMAT DATE TIME
// ==========================================
export const formatDateTime = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    const value = date.toLocaleDateString('en-IN', {
        timeZone: 'Asia/Kolkata',
        year:   'numeric',
        month:  'short',
        day:    'numeric',
        hour:   'numeric',
        minute: '2-digit',
        hour12: true,
    });
    return value.replace(' am', ' AM').replace(' pm', ' PM');
};

// ==========================================
// FORMAT TIME (IST, 12H)
// ==========================================
export const formatTimeIST = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    const value = date.toLocaleTimeString('en-IN', {
        timeZone: 'Asia/Kolkata',
        hour: 'numeric',
        minute: '2-digit',
        second: '2-digit',
        hour12: true,
    });
    return value.replace(' am', ' AM').replace(' pm', ' PM');
};

// ==========================================
// FORMAT TIME AGO
// ==========================================
export const timeAgo = (dateString) => {
    if (!dateString) return 'N/A';
    const date  = new Date(dateString);
    const now   = new Date();
    const diff  = now - date;
    const mins  = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days  = Math.floor(diff / 86400000);

    if (mins < 1)   return 'Just now';
    if (mins < 60)  return `${mins}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7)   return `${days}d ago`;
    return formatDate(dateString);
};

// ==========================================
// GET RISK LEVEL COLOR
// ==========================================
export const getRiskColor = (riskLevel) => {
    switch (riskLevel?.toUpperCase()) {
        case 'LOW':      return '#00ff88';
        case 'MEDIUM':   return '#ffd32a';
        case 'HIGH':     return '#ff4757';
        case 'CRITICAL': return '#a55eea';
        default:         return '#94a3b8';
    }
};

// ==========================================
// GET RISK LEVEL BADGE CLASS
// ==========================================
export const getRiskBadgeClass = (riskLevel) => {
    switch (riskLevel?.toUpperCase()) {
        case 'LOW':      return 'badge-low';
        case 'MEDIUM':   return 'badge-medium';
        case 'HIGH':     return 'badge-high';
        case 'CRITICAL': return 'badge-critical';
        default:         return 'badge-pending';
    }
};

// ==========================================
// GET STATUS BADGE CLASS
// ==========================================
export const getStatusBadgeClass = (status) => {
    switch (status?.toUpperCase()) {
        case 'PENDING':      return 'badge-pending';
        case 'UNDER_REVIEW': return 'badge-active';
        case 'VERIFIED':     return 'badge-verified';
        case 'DISMISSED':    return 'badge-dismissed';
        case 'ACTIVE':       return 'badge-active';
        case 'EXPIRED':      return 'badge-dismissed';
        case 'WITHDRAWN':    return 'badge-dismissed';
        default:             return 'badge-pending';
    }
};

// ==========================================
// GET SEVERITY COLOR
// ==========================================
export const getSeverityColor = (severity) => {
    switch (severity?.toUpperCase()) {
        case 'LOW':      return '#00ff88';
        case 'MEDIUM':   return '#ffd32a';
        case 'HIGH':     return '#ff4757';
        case 'CRITICAL': return '#a55eea';
        default:         return '#94a3b8';
    }
};

// ==========================================
// TRUNCATE TEXT
// ==========================================
export const truncateText = (text, maxLength = 50) => {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
};

// ==========================================
// EXTRACT URLS FROM TEXT
// ==========================================
export const extractUrls = (text) => {
    if (!text) return [];
    const matches = String(text).match(/((https?:\/\/|www\.)[^\s<>"')]+)/gi) || [];
    const seen = new Set();
    const urls = [];
    matches.forEach((entry) => {
        const item = entry.trim();
        if (!item) return;
        const key = item.toLowerCase();
        if (seen.has(key)) return;
        seen.add(key);
        urls.push(item);
    });
    return urls;
};

// ==========================================
// NORMALIZE URL
// ==========================================
export const normalizeUrl = (value) => {
    const raw = String(value || '').trim();
    if (!raw) return '';
    return /^https?:\/\//i.test(raw) ? raw : `https://${raw}`;
};

// ==========================================
// CAPITALIZE FIRST LETTER
// ==========================================
export const capitalize = (str) => {
    if (!str) return '';
    return str.charAt(0).toUpperCase()
        + str.slice(1).toLowerCase();
};

// ==========================================
// FORMAT INCIDENT TYPE
// ==========================================
export const formatIncidentType = (type) => {
    if (!type) return '';
    return type.replace(/_/g, ' ')
               .toLowerCase()
               .replace(/\b\w/g, c => c.toUpperCase());
};

// ==========================================
// GET INITIALS FROM NAME
// ==========================================
export const getInitials = (name) => {
    if (!name) return '??';
    return name
        .split(' ')
        .map(word => word[0])
        .join('')
        .toUpperCase()
        .substring(0, 2);
};

// ==========================================
// LOCAL STORAGE HELPERS
// ==========================================
export const storage = {
    get: (key) => {
        try {
            const item = localStorage.getItem(key);
            return item ? JSON.parse(item) : null;
        } catch {
            return null;
        }
    },
    set: (key, value) => {
        try {
            localStorage.setItem(key, JSON.stringify(value));
        } catch {
            console.error('Storage error');
        }
    },
    remove: (key) => {
        try {
            localStorage.removeItem(key);
        } catch {
            console.error('Storage error');
        }
    },
    clear: () => {
        try {
            localStorage.clear();
        } catch {
            console.error('Storage error');
        }
    },
};
