package com.example.plantumlwebeditorv2.controller;


import com.example.plantumlwebeditorv2.dto.AuthDTOs;
import com.example.plantumlwebeditorv2.model.User;
import com.example.plantumlwebeditorv2.security.jwt.JwtUtils;
import com.example.plantumlwebeditorv2.security.services.UserDetailsImpl;
import com.example.plantumlwebeditorv2.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

// Cors not restricted for dev
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;


    @PostMapping("/signin") // or login
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthDTOs.LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Extract user details for response to client
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new AuthDTOs.JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody AuthDTOs.SignupRequest signUpRequest) {

        try {
            User user = userService.registerUser(
                    signUpRequest.getUsername(),
                    signUpRequest.getEmail(),
                    signUpRequest.getPassword());

            return ResponseEntity.ok(new AuthDTOs.MessageResponse("User registered successfully!"));

        } catch (RuntimeException e) {
            // Return error message from the service to client response
            return ResponseEntity.badRequest().body(new AuthDTOs.MessageResponse(e.getMessage()));
        }

    }
}


