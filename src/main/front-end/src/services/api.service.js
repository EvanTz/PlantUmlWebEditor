import axios from 'axios';
import authService from './auth.service';
import { toast } from 'react-toastify';

const API_URL = '/api';

// Create axios instance with auth header - default config
const api = axios.create({
    baseURL: API_URL,
    // timeout: 10000,
});

// Add auth header to requests
api.interceptors.request.use(
    (config) => {
        const user = authService.getCurrentUser();
        if (user && user.token) {
            config.headers['Authorization'] = 'Bearer ' + user.token;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }

);


// Handle token expiration
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            authService.logout();
            window.location = '/login';
        }

        //  Network errors
        if (!error.response) {
            console.error('Network error:', error.message);
            toast.error('Network error!');
        }


        return Promise.reject(error);
    }
);


export default api;