package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.request.LoginRequest;
import com.infy.NeoBank.dto.request.RegisterRequest;
import com.infy.NeoBank.dto.response.AuthResponse;
import com.infy.NeoBank.dto.response.UserResponse;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.enums.Role;
import com.infy.NeoBank.exception.DuplicateResourceException;
import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setRole(Role.CUSTOMER);
        testUser.setActive(true);

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setFullName("Test User");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("mockToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    void register_VerifyPasswordEncoded() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        authService.register(registerRequest);

        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_VerifySaveCalled() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        authService.register(registerRequest);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_VerifyTokenGenerated() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("mockToken");

        authService.login(loginRequest);

        verify(jwtUtil).generateToken(1L, "test@example.com", "CUSTOMER");
    }

    @Test
    void login_ReturnsCorrectAuthResponse() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("mockToken");

        AuthResponse response = authService.login(loginRequest);

        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(testUser.getFullName(), response.getFullName());
        assertEquals(testUser.getRole(), response.getRole());
    }

    @Test
    void register_MapsToUserResponseCorrectly() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = authService.register(registerRequest);

        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.isActive(), response.isActive());
    }
}
