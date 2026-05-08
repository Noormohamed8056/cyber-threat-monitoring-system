import axios from 'axios';
import { API_BASE_URL, TOKEN_KEY } from '../utils/constants';
import { storage } from '../utils/helpers';

// ==========================================
// CREATE AXIOS INSTANCE
// ==========================================
const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 15000,
});

// ==========================================
// REQUEST INTERCEPTOR
// Automatically attach JWT token to every request
// ==========================================
api.interceptors.request.use(
    (config) => {
        const token = storage.get(TOKEN_KEY);
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// ==========================================
// RESPONSE INTERCEPTOR
// Handle errors globally
// ==========================================
api.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        // Handle 401 Unauthorized
        if (error.response?.status === 401) {
            storage.remove(TOKEN_KEY);
            storage.remove('cyber_user');
            window.location.href = '/login';
        }

        // Handle 403 Forbidden
        if (error.response?.status === 403) {
            console.error('Access forbidden');
        }

        // Handle Network Error
        if (!error.response) {
            console.error(
                'Network error - is the backend running?'
            );
        }

        return Promise.reject(error);
    }
);

export default api;