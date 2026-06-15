package com.infy.NeoBank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infy.NeoBank.dto.request.AccountRequest;
import com.infy.NeoBank.dto.response.AccountResponse;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.enums.AccountType;
import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.service.AccountService;
import com.infy.NeoBank.security.JwtAuthFilter;
import com.infy.NeoBank.security.JwtUtil;
import com.infy.NeoBank.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private AccountResponse accountResponse;
    private AccountRequest accountRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        accountResponse = new AccountResponse();
        accountResponse.setId(1L);
        accountResponse.setAccountNumber("1234567890");
        accountResponse.setBalance(new BigDecimal("1000.00"));
        accountResponse.setAccountType(AccountType.SAVINGS);

        accountRequest = new AccountRequest();
        accountRequest.setAccountType(AccountType.SAVINGS);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void create_Success_Returns201() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(accountService.create(eq(1L), any(AccountRequest.class))).thenReturn(accountResponse);

        mockMvc.perform(post("/api/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void create_InvalidRequest_Returns400() throws Exception {
        accountRequest.setAccountType(null); 

        mockMvc.perform(post("/api/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAll_Success_Returns200() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(accountService.getAll(1L)).thenReturn(List.of(accountResponse));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAll_Empty_Returns200() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(accountService.getAll(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getById_Success_Returns200() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(accountService.getById(eq(1L), eq(1L))).thenReturn(accountResponse);

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getById_NotFound_Returns404() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(accountService.getById(anyLong(), anyLong())).thenThrow(new com.infy.NeoBank.exception.ResourceNotFoundException("Account not found"));

        mockMvc.perform(get("/api/accounts/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getById_AccessDenied_Returns403() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(accountService.getById(anyLong(), anyLong())).thenThrow(new com.infy.NeoBank.exception.AccessDeniedException("Access denied"));

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "unknown@example.com")
    void resolveUserId_UserNotFound_ReturnsError() throws Exception {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void create_NullBody_Returns400() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAll_InternalError_Returns500() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(accountService.getAll(anyLong())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isInternalServerError());
    }
}
