package com.infy.NeoBank.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infy.NeoBank.enums.Role;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String dob;
    private String gender;
    private String nationality;
    private String idType;
    private String idNumber;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String nomineeName;
    private String nomineeRelation;
    private String nomineeDob;
    private String preferredAccountType;
    private String currency;
    private Role role;
    @JsonProperty("isActive")
    private boolean active;
    private LocalDateTime createdAt;
}
