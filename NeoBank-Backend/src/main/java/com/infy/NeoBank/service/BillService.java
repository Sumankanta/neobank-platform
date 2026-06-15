package com.infy.NeoBank.service;

import com.infy.NeoBank.config.AppConstants;
import com.infy.NeoBank.dto.request.BillRequest;
import com.infy.NeoBank.dto.request.BillStatusRequest;
import com.infy.NeoBank.dto.response.BillResponse;
import com.infy.NeoBank.entity.Bill;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.enums.BillStatus;
import com.infy.NeoBank.exception.DuplicateResourceException;
import com.infy.NeoBank.exception.InvalidRequestException;
import com.infy.NeoBank.exception.ResourceNotFoundException;
import com.infy.NeoBank.repository.BillRepository;
import com.infy.NeoBank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;

    @Transactional
    public BillResponse create(Long userId, BillRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        LocalDate firstOfMonth = request.getDueDate().withDayOfMonth(1);
        LocalDate lastOfMonth = request.getDueDate().withDayOfMonth(
                request.getDueDate().lengthOfMonth());

        if (billRepository.existsByUserIdAndBillerNameAndDueDateBetween(
                userId, request.getBillerName(), firstOfMonth, lastOfMonth)) {
            throw new DuplicateResourceException(
                    "Bill already exists for " + request.getBillerName() + " this month");
        }

        Bill bill = new Bill();
        bill.setUser(user);
        bill.setBillerName(request.getBillerName());
        bill.setAmount(request.getAmount());
        bill.setDueDate(request.getDueDate());

        return mapToResponse(billRepository.save(bill));
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getAll(Long userId) {
        return billRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BillResponse getById(Long billId, Long userId) {
        Bill bill = billRepository.findByIdAndUserId(billId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + billId));
        return mapToResponse(bill);
    }

    @Transactional
    public BillResponse updateStatus(Long billId, Long userId, BillStatusRequest request) {
        Bill bill = billRepository.findByIdAndUserId(billId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + billId));

        if (bill.getStatus() != BillStatus.PENDING) {
            throw new InvalidRequestException(
                    "Invalid status transition: only PENDING bills can be updated");
        }

        bill.setStatus(request.getStatus());
        Bill saved = billRepository.save(bill);

        if (request.getStatus() == BillStatus.PAID) {
            rewardService.accrue(userId, 50);
        }

        return mapToResponse(saved);
    }

    private BillResponse mapToResponse(Bill bill) {
        BillStatus status = bill.getStatus();
        if (status == BillStatus.PENDING && LocalDate.now().isAfter(bill.getDueDate())) {
            status = BillStatus.OVERDUE;
        }

        boolean remindMe = !status.equals(BillStatus.PAID) &&
                !LocalDate.now().isAfter(bill.getDueDate()) &&
                bill.getDueDate().isBefore(LocalDate.now().plusDays(AppConstants.BILL_REMINDER_DAYS + 1));

        BillResponse r = new BillResponse();
        r.setId(bill.getId());
        r.setBillerName(bill.getBillerName());
        r.setAmount(bill.getAmount());
        r.setDueDate(bill.getDueDate());
        r.setStatus(status);
        r.setRemindMe(remindMe);
        r.setCreatedAt(bill.getCreatedAt());
        return r;
    }
}

