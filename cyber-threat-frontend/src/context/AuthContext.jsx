import React, {
    createContext,
    useContext,
    useState,
    useEffect,
    useCallback
} from 'react';
import { TOKEN_KEY, USER_KEY } from '../utils/constants';
import { storage } from '../utils/helpers';

// ==========================================
// CREATE CONTEXT
// ==========================================
const AuthContext = createContext(null);

// ==========================================
// AUTH PROVIDER
// ==========================================
export const AuthProvider = ({ children }) => {

    const [user,          setUser]
        = useState(null);
    const [token,         setToken]
        = useState(null);
    const [isLoading,     setIsLoading]
        = useState(true);
    const [isAuthenticated, setIsAuthenticated]
        = useState(false);

    // ==========================================
    // LOAD USER FROM LOCAL STORAGE ON MOUNT
    // ==========================================
    useEffect(() => {
        const savedToken = storage.get(TOKEN_KEY);
        const savedUser  = storage.get(USER_KEY);

        if (savedToken && savedUser) {
            setToken(savedToken);
            setUser(savedUser);
            setIsAuthenticated(true);
        }
        setIsLoading(false);
    }, []);

    // ==========================================
    // LOGIN
    // ==========================================
    const login = useCallback((userData, authToken) => {
        setUser(userData);
        setToken(authToken);
        setIsAuthenticated(true);
        storage.set(TOKEN_KEY, authToken);
        storage.set(USER_KEY, userData);
    }, []);

    // ==========================================
    // LOGOUT
    // ==========================================
    const logout = useCallback(() => {
        setUser(null);
        setToken(null);
        setIsAuthenticated(false);
        storage.remove(TOKEN_KEY);
        storage.remove(USER_KEY);
    }, []);

    // ==========================================
    // UPDATE USER
    // ==========================================
    const updateUser = useCallback((updatedUser) => {
        setUser(updatedUser);
        storage.set(USER_KEY, updatedUser);
    }, []);

    // ==========================================
    // CHECK IF ADMIN
    // ==========================================
    const isAdmin = useCallback(() => {
        return user?.role === 'ADMIN';
    }, [user]);

    // ==========================================
    // CHECK IF STUDENT
    // ==========================================
    const isStudent = useCallback(() => {
        return user?.role === 'STUDENT';
    }, [user]);

    // ==========================================
    // CONTEXT VALUE
    // ==========================================
    const value = {
        user,
        token,
        isLoading,
        isAuthenticated,
        login,
        logout,
        updateUser,
        isAdmin,
        isStudent,
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

// ==========================================
// USE AUTH HOOK
// ==========================================
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error(
            'useAuth must be used within AuthProvider'
        );
    }
    return context;
};

export default AuthContext;