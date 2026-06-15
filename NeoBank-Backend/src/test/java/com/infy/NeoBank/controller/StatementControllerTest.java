package com.infy.NeoBank.controller;

import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.service.StatementService;
import com.infy.NeoBank.security.JwtAuthFilter;
import com.infy.NeoBank.security.JwtUtil;
import com.infy.NeoBank.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatementController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StatementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatementService statementService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void downloadStatement_Success_Returns200AndPdf() throws Exception {
        byte[] mockPdf = "Mock PDF Content".getBytes();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(statementService.generateMonthlyStatement(1L)).thenReturn(mockPdf);

        mockMvc.perform(get("/api/statements/download"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                .andExpect(content().bytes(mockPdf));
    }

    @Test
    @WithMockUser(username = "unknown@example.com")
    void downloadStatement_UserNotFound_Returns500() throws Exception {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/statements/download"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void downloadStatement_ServiceError_Returns500() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(statementService.generateMonthlyStatement(anyLong())).thenThrow(new RuntimeException("PDF generation failed"));

        mockMvc.perform(get("/api/statements/download"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void downloadStatement_EmptyContent_Returns200() throws Exception {
        byte[] mockPdf = new byte[0];
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(statementService.generateMonthlyStatement(1L)).thenReturn(mockPdf);

        mockMvc.perform(get("/api/statements/download"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(mockPdf));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void downloadStatement_LargeContent_Returns200() throws Exception {
        byte[] mockPdf = new byte[1024 * 1024]; // 1MB
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(statementService.generateMonthlyStatement(1L)).thenReturn(mockPdf);

        mockMvc.perform(get("/api/statements/download"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(mockPdf));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void downloadStatement_CheckHeaderFormat_Returns200() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(statementService.generateMonthlyStatement(1L)).thenReturn(new byte[0]);

        mockMvc.perform(get("/api/statements/download"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("attachment; filename=NeoBank_Statement_")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void downloadStatement_NullPdf_Returns500() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(statementService.generateMonthlyStatement(1L)).thenReturn(null);

        mockMvc.perform(get("/api/statements/download"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void downloadStatement_AccessDenied_Returns403() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(statementService.generateMonthlyStatement(anyLong())).thenThrow(new com.infy.NeoBank.exception.AccessDeniedException("Access denied"));

        mockMvc.perform(get("/api/statements/download"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void downloadStatement_VerifyContentType_IsPdf() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(statementService.generateMonthlyStatement(1L)).thenReturn(new byte[0]);

        mockMvc.perform(get("/api/statements/download"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void downloadStatement_CheckFilenameExtension_IsPdf() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(statementService.generateMonthlyStatement(1L)).thenReturn(new byte[0]);

        mockMvc.perform(get("/api/statements/download"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.endsWith(".pdf")));
    }
}
