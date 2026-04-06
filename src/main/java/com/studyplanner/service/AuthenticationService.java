package com.studyplanner.service;

import com.studyplanner.domain.Admin;
import com.studyplanner.domain.Student;
import com.studyplanner.domain.Tutor;
import com.studyplanner.domain.User;
import com.studyplanner.domain.UserRole;
import com.studyplanner.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String username, String rawPassword, String email, UserRole role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        String hash = passwordEncoder.encode(rawPassword);
        User user = switch (role) {
            case STUDENT -> new Student(username, hash, email);
            case TUTOR -> new Tutor(username, hash, email);
            case ADMIN -> new Admin(username, hash, email);
        };
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }
}
