package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.request.RegisterRequest;
import com.infy.NeoBank.dto.response.UserResponse;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.enums.Role;
import com.infy.NeoBank.exception.ResourceNotFoundException;
import com.infy.NeoBank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Updated Name");
    }

    @Test
    void getProfile_Success_ReturnsResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getProfile(1L);

        assertNotNull(response);
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getFullName(), response.getFullName());
    }

    @Test
    void getProfile_NotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getProfile(1L));
    }

    @Test
    void updateProfile_Success_ReturnsResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateProfile(1L, registerRequest);

        assertNotNull(response);
        assertEquals("Updated Name", response.getFullName());
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_NotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateProfile(1L, registerRequest));
    }

    @Test
    void getAllUsers_Empty_ReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> responses = userService.getAllUsers();

        assertTrue(responses.isEmpty());
    }

    @Test
    void getAllUsers_NonEmpty_ReturnsList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> responses = userService.getAllUsers();

        assertEquals(1, responses.size());
        assertEquals(user.getEmail(), responses.get(0).getEmail());
    }

    @Test
    void toggleUserStatus_ToInactive_ReturnsResponse() {
        user.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.toggleUserStatus(1L);

        assertFalse(response.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void toggleUserStatus_ToActive_ReturnsResponse() {
        user.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.toggleUserStatus(1L);

        assertTrue(response.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void toggleUserStatus_NotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.toggleUserStatus(1L));
    }

    @Test
    void mapToResponse_CorrectMapping() {
        // This indirectly tests mapToResponse through getProfile
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getProfile(1L);

        assertEquals(user.getId(), response.getId());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getFullName(), response.getFullName());
        assertEquals(user.getRole(), response.getRole());
        assertEquals(user.isActive(), response.isActive());
        assertEquals(user.getCreatedAt(), response.getCreatedAt());
    }
}
