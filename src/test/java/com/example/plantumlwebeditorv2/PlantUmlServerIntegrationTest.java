package com.example.plantumlwebeditorv2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.plantumlwebeditorv2.dto.AuthDTOs.*;
import com.example.plantumlwebeditorv2.dto.ProjectDTO;
import com.example.plantumlwebeditorv2.model.Role;
import com.example.plantumlwebeditorv2.model.RoleType;
import com.example.plantumlwebeditorv2.repository.RoleRepository;
import com.example.plantumlwebeditorv2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PlantUmlServerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Initialize roles
        roleRepository.save(new Role(RoleType.ROLE_USER));
        roleRepository.save(new Role(RoleType.ROLE_ADMIN));
    }


    // End to end  integration testing for user register, login and project interactions
    @Test
    void fullUserTest_RegisterLoginCreateProject() throws Exception {

        // 1. Register a new user
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("testpassword");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());



        // 2. Login with the new user
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("testpassword");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse jwtResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                JwtResponse.class
        );
        String token = jwtResponse.getToken();



        // 3. Create a project
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("My Journey Project");
        projectDTO.setDescription("A project created during integration test");
        projectDTO.setContent("@startuml\nUser -> System: Create Project Test\nSystem --> User: Project Created Test\n@enduml");

        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isOk())
                .andReturn();

        ProjectDTO createdProject = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                ProjectDTO.class
        );



        // 4. Retrieve the project
        mockMvc.perform(get("/api/projects/" + createdProject.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("My Journey Project")));




        // 5. Update the project
        createdProject.setName("Updated Journey Project");
        createdProject.setContent("@startuml\nUser -> System: Update Project\nSystem --> User: Project Updated\n@enduml");

        mockMvc.perform(put("/api/projects/" + createdProject.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdProject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Journey Project")));



        // 6. Render the PlantUML diagram
        mockMvc.perform(post("/api/plantuml/render")
                        .param("format", "SVG")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(createdProject.getContent()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/svg+xml"));



        // 7. Delete the project
        mockMvc.perform(delete("/api/projects/" + createdProject.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());



        // 8. Verify project is deleted
        mockMvc.perform(get("/api/projects/" + createdProject.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }


    @Test
    void accessControl_UnauthorizedRequests() throws Exception {

        // Try to access protected endpoints without authentication
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crossUserAccess_Prevented() throws Exception {

        // Create two users
        SignupRequest user1Request = new SignupRequest();
        user1Request.setUsername("user1");
        user1Request.setEmail("user1@example.com");
        user1Request.setPassword("password1");

        SignupRequest user2Request = new SignupRequest();
        user2Request.setUsername("user2");
        user2Request.setEmail("user2@example.com");
        user2Request.setPassword("password2");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1Request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2Request)))
                .andExpect(status().isOk());

        // Login as user1
        LoginRequest loginRequest1 = new LoginRequest();
        loginRequest1.setUsername("user1");
        loginRequest1.setPassword("password1");

        MvcResult loginResult1 = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest1)))
                .andExpect(status().isOk())
                .andReturn();

//        String token1 = objectMapper.readValue(
//                loginResult1.getResponse().getContentAsString(),
//                JwtResponse.class
//        ).getToken();

        // Extract token using JsonPath
        String responseBody1 = loginResult1.getResponse().getContentAsString();
        JsonNode jsonNode1 = objectMapper.readTree(responseBody1);
        String token1 = jsonNode1.get("token").asText();



        // Login as user2
        LoginRequest loginRequest2 = new LoginRequest();
        loginRequest2.setUsername("user2");
        loginRequest2.setPassword("password2");

        MvcResult loginResult2 = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest2)))
                .andExpect(status().isOk())
                .andReturn();

//        String token2 = objectMapper.readValue(
//                loginResult2.getResponse().getContentAsString(),
//                JwtResponse.class
//        ).getToken();

        // Extract token using JsonPath
        String responseBody2 = loginResult2.getResponse().getContentAsString();
        JsonNode jsonNode2 = objectMapper.readTree(responseBody2);
        String token2 = jsonNode2.get("token").asText();


        // Create project as user1
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("User1's Project");
        projectDTO.setContent("@startuml\nA -> B\n@enduml");

        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isOk())
                .andReturn();

        Long projectId = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                ProjectDTO.class
        ).getId();


        // Try to access user1's project as user2
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isBadRequest());


        // Try to update user1's project as user2
        mockMvc.perform(put("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isBadRequest());

        // Try to delete user1's project as user2
        mockMvc.perform(delete("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isBadRequest());


    }
}