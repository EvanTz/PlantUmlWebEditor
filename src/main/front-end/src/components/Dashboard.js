import React, { useState, useEffect } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
    Container,
    Typography,
    Grid,
    Card,
    CardContent,
    CardActions,
    Button,
    Box,
    CircularProgress,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import FolderIcon from '@mui/icons-material/Folder';
import { useAuth } from '../context/AuthContext';
import projectService from '../services/project.service';
import { toast } from 'react-toastify';

const Dashboard = () => {
    const [projects, setProjects] = useState([]);
    const [loading, setLoading] = useState(true);
    const { user } = useAuth();

    // Load recent projects when starting component
    useEffect(() => {
        fetchProjects();
    }, []);

    const fetchProjects = async () => {
        try {
            const response = await projectService.getAllProjects();
            // Show the 6 most recent projects on dashboard - maybe change this to show all??
            setProjects(response.data.slice(0, 6)); // Show only the last 6 projects
        } catch (error) {
            console.error('Error fetching projects:', error);
            toast.error('Failed to fetch projects');
        } finally {
            setLoading(false);
        }
    };

    // Show loading icon while fetching data
    if (loading) {
        return (
            <Container sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}>
                <CircularProgress />
            </Container>
        );
    }

    return (
        <Container maxWidth="lg">
            <Box sx={{ my: 4 }}>
                <Typography variant="h4" component="h1" gutterBottom>
                    Welcome, {user?.username}
                </Typography>
                <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                    PlantUML Visual Diagram Editor
                </Typography>

                {/* Main dashboard navigation cards */}
                <Grid container spacing={3} sx={{ mt: 4 }}>
                    <Grid item xs={12} sm={6} md={6}>
                        {/* Create new project card */}
                        <Card
                            sx={{
                                height: '100%',
                                display: 'flex',
                                flexDirection: 'column',
                                bgcolor: 'primary.main',
                                color: 'primary.contrastText',
                            }}
                        >
                            <CardContent sx={{ flexGrow: 1 }}>
                                <Typography gutterBottom variant="h5" component="h2">
                                    Create New Project
                                </Typography>
                                <Typography>
                                    Start a new PlantUML diagram project
                                </Typography>
                            </CardContent>
                            <CardActions>
                                <Button
                                    size="large"
                                    color="inherit"
                                    component={RouterLink}
                                    to="/projects/new"
                                    startIcon={<AddIcon />}
                                    // sx={{ color: 'inherit' }}
                                >
                                    Create
                                </Button>
                            </CardActions>
                        </Card>
                    </Grid>


                    <Grid item xs={12} sm={6} md={6}>
                        {/* Projects overview card */}
                        <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                            <CardContent sx={{ flexGrow: 1 }}>
                                <FolderIcon color="primary" sx={{ fontSize: 40, mb: 1 }} />
                                <Typography gutterBottom variant="h5" component="h2">
                                    My Projects
                                </Typography>
                                <Typography>
                                    {projects.length} saved projects
                                </Typography>
                            </CardContent>
                            <CardActions>
                                <Button size="large" component={RouterLink} to="/projects">
                                    View All
                                </Button>
                            </CardActions>
                        </Card>
                    </Grid>

                </Grid>

                {/* Recent projects - only show if user has previous projects */}
                {projects.length > 0 && (
                    <Box sx={{ mt: 6 }}>
                        <Typography variant="h5" gutterBottom>
                            Recent Projects
                        </Typography>
                        <Grid container spacing={3}>
                            {projects.map((project) => (
                                <Grid item xs={12} sm={6} md={4} key={project.id}>
                                    <Card>
                                        <CardContent>
                                            <Typography gutterBottom variant="h6" component="h3">
                                                {project.name}
                                            </Typography>
                                            <Typography variant="body2" color="text.secondary">
                                                {project.description || 'No description'}
                                            </Typography>
                                            <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                                                Updated: {new Date(project.updatedAt).toLocaleDateString()}
                                            </Typography>
                                        </CardContent>
                                        <CardActions>
                                            <Button
                                                size="small"
                                                component={RouterLink}
                                                to={`/projects/${project.id}/edit`}
                                            >
                                                Edit
                                            </Button>
                                        </CardActions>
                                    </Card>
                                </Grid>
                            ))}
                        </Grid>
                    </Box>
                )}
            </Box>
        </Container>

    );
};


export default Dashboard;

