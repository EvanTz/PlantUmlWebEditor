import React, { useState, useEffect } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
    Container,
    Typography,
    Button,
    Box,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    IconButton,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import projectService from '../services/project.service';
import { toast } from 'react-toastify';

const ProjectList = () => {
    const [projects, setProjects] = useState([]);
    const [loading, setLoading] = useState(true);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [projectToDelete, setProjectToDelete] = useState(null);

    useEffect(() => {
        fetchProjects();
    }, []);

    const fetchProjects = async () => {
        try {
            const response = await projectService.getAllProjects();
            // Sort by updated date, most recent first
            // const sortedProjects = response.data.sort((a, b)  =>
            //     new Date(b.updatedAt) - new Date(a.updatedAt)
            // );
            setProjects(response.data);
        } catch (error) {
            console.error('Error fetching projects:', error);
            toast.error('Failed to fetch projects');
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteClick = (project) => {
        setProjectToDelete(project);
        setDeleteDialogOpen(true);
    };

    const handleDeleteConfirm = async () => {
        if (!projectToDelete) return;

        try {
            await projectService.deleteProject(projectToDelete.id);
            toast.success('Project deleted successfully');
            //Refresh project list
            fetchProjects();
        } catch (error) {
            console.error('Error deleting project:', error);
            toast.error('Failed to delete project');
        } finally {
            setDeleteDialogOpen(false);
            setProjectToDelete(null);
        }
    };

    const handleDeleteCancel = () => {
        setDeleteDialogOpen(false);
        setProjectToDelete(null);
    };

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
                {/* Header with title and create button */}
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                    <Typography variant="h4" component="h1">
                        My Projects
                    </Typography>
                    <Button
                        variant="contained"
                        startIcon={<AddIcon />}
                        component={RouterLink}
                        to="/projects/new"
                    >
                        New Project
                    </Button>
                </Box>

                {/* Either empty or projects table  */}
                {projects.length === 0 ? (
                    <Paper sx={{ p: 4, textAlign: 'center' }}>
                        <Typography variant="h6" gutterBottom>
                            No projects yet
                        </Typography>
                        <Typography color="text.secondary" sx={{ mb: 2 }}>
                            Start by creating your first PlantUML project
                        </Typography>
                        <Button
                            variant="contained"
                            component={RouterLink}
                            to="/projects/new"
                            startIcon={<AddIcon />}
                        >
                            Create Project
                        </Button>
                    </Paper>
                ) : (
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Name</TableCell>
                                    <TableCell>Description</TableCell>
                                    <TableCell>Created</TableCell>
                                    <TableCell>Updated</TableCell>
                                    <TableCell align="right">Actions</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {projects.map((project) => (
                                    <TableRow key={project.id} hover>
                                        <TableCell component="th" scope="row">
                                            <Typography
                                                component={RouterLink}
                                                to={`/projects/${project.id}/edit`}
                                                sx={{ textDecoration: 'none'
                                            }}>
                                                {project.name}
                                            </Typography>
                                        </TableCell>
                                        <TableCell>{project.description || '-'}</TableCell>
                                        <TableCell>{new Date(project.createdAt).toLocaleDateString()}</TableCell>
                                        <TableCell>{new Date(project.updatedAt).toLocaleDateString()}</TableCell>
                                        <TableCell align="right">
                                            <IconButton
                                                color="primary"
                                                component={RouterLink}
                                                to={`/projects/${project.id}/edit`}
                                                aria-label="edit"
                                            >
                                                <EditIcon />
                                            </IconButton>
                                            <IconButton
                                                color="error"
                                                onClick={() => handleDeleteClick(project)}
                                                aria-label="delete"
                                            >
                                                <DeleteIcon />
                                            </IconButton>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                )}
            </Box>

            {/* Delete Confirmation Dialog */}
            <Dialog open={deleteDialogOpen} onClose={handleDeleteCancel}>
                <DialogTitle>Delete Project</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        Are you sure you want to delete "{projectToDelete?.name}"? This action cannot be undone.
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleDeleteCancel}>Cancel</Button>
                    <Button onClick={handleDeleteConfirm} color="error" autoFocus>
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
};

export default ProjectList;