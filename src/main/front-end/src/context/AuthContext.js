import React, { createContext, useState, useContext, useEffect } from 'react';
import authService from '../services/auth.service';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    // Initialize auth state on app start
    useEffect(() => {
        const initAuth = async () => {
            try {
                const currentUser = authService.getCurrentUser();
                if (currentUser) {
                    // Validation that the stored user data still valid
                    setUser(currentUser);
                }
            } catch (error) {
                console.error('Auth initialization error:', error);
                // Clear invalid user data
                authService.logout();
            } finally {
                setLoading(false);
            }
        };

        initAuth();
    }, []);

    const login = async (username, password) => {
        try {
            const response = await authService.login(username, password);
            if (response.token) {
                setUser(response);
                return { success: true };
            }
            return { success: false, message: 'Invalid credentials' };
        } catch (error) {
            return { success: false, message: error.response?.data?.message || 'Login failed' };
        }
    };

    const register = async (username, email, password) => {
        try {
            await authService.register(username, email, password);
            return { success: true, message: 'Registration successful' };
        } catch (error) {
            // console.error('Registration error:', error);
            return { success: false, message: error.response?.data?.message || 'Registration failed' };
        }
    };

    const logout = () => {
        authService.logout();
        setUser(null);
    };


    const value = {
        user,
        login,
        register,
        logout,
        loading,
        isAuthenticated: !!user,
    };

    // Don't render children until auth state is initialized
    return <AuthContext.Provider value={value}>{!loading && children}</AuthContext.Provider>;

};


export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
