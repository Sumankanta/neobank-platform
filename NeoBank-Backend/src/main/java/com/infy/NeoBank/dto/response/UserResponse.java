package com.infy.NeoBank.dto.response;

import com.infy.NeoBank.enums.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private boolean isActive;
    private LocalDateTime createdAt;
}
