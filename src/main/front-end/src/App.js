import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import GlobalStyles from '@mui/material/GlobalStyles';
import Box from '@mui/material/Box';

import Header from './components/Header';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import ProjectEditor from './components/ProjectEditor';
import ProjectList from './components/ProjectList';
import PrivateRoute from './components/PrivateRoute';

// custom material themer
const theme = createTheme({
    palette: {
        primary: {
            main: '#155d20',
        },
        secondary: {
            main: '#b20909', // accent
        },
        background: {
            default: '#f5f5f5',
        },
    },
    typography: {
        fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    },
});

function App() {
    return (
        <ThemeProvider theme={theme}>
            <GlobalStyles
                styles={{
                    '#root': {
                        zoom: 0.9,
                    },
                }}
            />
            <CssBaseline />
            <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
                <Header />
                <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
                    <Routes>
                        {/*public routes*/}
                        <Route path="/login" element={<Login />} />
                        <Route path="/register" element={<Register />} />

                        {/*protected routes in PrivateRoute*/}
                        <Route
                            path="/"
                            element={
                                <PrivateRoute>
                                    <Dashboard />
                                </PrivateRoute>
                            }
                        />
                        <Route
                            path="/projects"
                            element={
                                <PrivateRoute>
                                    <ProjectList />
                                </PrivateRoute>
                            }
                        />
                        <Route
                            path="/projects/new"
                            element={
                                <PrivateRoute>
                                    <ProjectEditor />
                                </PrivateRoute>
                            }
                        />
                        <Route
                            path="/projects/:id/edit"
                            element={
                                <PrivateRoute>
                                    <ProjectEditor />
                                </PrivateRoute>
                            }
                        />
                    </Routes>
                </Box>
            </Box>
        </ThemeProvider>
    );
}


export default App;
