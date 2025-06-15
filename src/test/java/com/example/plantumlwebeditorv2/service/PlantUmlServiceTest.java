package com.example.plantumlwebeditorv2.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PlantUmlServiceTest {

    @Autowired
    private PlantUmlService plantUmlService;

    private static final String VALID_PLANTUML = "@startuml\nAlice -> Bob: Hello\nBob --> Alice: Hi\n@enduml";
    private static final String EMPTY_PLANTUML = "";
    private static final String INVALID_PLANTUML = "@startuml\nInvalid syntax\n@enduml";

    @Test
    void generateImage_PNG_Success() throws IOException {
        byte[] image = plantUmlService.generateImage(VALID_PLANTUML, PlantUmlService.OutputFormat.PNG);

        assertNotNull(image);
        assertTrue(image.length > 0);
        // PNG images start with specific bytes:  137 80 78 71
        assertEquals((byte) 0x89, image[0]);
        assertEquals((byte) 0x50, image[1]);
        assertEquals((byte) 0x4E, image[2]);
        assertEquals((byte) 0x47, image[3]);
    }

    @Test
    void generateImage_SVG_Success() throws IOException {
        byte[] image = plantUmlService.generateImage(VALID_PLANTUML, PlantUmlService.OutputFormat.SVG);

        assertNotNull(image);
        assertTrue(image.length > 0);
        // SVG starts with specific XML declaration
        String svgStart = new String(image, 0, Math.min(10, image.length));
        assertTrue(svgStart.contains("<?xml") || svgStart.contains("<svg"));
    }


    @Test
    void renderDiagram_SVG_Success() throws IOException {
        String svg = plantUmlService.renderDiagram(VALID_PLANTUML, PlantUmlService.OutputFormat.SVG);

        assertNotNull(svg);
        assertTrue(svg.length() > 0);
        assertTrue(svg.contains("<?xml") || svg.contains("<svg"));
    }

    @Test
    void renderDiagram_PNG_Failure() {
        assertThrows(IllegalArgumentException.class, () -> {
            plantUmlService.renderDiagram(VALID_PLANTUML, PlantUmlService.OutputFormat.PNG);
        });
    }


    @Test
    void generateImage_EmptyContent_Success() throws IOException {
        byte[] image = plantUmlService.generateImage(EMPTY_PLANTUML, PlantUmlService.OutputFormat.PNG);

        assertNotNull(image);
        assertTrue(image.length > 0);
    }

    @Test
    void generateImage_NullContent_Failure() {
        assertThrows(IllegalArgumentException.class, () -> {
            plantUmlService.generateImage(null, PlantUmlService.OutputFormat.PNG);
        });
    }


    @Test
    void generateImage_TooLargeContent_Failure() {
        StringBuilder largeContent = new StringBuilder("@startuml\n");
        for (int i = 0; i < 10000; i++) {
            largeContent.append("A -> B: Message ").append(i).append("\n");
        }
        largeContent.append("@enduml");

        assertThrows(IllegalArgumentException.class, () -> {
            plantUmlService.generateImage(largeContent.toString(), PlantUmlService.OutputFormat.PNG);
        });
    }

    @Test
    void generateImage_InvalidSyntax_Success() throws IOException {
        // PlantUML typically handles invalid syntax
        byte[] image = plantUmlService.generateImage(INVALID_PLANTUML, PlantUmlService.OutputFormat.PNG);

        assertNotNull(image);
        assertTrue(image.length > 0);
    }

}