package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.response.loan.LoanAccountResponse;
import com.infy.NeoBank.dto.response.loan.LoanApplicationResponse;
import com.infy.NeoBank.dto.response.loan.LoanProductResponse;
import com.infy.NeoBank.entity.LoanAccount;
import com.infy.NeoBank.entity.LoanApplication;
import com.infy.NeoBank.entity.LoanProduct;
import com.infy.NeoBank.exception.ResourceNotFoundException;
import com.infy.NeoBank.repository.LoanAccountRepository;
import com.infy.NeoBank.repository.LoanApplicationRepository;
import com.infy.NeoBank.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanProductRepository loanProductRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanAccountRepository loanAccountRepository;

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
        r.setUserId(a.getUser().getId());
        r.setLoanProductId(a.getLoanProduct().getId());
        r.setProductName(a.getLoanProduct().getProductName());
        r.setRequestedAmount(a.getRequestedAmount());
        r.setStatus(a.getStatus().name());
        r.setAdminRemarks(a.getAdminRemarks());
        r.setAppliedAt(a.getAppliedAt());
        r.setDecisionAt(a.getDecisionAt());
        return r;
    }

    private LoanAccountResponse mapToAccountResponse(LoanAccount a) {
        LoanAccountResponse r = new LoanAccountResponse();
        r.setId(a.getId());
        r.setUserId(a.getUser().getId());
        r.setLoanProductId(a.getLoanProduct().getId());
        r.setProductName(a.getLoanProduct().getProductName());
        r.setPrincipalAmount(a.getPrincipalAmount());
        r.setRemainingBalance(a.getRemainingBalance());
        r.setStartDate(a.getStartDate());
        r.setEndDate(a.getEndDate());
        return r;
    }
}
