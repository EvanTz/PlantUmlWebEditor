import api from './api.service';

const PROJECTS_URL = '/projects';

//  ga
const getAllProjects = () => {
    return api.get(PROJECTS_URL);
};

const getProject = (id) => {
    return api.get(`${PROJECTS_URL}/${id}`);
};

const createProject = (projectData) => {
    return api.post(PROJECTS_URL, projectData);
};

const updateProject = (id, projectData) => {
    return api.put(`${PROJECTS_URL}/${id}`, projectData);
};


const deleteProject = (id) => {
    return api.delete(`${PROJECTS_URL}/${id}`);
};


const projectService = {
    getAllProjects,
    getProject,
    createProject,
    updateProject,
    deleteProject,
};
export default projectService;