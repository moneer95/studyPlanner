package com.studyplanner.security;

import com.studyplanner.domain.User;
import com.studyplanner.domain.UserRole;
import com.studyplanner.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        String role = "ROLE_" + u.getRole().name();
        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(role))
        );
    }

    public static UserRole roleFromAuthorities(org.springframework.security.core.userdetails.UserDetails details) {
        return details.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .map(UserRole::valueOf)
                .orElse(UserRole.STUDENT);
    }
}
