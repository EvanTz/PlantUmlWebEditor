package com.example.plantumlwebeditorv2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.plantumlwebeditorv2.dto.ProjectDTO;
import com.example.plantumlwebeditorv2.model.Role;
import com.example.plantumlwebeditorv2.model.RoleType;
import com.example.plantumlwebeditorv2.model.User;
import com.example.plantumlwebeditorv2.repository.RoleRepository;
import com.example.plantumlwebeditorv2.repository.UserRepository;
import com.example.plantumlwebeditorv2.security.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Set;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    private String token;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Initialize roles
        Role userRole = roleRepository.save(new Role(RoleType.ROLE_USER));

        // Create test user
        testUser = new User("testuser", "test@example.com", passwordEncoder.encode("testpassword"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);
        testUser = userRepository.save(testUser);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(testUser.getUsername(),  "testpassword"));


        token = jwtUtils.generateJwtToken(authentication);
        log.info("Token: {}", token);
    }

    @Test
    void createProject_Success() throws Exception {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("Test Project");
        projectDTO.setDescription("Test Description");
        projectDTO.setContent("@startuml\nAlice -> Bob: Hello\n@enduml");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Project")))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.content", is("@startuml\nAlice -> Bob: Hello\n@enduml")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void createProject_Unauthorized() throws Exception {

        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("Test Project");

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProjects_Success() throws Exception {

        // Create a project first
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("Test Project");
        projectDTO.setDescription("Test Description");
        projectDTO.setContent("@startuml\nAlice -> Bob: Hello\n@enduml");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isOk());

        // Get projects
        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Project")));
    }

    @Test
    void getProject_Success() throws Exception {

        // Create a project first
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("Test Project");
        projectDTO.setContent("@startuml\nAlice -> Bob: Hello\n@enduml");

        String createResponse = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ProjectDTO createdProject = objectMapper.readValue(createResponse, ProjectDTO.class);

        // Get the specific project
        mockMvc.perform(get("/api/projects/" + createdProject.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Project")))
                .andExpect(jsonPath("$.id", is(createdProject.getId().intValue())));
    }

    @Test
    void updateProject_Success() throws Exception {

        // Create a project first
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("Test Project");
        projectDTO.setContent("@startuml\nAlice -> Bob: Hello\n@enduml");

        String createResponse = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ProjectDTO createdProject = objectMapper.readValue(createResponse, ProjectDTO.class);

        // Update the project
        createdProject.setName("Updated Project");
        createdProject.setContent("@startuml\nAlice -> Bob: Updated\n@enduml");

        mockMvc.perform(put("/api/projects/" + createdProject.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdProject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Project")))
                .andExpect(jsonPath("$.content", is("@startuml\nAlice -> Bob: Updated\n@enduml")));
    }

    @Test
    void deleteProject_Success() throws Exception {

        // Create a project first
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("Test Project");
        projectDTO.setContent("@startuml\nAlice -> Bob: Hello\n@enduml");

        String createResponse = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ProjectDTO createdProject = objectMapper.readValue(createResponse, ProjectDTO.class);

        // Delete the project
        mockMvc.perform(delete("/api/projects/" + createdProject.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Project deleted successfully")));

        // Verify project no longer exists
        mockMvc.perform(get("/api/projects/" + createdProject.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void accessOtherUserProject_Failure() throws Exception {
        // Create another user
        Role userRole = roleRepository.findByName(RoleType.ROLE_USER).orElseThrow();
        User otherUser = new User("otheruser", "other@example.com", passwordEncoder.encode("password"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        otherUser.setRoles(roles);
        otherUser = userRepository.save(otherUser);


        // Generate token for other user using authentication
        Authentication otherAuth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("otheruser", "password"));
        String otherToken = jwtUtils.generateJwtToken(otherAuth);

        // Create a project with the other user
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("Other User Project");
        projectDTO.setContent("@startuml\nAlice -> Bob: Hello\n@enduml");

        String createResponse = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ProjectDTO createdProject = objectMapper.readValue(createResponse, ProjectDTO.class);

        // Try to access the project with the test user
        mockMvc.perform(get("/api/projects/" + createdProject.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

}