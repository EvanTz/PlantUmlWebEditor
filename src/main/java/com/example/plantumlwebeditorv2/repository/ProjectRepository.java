package com.example.plantumlwebeditorv2.repository;

import com.example.plantumlwebeditorv2.model.Project;
import com.example.plantumlwebeditorv2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Used for the user's project list
    List<Project> findByOwner(User owner);

    // This provides security - users can only access their own projects
    Optional<Project> findByIdAndOwner(Long id, User owner);
}