package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.request.LoginRequest;
import com.infy.NeoBank.dto.request.RegisterRequest;
import com.infy.NeoBank.dto.response.AuthResponse;
import com.infy.NeoBank.dto.response.UserResponse;
import com.infy.NeoBank.entity.Account;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.enums.AccountType;
import com.infy.NeoBank.exception.DuplicateResourceException;
import com.infy.NeoBank.repository.AccountRepository;
import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infy.NeoBank.dto.request.OtpRequest;
import com.infy.NeoBank.dto.request.OtpVerificationRequest;
import com.infy.NeoBank.exception.ResourceNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDate;
import java.util.UUID;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final SmsService smsService;

    private static class OtpData {
        final String code;
        final long expiryTime;

        OtpData(String code, long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }

    private final ConcurrentHashMap<String, OtpData> otpCache = new ConcurrentHashMap<>();
    private final Random random = new Random();


    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = new User();

        // Step 1: Personal Info
        user.setFullName(request.getFullName());
        user.setDob(parseDate(request.getDob()));
        user.setGender(request.getGender());
        user.setNationality(request.getNationality());

        // Step 2: Contact Info
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        // Step 3: Identity Verification
        user.setIdType(request.getIdType());
        user.setIdNumber(request.getIdNumber());

        // Step 4: Address
        user.setStreet(request.getStreet());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setZipCode(request.getZipCode());

        // Step 5: Nominee
        user.setNomineeName(request.getNomineeName());
        user.setNomineeRelation(request.getNomineeRelationship());
        user.setNomineeDob(parseDate(request.getNomineeDob()));

        // Step 6: Preferences
        user.setPreferredAccountType(request.getAccountType());
        user.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");

        // Step 7: Security
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(false); // Pending admin approval

        User saved = userRepository.save(user);

        // Auto-create account with the user's preferred type (default SAVINGS)
        Account account = new Account();
        account.setUser(saved);
        account.setAccountType(resolveAccountType(request.getAccountType()));
        account.setAccountNumber(generateAccountNumber());
        accountRepository.save(account);

        // Send registration confirmation / pending approval email
        String emailBody = String.format(
            "Dear %s,\n\n" +
            "Welcome to NeoBank! We are excited to have you on board.\n\n" +
            "Your onboarding application has been successfully received and is currently undergoing verification by our administration team.\n\n" +
            "Please note that your bank account is currently PENDING verification. Once our team reviews and approves your application, your bank account will be activated and you will receive a confirmation email.\n\n" +
            "If you have any questions, feel free to reply to this email.\n\n" +
            "Best regards,\n" +
            "The NeoBank Team",
            saved.getFullName()
        );
        emailService.sendEmail(saved.getEmail(), "NeoBank Onboarding - Registration Successful (Pending Admin Approval)", emailBody);

        return mapToUserResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        try {
            return LocalDate.parse(date);
        } catch (Exception e) {
            return null;
        }
    }

    private AccountType resolveAccountType(String type) {
        if (type == null) return AccountType.SAVINGS;
        try {
            return AccountType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AccountType.SAVINGS;
        }
    }

    private String generateAccountNumber() {
        return "NB" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    public UserResponse mapToUserResponse(User user) {
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

    public boolean verifyDocument(String idNumber) {
        if (idNumber == null || idNumber.trim().isEmpty()) {
            return false;
        }
        return !userRepository.existsByIdNumber(idNumber.trim());
    }

    public void requestOtp(OtpRequest request) {
        String contact = request.getContact().trim();
        String type = request.getContactType().trim().toLowerCase();

        // 1. Verify user exists
        User user;
        if ("email".equals(type)) {
            user = userRepository.findByEmail(contact)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + contact));
        } else {
            user = userRepository.findByPhone(contact)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with phone number: " + contact));
        }

        // 2. Generate 6-digit code
        String otpCode = String.format("%06d", random.nextInt(900000) + 100000);
        long expiry = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutes expiration
        otpCache.put(contact, new OtpData(otpCode, expiry));

        // 3. Dispatch code
        if ("email".equals(type)) {
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "Your one-time password (OTP) for signing in to your NeoBank account is: %s\n\n" +
                "This OTP is valid for 5 minutes. Please do not share this code with anyone.\n\n" +
                "Best regards,\n" +
                "The NeoBank Team",
                user.getFullName(), otpCode
            );
            emailService.sendEmail(contact, "NeoBank - Sign in OTP Code", emailBody);
        } else {
            String smsText = String.format(
                "NeoBank: Your Sign-in OTP is %s. Valid for 5 minutes. Do not share.",
                otpCode
            );
            smsService.sendSms(contact, smsText);
        }
    }

    public AuthResponse verifyOtp(OtpVerificationRequest request) {
        String contact = request.getContact().trim();
        String code = request.getCode().trim();

        // 1. Fetch OTP
        OtpData otpData = otpCache.get(contact);
        if (otpData == null || !otpData.code.equals(code)) {
            throw new BadCredentialsException("Invalid OTP code");
        }

        // 2. Verify expiration
        if (System.currentTimeMillis() > otpData.expiryTime) {
            otpCache.remove(contact);
            throw new BadCredentialsException("OTP has expired");
        }

        // 3. Clear cache entry
        otpCache.remove(contact);

        // 4. Retrieve user
        User user = userRepository.findByEmail(contact)
            .orElseGet(() -> userRepository.findByPhone(contact)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for contact: " + contact)));

        // 5. Verify user status
        if (!user.isActive()) {
            throw new DisabledException("Account inactive. Pending admin approval.");
        }

        // 6. Generate token and return AuthResponse
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }
}
