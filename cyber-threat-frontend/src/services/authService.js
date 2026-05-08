import api from './api';

// ==========================================
// AUTH SERVICE
// ==========================================
const authService = {

    // ----------------------------------------
    // LOGIN
    // ----------------------------------------
    login: async (email, password) => {
        const response = await api.post('/auth/login', {
            email,
            password,
        });
        return response.data;
    },

    // ----------------------------------------
    // REGISTER
    // ----------------------------------------
    register: async (userData) => {
        const response = await api.post(
            '/auth/register',
            userData
        );
        return response.data;
    },

    // ----------------------------------------
    // GET CURRENT USER
    // ----------------------------------------
    getCurrentUser: async () => {
        const response = await api.get('/auth/me');
        return response.data;
    },

    // ----------------------------------------
    // VALIDATE TOKEN
    // ----------------------------------------
    validateToken: async () => {
        const response = await api.get('/auth/validate');
        return response.data;
    },

    // ----------------------------------------
    // CHANGE PASSWORD
    // ----------------------------------------
    changePassword: async (currentPassword, newPassword) => {
        const response = await api.put(
            '/auth/change-password',
            { currentPassword, newPassword }
        );
        return response.data;
    },

    // ----------------------------------------
    // UPDATE PROFILE
    // ----------------------------------------
    updateProfile: async (fullName, institutionName) => {
        const response = await api.put(
            '/auth/update-profile',
            { fullName, institutionName }
        );
        return response.data;
    },

    // ----------------------------------------
    // DELETE OWN ACCOUNT
    // ----------------------------------------
    deleteAccount: async () => {
        const response = await api.delete('/auth/delete-account');
        return response.data;
    },
};

export default authService;
