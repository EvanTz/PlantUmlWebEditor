package com.example.plantumlwebeditorv2.service;


import lombok.RequiredArgsConstructor;
import com.example.plantumlwebeditorv2.dto.ProjectDTO;
import com.example.plantumlwebeditorv2.model.Project;
import com.example.plantumlwebeditorv2.model.User;
import com.example.plantumlwebeditorv2.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserService userService;

    @Transactional
    public ProjectDTO saveProject(ProjectDTO projectDTO) {

        User currentUser = userService.getCurrentUser();

        Project project = new Project(
                projectDTO.getName(),
                projectDTO.getDescription(),
                projectDTO.getContent(),
                currentUser
        );

        Project savedProject = projectRepository.save(project);
        return mapToDTO(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO> getUserProjects() {
        User currentUser = userService.getCurrentUser();

        return projectRepository.findByOwner(currentUser).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public ProjectDTO getProject(Long id) {
        User currentUser = userService.getCurrentUser();
        // Extra security check with id  and user
        Project project = projectRepository.findByIdAndOwner(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return mapToDTO(project);
    }

    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO projectDTO) {
        // Validate input
        if (projectDTO.getName() != null && projectDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }

        User currentUser = userService.getCurrentUser();
        // Find existing project
        Project project = projectRepository.findByIdAndOwner(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Project not found"));

//        // Update the fields
//        project.setName(projectDTO.getName());
//        project.setDescription(projectDTO.getDescription());
//        project.setContent(projectDTO.getContent());

        // Update fields if provided
        if (projectDTO.getName() != null) {
            if (projectDTO.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Project name cannot be empty");
            }
            project.setName(projectDTO.getName());
        }

        if (projectDTO.getDescription() != null) {
            project.setDescription(projectDTO.getDescription());
        }

        if (projectDTO.getContent() != null) {
            project.setContent(projectDTO.getContent());
        }

        // Then save it
        Project updatedProject = projectRepository.save(project);
        return mapToDTO(updatedProject);
    }

    @Transactional
    public void deleteProject(Long id) {
        User currentUser = userService.getCurrentUser();
        //  Same security check as above
        Project project = projectRepository.findByIdAndOwner(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        projectRepository.delete(project);
    }

    // Convert project to dto for API response
    private ProjectDTO mapToDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setContent(project.getContent());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
        return dto;
    }

}



