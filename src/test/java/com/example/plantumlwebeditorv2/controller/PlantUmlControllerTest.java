package com.example.plantumlwebeditorv2.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PlantUmlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String VALID_PLANTUML = "@startuml\nAlice -> Bob: Hello\nBob --> Alice: Hi\n@enduml";

    @Test
    void renderDiagram_SVG_Success() throws Exception {
        mockMvc.perform(post("/api/plantuml/render")
                        .param("format", "SVG")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(VALID_PLANTUML))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/svg+xml"));
    }

    @Test
    void renderDiagram_InvalidFormat_Failure() throws Exception {
        mockMvc.perform(post("/api/plantuml/render")
                        .param("format", "PNG")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(VALID_PLANTUML))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateImage_PNG_Success() throws Exception {
        mockMvc.perform(post("/api/plantuml/image")
                        .param("format", "PNG")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(VALID_PLANTUML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    void generateImage_SVG_Success() throws Exception {
        mockMvc.perform(post("/api/plantuml/image")
                        .param("format", "SVG")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(VALID_PLANTUML))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/svg+xml"));
    }

    @Test
    void renderDiagram_EmptyContent_Success() throws Exception {
        mockMvc.perform(post("/api/plantuml/render")
                        .param("format", "SVG")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void renderDiagram_InvalidPlantUML_Success() throws Exception {
        // PlantUML typically still renders even with syntax errors
        mockMvc.perform(post("/api/plantuml/render")
                        .param("format", "SVG")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("@startuml\nInvalid syntax\n@enduml"))
                .andExpect(status().isOk());
    }

    @Test
    void generateImage_DefaultFormat_PNG() throws Exception {
        mockMvc.perform(post("/api/plantuml/image")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(VALID_PLANTUML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    void renderDiagram_DefaultFormat_SVG() throws Exception {
        mockMvc.perform(post("/api/plantuml/render")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(VALID_PLANTUML))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/svg+xml"));
    }

}