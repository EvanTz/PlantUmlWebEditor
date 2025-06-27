package com.example.plantumlwebeditorv2.controller;

import lombok.RequiredArgsConstructor;
import com.example.plantumlwebeditorv2.service.PlantUmlService;
import com.example.plantumlwebeditorv2.service.PlantUmlService.OutputFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;



@RestController
@RequestMapping("/api/plantuml")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PlantUmlController {
    private final PlantUmlService plantUmlService;

    // For rendering diagrams as string (SVG, ASCII)
    @PostMapping("/render")
    public ResponseEntity<String> renderDiagram(
            @RequestBody String source,
            @RequestParam(defaultValue = "SVG") String format) throws IOException {

        OutputFormat outputFormat = OutputFormat.valueOf(format.toUpperCase());

        if (outputFormat == OutputFormat.SVG) {
            String svgContent = plantUmlService.renderDiagram(source, OutputFormat.SVG);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("image/svg+xml")) // n
                    .body(svgContent);

        } else {
//            throw new IllegalArgumentException("Only SVG and ASCII formats can be rendered as strings");
            return ResponseEntity.badRequest().build();
        }

    }


    // Diagram as binary (image response or for downloading by setting the headers appropriately)
    @PostMapping(value = "/image")
    public ResponseEntity<byte[]> generateImage(
            @RequestBody String source,
            @RequestParam(defaultValue = "PNG") String format) throws IOException {

        OutputFormat outputFormat = OutputFormat.valueOf(format.toUpperCase());
        byte[] imageData = plantUmlService.generateImage(source, outputFormat);

        HttpHeaders headers = new HttpHeaders();
        switch (outputFormat) {
            case SVG:
                headers.setContentType(MediaType.parseMediaType("image/svg+xml"));
                break;
            case PNG:
            default:
                headers.setContentType(MediaType.IMAGE_PNG);
                break;
        }

        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);

    }
}

