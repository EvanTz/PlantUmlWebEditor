package com.example.plantumlwebeditorv2.service;


import lombok.RequiredArgsConstructor;
import com.example.plantumlwebeditorv2.model.Role;
import com.example.plantumlwebeditorv2.model.RoleType;
import com.example.plantumlwebeditorv2.model.User;
import com.example.plantumlwebeditorv2.repository.RoleRepository;
import com.example.plantumlwebeditorv2.repository.UserRepository;
import com.example.plantumlwebeditorv2.security.services.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Set;
import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    //  Retrieving current authenticated user from the security context
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Get the user from the database
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

    }


    @Transactional
    public User registerUser(String username, String email, String password) {
        // Check for pre-existing users
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username is already taken");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already in use");
        }

        // Create user with encoded password
        User user = new User(username, email, passwordEncoder.encode(password));

        // Assign  user role by default
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        roles.add(userRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }


}