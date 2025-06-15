package com.example.plantumlwebeditorv2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

// Each project belongs to a single user and contains the diagram plantuml code
@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Project name cannot be blank")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    // Automatic timestamp management by Hibernate
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Automatic timestamp management by Hibernate
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // New project constructor, timestamps not needed.
    public Project(String name, String description, String content, User owner) {
        this.name = name;
        this.description = description;
        this.content = content;
        this.owner = owner;
    }

}
