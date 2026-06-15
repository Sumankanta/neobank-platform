package com.infy.NeoBank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infy.NeoBank.dto.request.TransactionRequest;
import com.infy.NeoBank.dto.response.TransactionResponse;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.enums.TransactionType;
import com.infy.NeoBank.exception.AccessDeniedException;
import com.infy.NeoBank.exception.InsufficientBalanceException;
import com.infy.NeoBank.exception.ResourceNotFoundException;
import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.service.TransactionService;
import com.infy.NeoBank.security.JwtAuthFilter;
import com.infy.NeoBank.security.JwtUtil;
import com.infy.NeoBank.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

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
    private TransactionRequest transactionRequest;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        transactionRequest = new TransactionRequest();
        transactionRequest.setType(TransactionType.DEBIT);
        transactionRequest.setAmount(new BigDecimal("100.00"));
        transactionRequest.setDescription("Test Transaction");

        transactionResponse = new TransactionResponse();
        transactionResponse.setId(1L);
        transactionResponse.setType(TransactionType.DEBIT);
        transactionResponse.setAmount(new BigDecimal("100.00"));
        transactionResponse.setBalanceAfter(new BigDecimal("900.00"));
        transactionResponse.setDescription("Test Transaction");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void create_Success_Returns201() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionService.create(eq(1L), eq(1L), any(TransactionRequest.class))).thenReturn(transactionResponse);

        mockMvc.perform(post("/api/accounts/1/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void create_ValidationFailed_InvalidAmount_Returns400() throws Exception {
        transactionRequest.setAmount(new BigDecimal("-10.00"));

        mockMvc.perform(post("/api/accounts/1/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void create_InsufficientBalance_Returns422() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionService.create(anyLong(), anyLong(), any(TransactionRequest.class)))
                .thenThrow(new InsufficientBalanceException("Insufficient balance"));

        mockMvc.perform(post("/api/accounts/1/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void create_AccessDenied_Returns403() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionService.create(anyLong(), anyLong(), any(TransactionRequest.class)))
                .thenThrow(new AccessDeniedException("Not account owner"));

        mockMvc.perform(post("/api/accounts/1/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void create_AccountNotFound_Returns404() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionService.create(anyLong(), anyLong(), any(TransactionRequest.class)))
                .thenThrow(new ResourceNotFoundException("Account not found"));

        mockMvc.perform(post("/api/accounts/99/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getHistory_Success_Returns200() throws Exception {
        Page<TransactionResponse> page = new PageImpl<>(List.of(transactionResponse));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionService.getHistory(eq(1L), eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/accounts/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getHistory_AccessDenied_Returns403() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionService.getHistory(anyLong(), anyLong(), any(Pageable.class)))
                .thenThrow(new AccessDeniedException("Not account owner"));

        mockMvc.perform(get("/api/accounts/1/transactions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getHistory_AccountNotFound_Returns404() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionService.getHistory(anyLong(), anyLong(), any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("Account not found"));

        mockMvc.perform(get("/api/accounts/99/transactions"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getHistory_Empty_Returns200() throws Exception {
        Page<TransactionResponse> page = new PageImpl<>(List.of());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionService.getHistory(anyLong(), anyLong(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/accounts/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(0));
    }

    @Test
    @WithMockUser(username = "unknown@example.com")
    void resolveUserId_UserNotFound_Returns500() throws Exception {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/accounts/1/transactions"))
                .andExpect(status().isInternalServerError());
    }
}
