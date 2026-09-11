package com.college.feedback.service;

import com.college.feedback.dto.request.LoginRequest;
import com.college.feedback.dto.request.RegisterRequest;
import com.college.feedback.dto.response.AuthResponse;
import com.college.feedback.dto.response.UserDto;
import com.college.feedback.entity.User;
import com.college.feedback.entity.enums.Role;
import com.college.feedback.exception.BadRequestException;
import com.college.feedback.exception.ResourceNotFoundException;
import com.college.feedback.repository.UserRepository;
import com.college.feedback.security.JwtUtils;
import com.college.feedback.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(authentication);

        return new AuthResponse(
                jwt,
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                userPrincipal.getFullName(),
                userPrincipal.getRole(),
                userPrincipal.getIdentifier(),
                userPrincipal.getDepartment(),
                userPrincipal.getYearOfStudy()
        );
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered: " + email);
        }

        if (request.getRole() == Role.ADMIN) {
            throw new BadRequestException("Self-registration as Administrator is not permitted. Admin accounts must be created by authorized personnel.");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.STUDENT;

        User user = new User(
                email,
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                role,
                request.getIdentifier(),
                request.getDepartment(),
                request.getYearOfStudy()
        );

        User savedUser = userRepository.save(user);

        String jwt = jwtUtils.generateTokenFromUser(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());

        return new AuthResponse(
                jwt,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole(),
                savedUser.getIdentifier(),
                savedUser.getDepartment(),
                savedUser.getYearOfStudy()
        );
    }

    public UserDto getCurrentUser(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userPrincipal.getId()));
        return UserDto.fromEntity(user);
    }

    public User getUserEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
