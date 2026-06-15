package com.infy.NeoBank.controller;

import com.infy.NeoBank.dto.response.RewardResponse;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.service.RewardService;
import com.infy.NeoBank.security.JwtAuthFilter;
import com.infy.NeoBank.security.JwtUtil;
import com.infy.NeoBank.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RewardController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RewardService rewardService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private User user;
    private RewardResponse rewardResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        rewardResponse = new RewardResponse();
        rewardResponse.setUserId(1L);
        rewardResponse.setPointsBalance(100);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getBalance_Success_Returns200() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(rewardService.getBalance(eq(1L), eq(1L))).thenReturn(rewardResponse);

        mockMvc.perform(get("/api/rewards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointsBalance").value(100));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getBalance_Forbidden_Returns403() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(rewardService.getBalance(anyLong(), anyLong()))
                .thenThrow(new com.infy.NeoBank.exception.AccessDeniedException("Access denied"));

        mockMvc.perform(get("/api/rewards/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getBalance_NotFound_Returns404() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(rewardService.getBalance(anyLong(), anyLong()))
                .thenThrow(new com.infy.NeoBank.exception.ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/rewards/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "unknown@example.com")
    void getBalance_UserNotAuthenticated_Returns500() throws Exception {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/rewards/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getBalance_ZeroBalance_Returns200() throws Exception {
        rewardResponse.setPointsBalance(0);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(rewardService.getBalance(eq(1L), eq(1L))).thenReturn(rewardResponse);

        mockMvc.perform(get("/api/rewards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointsBalance").value(0));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getBalance_LargeBalance_Returns200() throws Exception {
        rewardResponse.setPointsBalance(999999);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(rewardService.getBalance(eq(1L), eq(1L))).thenReturn(rewardResponse);

        mockMvc.perform(get("/api/rewards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointsBalance").value(999999));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getBalance_ServiceError_Returns500() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(rewardService.getBalance(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Service Error"));

        mockMvc.perform(get("/api/rewards/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getBalance_InvalidUserIdFormat_Returns400() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        mockMvc.perform(get("/api/rewards/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getBalance_MissingUserId_Returns404() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        mockMvc.perform(get("/api/rewards/"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getBalance_NegativeUserId_Returns404_or_SuccessDependingOnService() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(rewardService.getBalance(eq(-1L), eq(1L)))
                .thenThrow(new com.infy.NeoBank.exception.ResourceNotFoundException("User not found"));
        
        mockMvc.perform(get("/api/rewards/-1"))
                .andExpect(status().isNotFound());
    }
}
