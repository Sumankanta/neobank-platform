package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.request.RegisterRequest;
import com.infy.NeoBank.dto.response.UserResponse;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.exception.ResourceNotFoundException;
import com.infy.NeoBank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;


    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return mapToResponse(user);
    }

    public UserResponse updateProfile(Long userId, RegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setFullName(request.getFullName());
        return mapToResponse(userRepository.save(user));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        boolean originalStatus = user.isActive();
        user.setActive(!originalStatus);
        User saved = userRepository.save(user);

        // If toggled from inactive to active, send approval email
        if (!originalStatus && saved.isActive()) {
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "We are pleased to inform you that your NeoBank account application has been reviewed and APPROVED by our administration team!\n\n" +
                "Your account status is now ACTIVE, and you can log in to start using our banking services.\n\n" +
                "Log in here: http://localhost:4200/auth/login\n\n" +
                "Thank you for choosing NeoBank!\n\n" +
                "Best regards,\n" +
                "The NeoBank Team",
                saved.getFullName()
            );
            emailService.sendEmail(saved.getEmail(), "NeoBank Account Approved & Activated!", emailBody);
        }

        return mapToResponse(saved);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setEmail(user.getEmail());
        r.setFullName(user.getFullName());
        r.setPhone(user.getPhone());
        r.setDob(user.getDob() != null ? user.getDob().toString() : null);
        r.setGender(user.getGender());
        r.setNationality(user.getNationality());
        r.setIdType(user.getIdType());
        r.setIdNumber(user.getIdNumber());
        r.setStreet(user.getStreet());
        r.setCity(user.getCity());
        r.setState(user.getState());
        r.setZipCode(user.getZipCode());
        r.setNomineeName(user.getNomineeName());
        r.setNomineeRelation(user.getNomineeRelation());
        r.setNomineeDob(user.getNomineeDob() != null ? user.getNomineeDob().toString() : null);
        r.setPreferredAccountType(user.getPreferredAccountType());
        r.setCurrency(user.getCurrency());
        r.setRole(user.getRole());
        r.setActive(user.isActive());
        r.setCreatedAt(user.getCreatedAt());
        return r;
    }
}

