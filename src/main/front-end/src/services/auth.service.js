import axios from 'axios';

const API_URL = '/api/auth/';

const register = (username, email, password) => {
    return axios.post(API_URL + 'signup', {
        username,
        email,
        password,
    });
};

const login = async (username, password) => {

    const response = await axios.post(API_URL + 'signin', {
        username,
        password,
    });

    if (response.data.token) {
        localStorage.setItem('user', JSON.stringify(response.data)); // Store JWT token in local storage
    }

    return response.data;

};


const logout = () => {
    localStorage.removeItem('user');
};


const getCurrentUser = () => {
    const userStr = localStorage.getItem('user');

    if (userStr) {

        try {
            return JSON.parse(userStr);
            // maybe need further validation?
        } catch (error) {
            console.error('Error  parsing user date :', error);
            localStorage.removeItem('user');
            return null;
        }
    }

    return null;
};


const authService = {
    register,
    login,
    logout,
    getCurrentUser,

};


export default authService;
