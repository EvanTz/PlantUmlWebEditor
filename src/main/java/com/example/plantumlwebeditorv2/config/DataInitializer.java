package com.example.plantumlwebeditorv2.config;

import lombok.RequiredArgsConstructor;
import com.example.plantumlwebeditorv2.model.Role;
import com.example.plantumlwebeditorv2.model.RoleType;
import com.example.plantumlwebeditorv2.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Executes at the beginning of running the app
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(RoleType.ROLE_USER));
            roleRepository.save(new Role(RoleType.ROLE_ADMIN));
        }
    }
}