import React from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { AppBar, Toolbar, Typography, Button, Box, IconButton } from '@mui/material';
import LogoutIcon from '@mui/icons-material/Logout';
import AccountTreeIcon from '@mui/icons-material/AccountTree';
import Divider from '@mui/material/Divider';
import { useAuth } from '../context/AuthContext';


const Header = () => {
    const { user, logout, isAuthenticated } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <AppBar position="sticky" elevation={1}>
            <Toolbar>
                <IconButton
                    edge="start"
                    color="inherit"
                    component={RouterLink}
                    to="/"
                    sx={{ mr: 2 }}
                >
                    <AccountTreeIcon />
                </IconButton>

                <Typography
                    variant="h6"
                    component={RouterLink}
                    to="/"
                    sx={{
                        flexGrow: 1,
                        textDecoration: 'none',
                        color: 'inherit',
                        fontWeight: 500, // For better readability
                    }}
                >
                    PlantUML Editor
                </Typography>

                {/* Change navigation options based on auth status */}
                {isAuthenticated ? (
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                        <Button
                            color="inherit"
                            component={RouterLink}
                            to="/projects"
                        >
                            Projects
                        </Button>

                        <Divider orientation="vertical" variant="fullWidth" flexItem color={'white'}/>
                        <Typography variant="body2" sx={{ mr: 1 }}>
                            {user?.username}
                        </Typography>
                        <IconButton
                            color="inherit"
                            onClick={handleLogout}
                            aria-label="logout"
                        >
                            <LogoutIcon />
                        </IconButton>
                    </Box>
                ) : (
                    <Box sx={{ display: 'flex', gap: 1 }}>
                        <Button
                            color="inherit"
                            component={RouterLink}
                            to="/login"
                        >
                            Login
                        </Button>
                        <Button
                            color="inherit"
                            component={RouterLink}
                            to="/register"
                        >
                            Register
                        </Button>
                    </Box>
                )}
            </Toolbar>
        </AppBar>
    );
};

export default Header;

