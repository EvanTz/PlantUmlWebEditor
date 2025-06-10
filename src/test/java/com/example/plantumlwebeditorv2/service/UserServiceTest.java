package com.example.plantumlwebeditorv2.service;

import com.example.plantumlwebeditorv2.model.Role;
import com.example.plantumlwebeditorv2.model.RoleType;
import com.example.plantumlwebeditorv2.model.User;
import com.example.plantumlwebeditorv2.repository.RoleRepository;
import com.example.plantumlwebeditorv2.repository.UserRepository;
import com.example.plantumlwebeditorv2.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Clean previous
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Initialize roles
        userRole = roleRepository.save(new Role(RoleType.ROLE_USER));
        roleRepository.save(new Role(RoleType.ROLE_ADMIN));

        // Create and save test user
        testUser = new User("testuser", "test@example.com", passwordEncoder.encode("testpassword"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);
        testUser = userRepository.save(testUser);

        // Set up security context
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }


    @Test
    void registerUser_Success() {
        User registeredUser = userService.registerUser("newuser", "new@example.com", "newpassword");

        assertNotNull(registeredUser);
        assertEquals("newuser", registeredUser.getUsername());
        assertEquals("new@example.com", registeredUser.getEmail());
        assertTrue(passwordEncoder.matches("newpassword", registeredUser.getPassword()));
        assertTrue(registeredUser.getRoles().contains(userRole));
    }

    @Test
    void registerUser_DuplicateUsername() {
        assertThrows(RuntimeException.class, () -> {
            userService.registerUser("testuser", "different@example.com", "password");
        });
    }

    @Test
    void registerUser_DuplicateEmail() {
        assertThrows(RuntimeException.class, () -> {
            userService.registerUser("differentuser", "test@example.com", "password");
        });
    }


    @Test
    void getCurrentUser_Success() {
        User currentUser = userService.getCurrentUser();

        assertNotNull(currentUser);
        assertEquals(testUser.getId(), currentUser.getId());
        assertEquals(testUser.getUsername(), currentUser.getUsername());
        assertEquals(testUser.getEmail(), currentUser.getEmail());
    }

    @Test
    void getCurrentUser_WithoutAuthentication() {
        SecurityContextHolder.clearContext();

        assertThrows(RuntimeException.class, () -> {
            userService.getCurrentUser();
        });

    }

}