package com.example.plantumlwebeditorv2.controller;

import lombok.RequiredArgsConstructor;
import com.example.plantumlwebeditorv2.dto.AuthDTOs.MessageResponse;
import com.example.plantumlwebeditorv2.dto.ProjectDTO;
import com.example.plantumlwebeditorv2.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;




@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    // New project creation for current user
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')") // Only authenticated users can create projects
    public ResponseEntity<?> createProject(@Valid @RequestBody ProjectDTO projectDTO) {

        try {
            ProjectDTO savedProject = projectService.saveProject(projectDTO);
            return ResponseEntity.ok(savedProject);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // Get all projects for the user
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<ProjectDTO>> getUserProjects() {
        List<ProjectDTO> projects = projectService.getUserProjects(); // Projects filtered in the service
        return ResponseEntity.ok(projects);

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getProject(@PathVariable Long id) {

        try {
            ProjectDTO project = projectService.getProject(id);
            return ResponseEntity.ok(project);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectDTO projectDTO) {

        try {
            ProjectDTO updatedProject = projectService.updateProject(id, projectDTO);
            return ResponseEntity.ok(updatedProject);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {

        try {
            projectService.deleteProject(id);
            return ResponseEntity.ok(new MessageResponse("Project deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }

    }
}