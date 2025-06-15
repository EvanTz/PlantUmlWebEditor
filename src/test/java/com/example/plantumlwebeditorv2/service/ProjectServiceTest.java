package com.example.plantumlwebeditorv2.service;

import com.example.plantumlwebeditorv2.dto.ProjectDTO;
import com.example.plantumlwebeditorv2.model.Project;
import com.example.plantumlwebeditorv2.model.Role;
import com.example.plantumlwebeditorv2.model.RoleType;
import com.example.plantumlwebeditorv2.model.User;
import com.example.plantumlwebeditorv2.repository.ProjectRepository;
import com.example.plantumlwebeditorv2.repository.RoleRepository;
import com.example.plantumlwebeditorv2.repository.UserRepository;
import com.example.plantumlwebeditorv2.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        // Clean up repos
        projectRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Initialize roles
        Role userRole = roleRepository.save(new Role(RoleType.ROLE_USER));

        // Create and save test users
        testUser = new User("testuser", "test@example.com", passwordEncoder.encode("testpassword"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);
        testUser = userRepository.save(testUser);

        otherUser = new User("otheruser", "other@example.com", passwordEncoder.encode("password"));
        otherUser.setRoles(roles);
        otherUser = userRepository.save(otherUser);

        // Set up security context with test user
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

    }

    @Test
    void saveProject_Success() {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("Test Project");
        projectDTO.setDescription("Test Description");
        projectDTO.setContent("@startuml\nAlice -> Bob: Hello\n@enduml");

        ProjectDTO savedProject = projectService.saveProject(projectDTO);

        assertNotNull(savedProject);
        assertNotNull(savedProject.getId());
        assertEquals("Test Project", savedProject.getName());
        assertEquals("Test Description", savedProject.getDescription());
        assertEquals("@startuml\nAlice -> Bob: Hello\n@enduml", savedProject.getContent());
        assertNotNull(savedProject.getCreatedAt());
        assertNotNull(savedProject.getUpdatedAt());
    }

    @Test
    void saveProject_WithMinimalData_Success() {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("Minimal Project");
        // No description, minimal content
        projectDTO.setContent("@startuml\n@enduml");

        ProjectDTO savedProject = projectService.saveProject(projectDTO);

        assertNotNull(savedProject);
        assertEquals("Minimal Project", savedProject.getName());
        assertNull(savedProject.getDescription());
        assertEquals("@startuml\n@enduml", savedProject.getContent());
    }

    @Test
    void saveProject_WithEmptyName_ThrowsException() {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName(""); // Empty name
        projectDTO.setContent("@startuml\n@enduml");

        assertThrows(Exception.class, () -> {
            projectService.saveProject(projectDTO);
        });
    }

    @Test
    void saveProject_WithNullContent_Success() {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("No Content Project");
        projectDTO.setContent(null);

        ProjectDTO savedProject = projectService.saveProject(projectDTO);

        assertNotNull(savedProject);
        assertEquals("No Content Project", savedProject.getName());
        assertNull(savedProject.getContent());
    }

    @Test
    void saveProject_WithInvalidPlantUML_StillSaves() {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("Invalid PlantUML");
        projectDTO.setContent("@startuml\nThis is invalid PlantUML syntax\n@enduml");

        ProjectDTO savedProject = projectService.saveProject(projectDTO);

        assertNotNull(savedProject);
        assertEquals("Invalid PlantUML", savedProject.getName());
        assertEquals("@startuml\nThis is invalid PlantUML syntax\n@enduml", savedProject.getContent());
    }

    @Test
    void getUserProjects_Success() {
        // Create projects for test user
        Project project1 = new Project("Project 1", "Description 1", "Content 1", testUser);
        Project project2 = new Project("Project 2", "Description 2", "Content 2", testUser);
        projectRepository.save(project1);
        projectRepository.save(project2);

        // Create project for other user
        Project otherProject = new Project("Other Project", "Other Description", "Other Content", otherUser);
        projectRepository.save(otherProject);

        List<ProjectDTO> userProjects = projectService.getUserProjects();

        assertEquals(2, userProjects.size());
        assertTrue(userProjects.stream().anyMatch(p -> p.getName().equals("Project 1")));
        assertTrue(userProjects.stream().anyMatch(p -> p.getName().equals("Project 2")));
        assertFalse(userProjects.stream().anyMatch(p -> p.getName().equals("Other Project")));
    }

    @Test
    void getUserProjects_NoProjects_ReturnsEmptyList() {
        List<ProjectDTO> userProjects = projectService.getUserProjects();

        assertNotNull(userProjects);
        assertTrue(userProjects.isEmpty());
    }

    @Test
    void getUserProjects_ManyProjects_ReturnsAll() {
        // Create 50 projects
        IntStream.range(0, 50).forEach(i -> {
            Project project = new Project(
                    "Project " + i,
                    "Description " + i,
                    "Content " + i,
                    testUser
            );
            projectRepository.save(project);
        });

        List<ProjectDTO> userProjects = projectService.getUserProjects();

        assertEquals(50, userProjects.size());
    }

    @Test
    void getProject_Success() {
        Project project = new Project("Test Project", "Description", "Content", testUser);
        project = projectRepository.save(project);

        ProjectDTO retrievedProject = projectService.getProject(project.getId());

        assertNotNull(retrievedProject);
        assertEquals(project.getId(), retrievedProject.getId());
        assertEquals("Test Project", retrievedProject.getName());
        assertEquals("Description", retrievedProject.getDescription());
        assertEquals("Content", retrievedProject.getContent());
    }

    @Test
    void getProject_NotFound() {
        assertThrows(RuntimeException.class, () -> {
            projectService.getProject(99999L);
        });
    }

    @Test
    void getProject_NotOwned() {
        Project otherProject = new Project("Other Project", "Description", "Content", otherUser);
        otherProject = projectRepository.save(otherProject);

        Long otherProjectId = otherProject.getId();
        assertThrows(RuntimeException.class, () -> {
            projectService.getProject(otherProjectId);
        });
    }

    @Test
    void updateProject_Success() {
        Project project = new Project("Original Name", "Original Description", "Original Content", testUser);
        project = projectRepository.save(project);

        ProjectDTO updateDTO = new ProjectDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setDescription("Updated Description");
        updateDTO.setContent("Updated Content");

        ProjectDTO updatedProject = projectService.updateProject(project.getId(), updateDTO);

        assertNotNull(updatedProject);
        assertEquals("Updated Name", updatedProject.getName());
        assertEquals("Updated Description", updatedProject.getDescription());
        assertEquals("Updated Content", updatedProject.getContent());
        assertEquals(project.getId(), updatedProject.getId());
    }

    @Test
    void updateProject_PartialUpdate_Success() {
        Project project = new Project("Original Name", "Original Description", "Original Content", testUser);
        project = projectRepository.save(project);

        ProjectDTO updateDTO = new ProjectDTO();
        updateDTO.setName("Updated Name");
        // Description and content not provided - should remain unchanged

        ProjectDTO updatedProject = projectService.updateProject(project.getId(), updateDTO);

        assertNotNull(updatedProject);
        assertEquals("Updated Name", updatedProject.getName());
        assertEquals("Original Description", updatedProject.getDescription());
        assertEquals("Original Content", updatedProject.getContent());
    }

    @Test
    void updateProject_NotFound() {
        ProjectDTO updateDTO = new ProjectDTO();
        updateDTO.setName("Updated Name");

        assertThrows(RuntimeException.class, () -> {
            projectService.updateProject(9999L, updateDTO);
        });
    }

    @Test
    void updateProject_ToInvalidState_ThrowsException() {
        Project project = new Project("Original Name", "Original Description", "Original Content", testUser);
        project = projectRepository.save(project);

        ProjectDTO updateDTO = new ProjectDTO();
        updateDTO.setName(""); // Empty name should be invalid

        Project finalProject = project;
        assertThrows(Exception.class, () -> {
            projectService.updateProject(finalProject.getId(), updateDTO);
        });
    }

    @Test
    void updateProject_NotOwned_ThrowsException() {
        Project otherProject = new Project("Other Project", "Description", "Content", otherUser);
        otherProject = projectRepository.save(otherProject);

        ProjectDTO updateDTO = new ProjectDTO();
        updateDTO.setName("Updated Name");

        Long otherProjectId = otherProject.getId();
        assertThrows(RuntimeException.class, () -> {
            projectService.updateProject(otherProjectId, updateDTO);
        });
    }

    // plantuml.render.max-size=4096 in test properties
    @Test
    void updateProject_WithLargeContent_Success() {
        Project project = new Project("Large Content Project", "Description", "Original Content", testUser);
        project = projectRepository.save(project);

        StringBuilder largeContent = new StringBuilder("@startuml\n");
        for (int i = 0; i < 1000; i++) {
            largeContent.append("Class").append(i).append(" --> Class").append(i + 1).append("\n");
        }
        largeContent.append("@enduml");

        ProjectDTO updateDTO = new ProjectDTO();
        updateDTO.setName("Updated Large Project");
        updateDTO.setContent(largeContent.toString());

        ProjectDTO updatedProject = projectService.updateProject(project.getId(), updateDTO);

        assertNotNull(updatedProject);
        assertEquals("Updated Large Project", updatedProject.getName());
        assertEquals(largeContent.toString(), updatedProject.getContent());
    }

    @Test
    void deleteProject_Success() {
        Project project = new Project("Test Project", "Description", "Content", testUser);
        project = projectRepository.save(project);
        Long projectId = project.getId();

        projectService.deleteProject(projectId);

        assertFalse(projectRepository.existsById(projectId));
    }

    @Test
    void deleteProject_NotFound() {
        assertThrows(RuntimeException.class, () -> {
            projectService.deleteProject(9999L);
        });
    }

    @Test
    void deleteProject_NotOwned() {
        Project otherProject = new Project("Other Project", "Description", "Content", otherUser);
        otherProject = projectRepository.save(otherProject);

        Long otherProjectId = otherProject.getId();
        assertThrows(RuntimeException.class, () -> {
            projectService.deleteProject(otherProjectId);
        });
    }

    @Test
    void deleteProject_WithDependencies_CascadesCorrectly() {
        // Create project with nested objects if applicable
        Project project = new Project("Project to Delete", "Description", "Content", testUser);
        // Add any dependencies if your model has them

        project = projectRepository.save(project);
        Long projectId = project.getId();

        projectService.deleteProject(projectId);

        assertFalse(projectRepository.existsById(projectId));
        // Verify any cascaded deletes if applicable
    }

    @Test
    void getUserAndOwnerInfo_ValidatesProjectOwnership() {
        // Create two projects with different owners
        Project project1 = new Project("User Project", "Description", "Content", testUser);
        Project project2 = new Project("Other Project", "Description", "Content", otherUser);

        projectRepository.save(project1);
        projectRepository.save(project2);

        // Verify we can access our own project
        ProjectDTO ownProject = projectService.getProject(project1.getId());
        assertNotNull(ownProject);

        // Verify we cannot access other user's project
        Long otherProjectId = project2.getId();
        assertThrows(RuntimeException.class, () -> {
            projectService.getProject(otherProjectId);
        });
    }

    // Testing for multiple users project access
    @Test
    void multipleUsersConcurrentAccess_EachSeeOnlyTheirProjects() {
        // Create projects for both users
        Project project1 = new Project("User1 Project", "Description", "Content", testUser);
        Project project2 = new Project("User2 Project", "Description", "Content", otherUser);

        projectRepository.save(project1);
        projectRepository.save(project2);

        // First user can see only their projects
        List<ProjectDTO> user1Projects = projectService.getUserProjects();
        assertEquals(1, user1Projects.size());
        assertEquals("User1 Project", user1Projects.get(0).getName());

        // Switch context to other user
        UserDetailsImpl otherUserDetails = UserDetailsImpl.build(otherUser);
        Authentication otherAuth = new UsernamePasswordAuthenticationToken(
                otherUserDetails,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(otherAuth);

        // Second user can see only their projects
        List<ProjectDTO> user2Projects = projectService.getUserProjects();
        assertEquals(1, user2Projects.size());
        assertEquals("User2 Project", user2Projects.get(0).getName());
    }
}