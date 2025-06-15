package com.example.plantumlwebeditorv2.service;


import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class PlantUmlService {

    @Value("${plantuml.render.max-size}")
    private int maxRenderSize;

    // png needs graphviz to work
    public enum OutputFormat {
        PNG, SVG, ASCII
    }

    // Main image diagram generator
    public byte[] generateImage(String source, OutputFormat format) throws IOException {
        if (source == null || source.length() > maxRenderSize) {
            throw new IllegalArgumentException("Source code too large or null");
        }

        FileFormat fileFormat;
        switch (format) {
            case SVG:
                fileFormat = FileFormat.SVG;
                break;
            case ASCII:
                fileFormat = FileFormat.ATXT;
                break;
            case PNG:
            default:
                fileFormat = FileFormat.PNG;
                break;
        }


        if (!isGraphvizInstalled()) {
            System.out.println("graphviz is not installed, needed for png generaton.");
        }

        SourceStringReader reader = new SourceStringReader(source);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        reader.outputImage(outputStream, new FileFormatOption(fileFormat));
        outputStream.close();

        return outputStream.toByteArray();
    }

    // Render diagram as a string
    public String renderDiagram(String source, OutputFormat format) throws IOException {
        if (format == OutputFormat.ASCII) {
            return new String(generateImage(source, OutputFormat.ASCII), StandardCharsets.UTF_8);
        } else
        if (format == OutputFormat.SVG) {
            return new String(generateImage(source, OutputFormat.SVG), StandardCharsets.UTF_8);
        } else {
            throw new IllegalArgumentException("Only ASCII and SVG formats can be rendered as strings");
        }
    }

    // Check in generateImage
    public boolean isGraphvizInstalled() {
        try {
            Process process = Runtime.getRuntime().exec(new String[] {"dot", "-V"});
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}