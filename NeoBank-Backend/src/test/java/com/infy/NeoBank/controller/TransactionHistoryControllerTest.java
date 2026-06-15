package com.infy.NeoBank.controller;

import com.infy.NeoBank.dto.response.TransactionResponse;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.enums.TransactionType;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionHistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TransactionHistoryControllerTest {

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

    private User user;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        transactionResponse = new TransactionResponse();
        transactionResponse.setId(1L);
        transactionResponse.setType(TransactionType.CREDIT);
        transactionResponse.setAmount(new BigDecimal("500.00"));
        transactionResponse.setBalanceAfter(new BigDecimal("1500.00"));
        transactionResponse.setTransactionDate(LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getHistory_Success_Returns200() throws Exception {
        Page<TransactionResponse> page = new PageImpl<>(List.of(transactionResponse));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionService.getAllForUser(eq(1L), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/transactions/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getHistory_WithDateRange_Success() throws Exception {
        Page<TransactionResponse> page = new PageImpl<>(List.of(transactionResponse));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionService.getAllForUser(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/transactions/history")
                        .param("startDate", "2023-01-01T00:00:00")
                        .param("endDate", "2023-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getHistory_Empty_Returns200() throws Exception {
        Page<TransactionResponse> page = new PageImpl<>(List.of());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionService.getAllForUser(anyLong(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/transactions/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(0));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getHistory_InvalidDate_Returns400() throws Exception {
        mockMvc.perform(get("/api/transactions/history")
                        .param("startDate", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getHistory_InternalError_Returns500() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionService.getAllForUser(anyLong(), any(), any(), any(Pageable.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/transactions/history"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getSummary_Success_Returns200() throws Exception {
        Map<String, BigDecimal> summary = Map.of("FOOD", new BigDecimal("50.00"));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionService.getSpendingSummary(1L)).thenReturn(summary);

        mockMvc.perform(get("/api/transactions/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.FOOD").value(50.00));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getSummary_Empty_Returns200() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionService.getSpendingSummary(1L)).thenReturn(Map.of());

        mockMvc.perform(get("/api/transactions/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getSummary_InternalError_Returns500() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionService.getSpendingSummary(anyLong())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/transactions/summary"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "unknown@example.com")
    void resolveUserId_UserNotFound_Returns500() throws Exception {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/summary"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getHistory_DefaultPagination_Returns200() throws Exception {
        Page<TransactionResponse> page = new PageImpl<>(List.of(transactionResponse));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionService.getAllForUser(anyLong(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/transactions/history")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }
}
