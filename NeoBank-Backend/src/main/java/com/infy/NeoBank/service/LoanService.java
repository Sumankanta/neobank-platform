package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.request.LoanApplicationRequest;
import com.infy.NeoBank.dto.response.loan.LoanAccountResponse;
import com.infy.NeoBank.dto.response.loan.LoanApplicationResponse;
import com.infy.NeoBank.dto.response.loan.LoanProductResponse;
import com.infy.NeoBank.dto.response.loan.LoanRepaymentResponse;
import com.infy.NeoBank.entity.*;
import com.infy.NeoBank.exception.ResourceNotFoundException;
import com.infy.NeoBank.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanProductRepository loanProductRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<LoanProductResponse> getAllProducts() {
        return loanProductRepository.findAll().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getMyApplications(Long userId) {
        return loanApplicationRepository.findByUserId(userId).stream()
                .map(this::mapToApplicationResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoanAccountResponse> getMyAccounts(Long userId) {
        return loanAccountRepository.findByUserId(userId).stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanApplicationResponse applyForLoan(Long userId, LoanApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        LoanProduct product = loanProductRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan Product not found: " + request.getProductId()));

        // Validate amount limits
        if (request.getAmount().compareTo(product.getMinAmount()) < 0 || 
            request.getAmount().compareTo(product.getMaxAmount()) > 0) {
            throw new IllegalArgumentException("Requested amount must be between " + 
                product.getMinAmount() + " and " + product.getMaxAmount());
        }

        // Validate tenure limits
        if (request.getTenureMonths() < product.getMinTenureMonths() || 
            request.getTenureMonths() > product.getMaxTenureMonths()) {
            throw new IllegalArgumentException("Requested tenure must be between " + 
                product.getMinTenureMonths() + " and " + product.getMaxTenureMonths());
        }

        LoanApplication application = new LoanApplication();
        application.setUser(user);
        application.setLoanProduct(product);
        application.setRequestedAmount(request.getAmount());
        application.setTenureMonths(request.getTenureMonths());
        application.setPurpose(request.getPurpose());
        application.setStatus(LoanApplication.LoanStatus.PENDING);

        LoanApplication saved = loanApplicationRepository.save(application);
        return mapToApplicationResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getAdminApplications() {
        return loanApplicationRepository.findAll().stream()
                .map(this::mapToApplicationResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanApplicationResponse processApplication(Long applicationId, String statusStr, String remarks) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan Application not found: " + applicationId));

        LoanApplication.LoanStatus status = LoanApplication.LoanStatus.valueOf(statusStr.toUpperCase());
        application.setStatus(status);
        application.setAdminRemarks(remarks);
        application.setDecisionAt(LocalDateTime.now());

        if (status == LoanApplication.LoanStatus.APPROVED) {
            // Check if user already has an active loan account
            User user = application.getUser();
            loanAccountRepository.findOneByUserId(user.getId()).ifPresent(existing -> {
                throw new IllegalStateException("User already has an active loan account.");
            });

            // Create Loan Account
            LoanAccount loanAccount = new LoanAccount();
            loanAccount.setUser(user);
            loanAccount.setLoanProduct(application.getLoanProduct());
            loanAccount.setPrincipalAmount(application.getRequestedAmount());
            loanAccount.setRemainingBalance(application.getRequestedAmount());
            loanAccount.setStartDate(LocalDate.now());
            loanAccount.setEndDate(LocalDate.now().plusMonths(application.getTenureMonths()));

            LoanAccount savedAccount = loanAccountRepository.save(loanAccount);

            // Generate Repayment Schedule
            generateRepaymentSchedule(savedAccount, application.getTenureMonths());
        }

        LoanApplication savedApp = loanApplicationRepository.save(application);
        return mapToApplicationResponse(savedApp);
    }

    @Transactional(readOnly = true)
    public List<LoanRepaymentResponse> getRepaymentSchedule(Long loanAccountId) {
        return loanRepaymentRepository.findByLoanAccountId(loanAccountId).stream()
                .map(this::mapToRepaymentResponse)
                .collect(Collectors.toList());
    }

    private void generateRepaymentSchedule(LoanAccount account, int tenureMonths) {
        BigDecimal principal = account.getPrincipalAmount();
        BigDecimal annualRate = account.getLoanProduct().getAnnualInterestRate();
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);

        // EMI Calculation: [P x r x (1+r)^n] / [(1+r)^n - 1]
        BigDecimal emi;
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            emi = principal.divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
        } else {
            BigDecimal onePlusRToN = monthlyRate.add(BigDecimal.ONE).pow(tenureMonths);
            BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRToN);
            BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE);
            emi = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        }

        BigDecimal remainingBalance = principal;
        List<LoanRepayment> repayments = new ArrayList<>();

        for (int i = 1; i <= tenureMonths; i++) {
            BigDecimal interestPayment = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPayment = emi.subtract(interestPayment).setScale(2, RoundingMode.HALF_UP);

            // Adjust last payment to avoid rounding errors
            if (i == tenureMonths) {
                principalPayment = remainingBalance;
                emi = principalPayment.add(interestPayment);
            }

            remainingBalance = remainingBalance.subtract(principalPayment);

            LoanRepayment repayment = new LoanRepayment();
            repayment.setLoanAccount(account);
            repayment.setInstallmentNumber(i);
            repayment.setPrincipalRepaid(principalPayment);
            repayment.setInterestRepaid(interestPayment);
            repayment.setStatus(LoanRepayment.RepaymentStatus.PENDING);

            repayments.add(repayment);
        }

        loanRepaymentRepository.saveAll(repayments);
    }

    private LoanProductResponse mapToProductResponse(LoanProduct p) {
        LoanProductResponse r = new LoanProductResponse();
        r.setId(p.getId());
        r.setProductName(p.getProductName());
        r.setReferenceAmount(p.getReferenceAmount());
        r.setMinAmount(p.getMinAmount());
        r.setMaxAmount(p.getMaxAmount());
        r.setAnnualInterestRate(p.getAnnualInterestRate());
        r.setDurationMonths(p.getDurationMonths());
        r.setMinTenureMonths(p.getMinTenureMonths());
        r.setMaxTenureMonths(p.getMaxTenureMonths());
        r.setDescription(p.getDescription());
        return r;
    }

    private LoanApplicationResponse mapToApplicationResponse(LoanApplication a) {
        LoanApplicationResponse r = new LoanApplicationResponse();
        r.setId(a.getId());
        
        Long uId = null;
        String uName = "Unknown User";
        String uEmail = "unknown@neobank.com";
        try {
            if (a.getUser() != null) {
                uId = a.getUser().getId();
                uName = a.getUser().getFullName();
                uEmail = a.getUser().getEmail();
            }
        } catch (Exception e) {
            // fallback for missing/deleted user relations
        }
        
        r.setUserId(uId);
        r.setUserFullName(uName);
        r.setUserEmail(uEmail);
        
        r.setLoanProductId(a.getLoanProduct().getId());
        r.setProductName(a.getLoanProduct().getProductName());
        r.setLoanProduct(mapToProductResponse(a.getLoanProduct()));
        r.setRequestedAmount(a.getRequestedAmount());
        r.setTenureMonths(a.getTenureMonths());
        r.setPurpose(a.getPurpose());
        r.setStatus(a.getStatus().name());
        r.setAdminRemarks(a.getAdminRemarks());
        r.setAppliedAt(a.getAppliedAt());
        r.setDecisionAt(a.getDecisionAt());
        return r;
    }

    private LoanAccountResponse mapToAccountResponse(LoanAccount a) {
        LoanAccountResponse r = new LoanAccountResponse();
        r.setId(a.getId());
        
        Long uId = null;
        try {
            if (a.getUser() != null) {
                uId = a.getUser().getId();
            }
        } catch (Exception e) {
            // fallback
        }
        r.setUserId(uId);
        
        r.setLoanProduct(mapToProductResponse(a.getLoanProduct()));
        r.setPrincipalAmount(a.getPrincipalAmount());
        r.setRemainingBalance(a.getRemainingBalance());
        r.setStartDate(a.getStartDate());
        r.setEndDate(a.getEndDate());
        
        // Calculate monthly EMI
        int tenureMonths = a.getLoanProduct().getDurationMonths();
        BigDecimal principal = a.getPrincipalAmount();
        BigDecimal annualRate = a.getLoanProduct().getAnnualInterestRate();
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);
        BigDecimal emi;
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            emi = principal.divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
        } else {
            BigDecimal onePlusRToN = monthlyRate.add(BigDecimal.ONE).pow(tenureMonths);
            BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRToN);
            BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE);
            emi = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        }
        r.setMonthlyEmi(emi);
        r.setStatus("ACTIVE");
        r.setTenureMonths(tenureMonths);
        r.setInterestRate(annualRate);
        return r;
    }

    private LoanRepaymentResponse mapToRepaymentResponse(LoanRepayment r) {
        LoanRepaymentResponse res = new LoanRepaymentResponse();
        res.setId(r.getId());
        res.setLoanAccountId(r.getLoanAccount().getId());
        res.setInstallmentNumber(r.getInstallmentNumber());
        res.setDueDate(r.getLoanAccount().getStartDate().plusMonths(r.getInstallmentNumber()));
        res.setInstallmentAmount(r.getPrincipalRepaid().add(r.getInterestRepaid()));
        res.setPrincipalComponent(r.getPrincipalRepaid());
        res.setInterestComponent(r.getInterestRepaid());
        res.setPaymentDate(r.getPaidAt());
        
        // Map status: PENDING -> SCHEDULED, COMPLETED -> PAID, FAILED -> OVERDUE
        if (r.getStatus() == LoanRepayment.RepaymentStatus.COMPLETED) {
            res.setStatus("PAID");
        } else if (r.getStatus() == LoanRepayment.RepaymentStatus.FAILED) {
            res.setStatus("OVERDUE");
        } else {
            res.setStatus("SCHEDULED");
        }
        return res;
    }
}
