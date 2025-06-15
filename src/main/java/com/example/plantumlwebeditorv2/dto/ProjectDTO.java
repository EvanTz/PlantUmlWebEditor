package com.example.plantumlwebeditorv2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;


//For API requests/responses to avoid exposing entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private String content;

    // Set by Hibernate
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}