package com.example.plantumlwebeditorv2.repository;


import com.example.plantumlwebeditorv2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Used during authentication
    Optional<User> findByUsername(String username);

    // Used during registration
    Boolean existsByUsername(String username);

    // Used during registration
    Boolean existsByEmail(String email);

}
