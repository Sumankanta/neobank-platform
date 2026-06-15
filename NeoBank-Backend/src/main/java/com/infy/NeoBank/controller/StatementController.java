package com.infy.NeoBank.controller;

import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.service.StatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/api/statements")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Statements", description = "Monthly statement generation and download")
public class StatementController {

    private final StatementService statementService;
    private final UserRepository userRepository;

    @GetMapping("/download")
    @Operation(summary = "Download monthly statement in PDF format",
            responses = {
                    @ApiResponse(responseCode = "200", description = "PDF statement returned"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            })
    public ResponseEntity<byte[]> downloadStatement(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        log.info("Statement download requested for userId={}", userId);
        
        byte[] pdfContent = statementService.generateMonthlyStatement(userId);
        if (pdfContent == null) {
            throw new RuntimeException("Failed to generate PDF statement");
        }
        
        String fileName = "NeoBank_Statement_" + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM_yyyy")) + ".pdf";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userDetails.getUsername()))
                .getId();
    }
}
