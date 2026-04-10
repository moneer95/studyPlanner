package com.studyplanner.service;

import com.studyplanner.domain.*;
import com.studyplanner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterStudentSuccessfully() {
        when(userRepository.existsByUsername("leen")).thenReturn(false);
        when(userRepository.existsByEmail("leen@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("hashed1234");

        Student student = new Student("leen", "hashed1234", "leen@mail.com");
        when(userRepository.save(any(User.class))).thenReturn(student);

        User result = authenticationService.register("leen", "1234", "leen@mail.com", UserRole.STUDENT);

        assertNotNull(result);
        assertEquals("leen", result.getUsername());
    }

    @Test
    void testRegisterThrowsOnDuplicateEmail() {
        when(userRepository.existsByUsername("leen2")).thenReturn(false);
        when(userRepository.existsByEmail("leen@mail.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
            authenticationService.register("leen2", "1234", "leen@mail.com", UserRole.STUDENT)
        );
    }

    @Test
    void testRegisterThrowsOnDuplicateUsername() {
        when(userRepository.existsByUsername("leen")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
            authenticationService.register("leen", "1234", "other@mail.com", UserRole.STUDENT)
        );
    }
}
