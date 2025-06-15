import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    Container,
    Typography,
    TextField,
    Button,
    Grid,
    Box,
    Paper,
    CircularProgress,
    Select,
    MenuItem,
    FormControl,
    InputLabel,
    Tabs,
    Tab,
    ButtonGroup,
    Tooltip,
    // TabPanel,
} from '@mui/material';
import SaveIcon from '@mui/icons-material/Save';
import CodeIcon from '@mui/icons-material/Code';
import CodeOutlinedIcon from '@mui/icons-material/Code';
import DownloadIcon from '@mui/icons-material/Download';
import ImageIcon from '@mui/icons-material/Image';
import VisibilityIcon from '@mui/icons-material/Visibility';
import BrushIcon from '@mui/icons-material/Brush';
import projectService from '../services/project.service';
import plantUmlService from '../services/plantuml.service';
import VisualPlantUmlEditor from './VisualPlantUmlEditor';
import { toast } from 'react-toastify';
// import { TabPanel } from '@mui/lab';

// TabPanel component
function TabPanel(props) {
    const { children, value, index, ...other } = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`editor-tabpanel-${index}`}
            aria-labelledby={`editor-tab-${index}`}
            {...other}
        >
            {value === index && (
                <Box sx={{ p: 3 }}>
                    {children}
                </Box>
            )}
        </div>
    );
}

