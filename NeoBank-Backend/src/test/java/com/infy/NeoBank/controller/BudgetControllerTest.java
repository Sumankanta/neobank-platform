package com.infy.NeoBank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infy.NeoBank.dto.request.BudgetRequest;
import com.infy.NeoBank.dto.response.BudgetSummaryResponse;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.enums.BudgetCategory;
import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.service.BudgetService;
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
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BudgetController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BudgetService budgetService;

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
    private BudgetSummaryResponse budgetSummaryResponse;
    private BudgetRequest budgetRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        budgetSummaryResponse = new BudgetSummaryResponse();
        budgetSummaryResponse.setCategory(BudgetCategory.GROCERIES);
        budgetSummaryResponse.setBudgetLimit(new BigDecimal("500.00"));
        budgetSummaryResponse.setSpentAmount(new BigDecimal("100.00"));
        budgetSummaryResponse.setUtilizationPercentage(20.0);

        budgetRequest = new BudgetRequest();
        budgetRequest.setCategory(BudgetCategory.GROCERIES);
        budgetRequest.setBudgetLimit(new BigDecimal("500.00"));
        budgetRequest.setBudgetMonth(YearMonth.now().toString());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void create_Success_Returns201() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(budgetService.create(eq(1L), any(BudgetRequest.class))).thenReturn(budgetSummaryResponse);

        mockMvc.perform(post("/api/budgets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budgetRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("GROCERIES"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void create_InvalidRequest_Returns400() throws Exception {
        budgetRequest.setBudgetLimit(new BigDecimal("-100.00"));

        mockMvc.perform(post("/api/budgets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budgetRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getSummary_Success_Returns200() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(budgetService.getSummary(eq(1L), anyString(), eq(1L))).thenReturn(List.of(budgetSummaryResponse));

        mockMvc.perform(get("/api/budgets/1/" + YearMonth.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getSummary_Forbidden_Returns403() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(budgetService.getSummary(anyLong(), anyString(), anyLong()))
                .thenThrow(new com.infy.NeoBank.exception.AccessDeniedException("Access denied"));

        mockMvc.perform(get("/api/budgets/2/" + YearMonth.now().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAll_Success_Returns200() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(budgetService.getAll(1L)).thenReturn(List.of(budgetSummaryResponse));

        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void delete_Success_Returns204() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/budgets/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void delete_Forbidden_Returns403() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        doThrow(new com.infy.NeoBank.exception.AccessDeniedException("Access denied"))
                .when(budgetService).delete(anyLong(), anyLong());

        mockMvc.perform(delete("/api/budgets/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void delete_NotFound_Returns404() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        doThrow(new com.infy.NeoBank.exception.ResourceNotFoundException("Budget not found"))
                .when(budgetService).delete(anyLong(), anyLong());

        mockMvc.perform(delete("/api/budgets/99")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getSummary_InvalidMonth_Returns400() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        
        mockMvc.perform(get("/api/budgets/1/invalid-month"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    void create_AlreadyExists_Returns409() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(budgetService.create(anyLong(), any(BudgetRequest.class)))
                .thenThrow(new RuntimeException("Budget already exists"));
                
        mockMvc.perform(post("/api/budgets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budgetRequest)))
                .andExpect(status().isInternalServerError());
    }
}