const ProjectEditor = () => {
    const { id } = useParams(); // get project ID from the URL
    const navigate = useNavigate();
    const [project, setProject] = useState({
        name: '',
        description: '',
        content: '',
    });
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [rendering, setRendering] = useState(false);
    const [renderedDiagram, setRenderedDiagram] = useState('');
    const [outputFormat, setOutputFormat] = useState('SVG');
    const [editorTab, setEditorTab] = useState(0); // 0 for code editor tab, 1 for visual editor tab
    const [downloading, setDownloading] = useState(false);

    // Load existing porject
    useEffect(() => {
        if (id) {
            fetchProject();
        }
    }, [id]);

    // Autorender when project changes with debouncing
    useEffect(() => {
        if (project.content) {
            debounceRender();
        }
    }, [project.content, outputFormat]);

    const fetchProject = async () => {
        setLoading(true);
        try {
            const response = await projectService.getProject(id);
            setProject(response.data);
        } catch (error) {
            console.error('Error fetching project:', error);
            toast.error('Failed to load project');
            // Go to projects list if project not found
            navigate('/projects');
        } finally {
            setLoading(false);
        }
    };

    const handleChange = (e) => {
        setProject({
            ...project,
            [e.target.name]: e.target.value,
        });
    };

    const handleTabChange = (event, newValue) => {
        setEditorTab(newValue);
    };

    const handleContentChange = (newContent) => {
        console.log("Content changed:", newContent);
        setProject({
            ...project,
            content: newContent,
        });
    };

    const handleSave = async () => {
        setSaving(true);
        try {
            let response;
            if (id) {
                // Update existing project
                response = await projectService.updateProject(id, project);
            } else {
                // Create new project
                response = await projectService.createProject(project);
            }

            if (response.data.id && !id) {
                navigate(`/projects/${response.data.id}/edit`);
            }
            toast.success('Project saved successfully');
        } catch (error) {
            console.error('Error saving project:', error);
            toast.error('Failed to save project');
        } finally {
            setSaving(false);
        }
    };

    // Debounce rendering to avoid to many requests
    let timeoutId;
    const debounceRender = () => {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(renderDiagram, 500); // Wait 500ms after last change
    };

    const renderDiagram = async () => {
        if (!project.content || rendering) return;

        setRendering(true);
        try {
            if (outputFormat === 'SVG') {
                const diagramSvg = await plantUmlService.renderDiagram(project.content, 'SVG');
                setRenderedDiagram(diagramSvg);
            } else {
                const diagramBlob = await plantUmlService.generateImage(project.content, outputFormat);
                const diagramUrl = URL.createObjectURL(diagramBlob);
                setRenderedDiagram(diagramUrl);
            }
        } catch (error) {
            console.error('Error rendering diagram:', error);
            setRenderedDiagram('');
        } finally {
            setRendering(false);
        }
    };

    // Download functions
    const downloadCode = () => {
        try {
            const element = document.createElement('a');

            const file = new Blob([project.content], { type: 'text/plain' });

            element.href = URL.createObjectURL(file);
            element.download = `${project.name || 'diagram'}.puml`;
            document.body.appendChild(element);
            element.click();
            document.body.removeChild(element);
            URL.revokeObjectURL(element.href);
            toast.success('PlantUML code downloaded successfully');
        } catch (error) {
            console.error('Error downloading code:', error);
            toast.error('Failed to download code');
        }
    };

    const downloadSvg = async () => {
        setDownloading(true);
        try {
            const svgContent = await plantUmlService.renderDiagram(project.content, 'SVG');

            const element = document.createElement('a');

            const file = new Blob([svgContent], { type: 'image/svg+xml' });

            element.href = URL.createObjectURL(file);
            element.download = `${project.name || 'diagram'}.svg`;
            document.body.appendChild(element);
            element.click();
            document.body.removeChild(element);
            URL.revokeObjectURL(element.href);
            toast.success('SVG diagram downloaded successfully');
        } catch (error) {
            console.error('Error downloading SVG:', error);
            toast.error('Failed to download SVG');
        } finally {
            setDownloading(false);
        }
    };

    const downloadPng = async () => {
        setDownloading(true);
        try {
            const pngBlob = await plantUmlService.generateImage(project.content, 'PNG');

            const element = document.createElement('a');

            element.href = URL.createObjectURL(pngBlob);
            element.download = `${project.name || 'diagram'}.png`;
            document.body.appendChild(element);
            element.click();
            document.body.removeChild(element);
            URL.revokeObjectURL(element.href);
            toast.success('PNG diagram downloaded successfully');
        } catch (error) {
            console.error('Error downloading PNG:', error);
            toast.error('Failed to download PNG');
        } finally {
            setDownloading(false);
        }
    };


    // WHile fetching project data
    if (loading) {
        return (
            <Container sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}>
                <CircularProgress />
            </Container>
        );
    }

    return (
        <Container maxWidth={false} sx={{ mt: 2}}  >
            <Box sx={{ my: 1 }}>
                <Typography variant="h4" component="h1" gutterBottom>
                    {id ? 'Edit Project' : 'New Project'}
                </Typography>

                <Grid container spacing={2}>
                    <Grid item xs={12}>
                        <TextField
                            required
                            fullWidth
                            label="Project Name"
                            name="name"
                            value={project.name}
                            onChange={handleChange}
                            margin="normal"
                        />
                    </Grid>

                    <Grid item xs={12}>
                        <TextField
                            fullWidth
                            label="Description"
                            name="description"
                            value={project.description}
                            onChange={handleChange}
                            margin="normal"
                            multiline
                            rows={2}
                        />
                    </Grid>

                    {/* Editor panel */}
                    <Grid item xs={12} md={6} >
                        <Paper sx={{ p: 2, overflow: 'auto',minHeight: '400px' }}>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                                <Tabs value={editorTab} onChange={handleTabChange} aria-label="editor tabs">
                                    <Tab
                                        icon={<CodeIcon />}
                                        label="Code"
                                        iconPosition="start"
                                        id="editor-tab-0"
                                        aria-controls="editor-tabpanel-0"
                                    />
                                    <Tab
                                        icon={<BrushIcon />}
                                        label="Visual"
                                        iconPosition="start"
                                        id="editor-tab-1"
                                        aria-controls="editor-tabpanel-1"
                                    />
                                </Tabs>
                                <Button
                                    variant="contained"
                                    startIcon={<SaveIcon />}
                                    onClick={handleSave}
                                    disabled={saving}
                                >
                                    {saving ? 'Saving...' : 'Save'}
                                </Button>
                            </Box>

                            {/* Code editor tab */}
                            <TabPanel value={editorTab} index={0}>
                                <TextField
                                    fullWidth
                                    multiline
                                    name="content"
                                    value={project.content}
                                    onChange={handleChange}
                                    minRows={15}
                                    maxRows={50}
                                    placeholder="@startuml
                                                Alice -> Bob: Hello
                                                Bob --> Alice: Hi there
                                                @enduml"
                                    sx={{ '& .MuiInputBase-input': { fontFamily: 'monospace' } }}
                                />
                            </TabPanel>

                            {/* Visual editor tab */}
                            <TabPanel value={editorTab} index={1}>
                                <VisualPlantUmlEditor
                                    content={project.content}
                                    onChange={handleContentChange}
                                />
                            </TabPanel>
                        </Paper>
                    </Grid>

                    {/* Preview panel */}
                    <Grid item xs={12} md={6}>
                        <Paper sx={{ p: 2, overflow: 'auto' , minHeight: '546px'}}>
                            <Box sx={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                mb: 2,
                            }}>
                                <Typography variant="h6">Preview</Typography>
                                <FormControl size="small" sx={{ minWidth: 120 }}>
                                    <InputLabel>Format</InputLabel>
                                    <Select
                                        value={outputFormat}
                                        label="Format"
                                        onChange={(e) => setOutputFormat(e.target.value)}
                                    >
                                        <MenuItem value="SVG">SVG</MenuItem>
                                        <MenuItem value="PNG">PNG</MenuItem>
                                    </Select>
                                </FormControl>

                                <ButtonGroup variant="outlined" aria-label="download buttons">
                                    <Tooltip title="Download PlantUML Code">
                                        <Button
                                            onClick={downloadCode}
                                            startIcon={<CodeOutlinedIcon />}
                                        >
                                            Code
                                        </Button>
                                    </Tooltip>
                                    <Tooltip title="Download SVG">
                                        <Button
                                            onClick={downloadSvg}
                                            startIcon={<DownloadIcon />}
                                            disabled={downloading}
                                        >
                                            SVG
                                        </Button>
                                    </Tooltip>
                                    <Tooltip title="Download PNG">
                                        <Button
                                            onClick={downloadPng}
                                            startIcon={<ImageIcon />}
                                            disabled={downloading}
                                        >
                                            PNG
                                        </Button>
                                    </Tooltip>
                                </ButtonGroup>

                            </Box>

                            <Box sx={{
                                textAlign: 'center',
                                // minHeight: '500px',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center'
                            }}>
                                {rendering ? (
                                    <CircularProgress />
                                ) : renderedDiagram ? (
                                    outputFormat === 'SVG' ? (
                                        <div  dangerouslySetInnerHTML={{ __html: renderedDiagram }} />
                                    ) : (
                                        <img src={renderedDiagram} alt="Rendered diagram" style={{ maxWidth: '100%' }} />
                                    )
                                ) : (
                                    <Typography color="text.secondary">
                                        Enter PlantUML code to see the preview
                                    </Typography>
                                )}
                            </Box>
                        </Paper>
                    </Grid>
                </Grid>
            </Box>
        </Container>
    );
};

export default ProjectEditor;