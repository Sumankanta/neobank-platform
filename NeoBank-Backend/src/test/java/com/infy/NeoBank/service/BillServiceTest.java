package com.infy.NeoBank.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RewardService rewardService;

    @InjectMocks
    private BillService billService;

    private User testUser;
    private Bill testBill;
    private BillRequest billRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testBill = new Bill();
        testBill.setId(1L);
        testBill.setUser(testUser);
        testBill.setBillerName("Electricity");
        testBill.setAmount(BigDecimal.valueOf(100));
        testBill.setDueDate(LocalDate.now().plusDays(5));
        testBill.setStatus(BillStatus.PENDING);

        billRequest = new BillRequest();
        billRequest.setBillerName("Electricity");
        billRequest.setAmount(BigDecimal.valueOf(100));
        billRequest.setDueDate(LocalDate.now().plusDays(5));
    }

    @Test
    void create_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(billRepository.existsByUserIdAndBillerNameAndDueDateBetween(anyLong(), anyString(), any(), any())).thenReturn(false);
        when(billRepository.save(any(Bill.class))).thenReturn(testBill);

        BillResponse response = billService.create(1L, billRequest);

        assertNotNull(response);
        assertEquals(testBill.getBillerName(), response.getBillerName());
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void create_UserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> billService.create(1L, billRequest));
    }

    @Test
    void create_DuplicateBill_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(billRepository.existsByUserIdAndBillerNameAndDueDateBetween(anyLong(), anyString(), any(), any())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> billService.create(1L, billRequest));
    }

    @Test
    void getAll_Success() {
        when(billRepository.findByUserId(1L)).thenReturn(List.of(testBill));

        List<BillResponse> responses = billService.getAll(1L);

        assertEquals(1, responses.size());
        assertEquals(testBill.getBillerName(), responses.get(0).getBillerName());
    }

    @Test
    void getById_Success() {
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBill));

        BillResponse response = billService.getById(1L, 1L);

        assertNotNull(response);
        assertEquals(testBill.getId(), response.getId());
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> billService.getById(1L, 1L));
    }

    @Test
    void updateStatus_ToPaid_AccruesRewards() {
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBill));
        when(billRepository.save(any(Bill.class))).thenReturn(testBill);

        BillStatusRequest statusRequest = new BillStatusRequest();
        statusRequest.setStatus(BillStatus.PAID);

        billService.updateStatus(1L, 1L, statusRequest);

        verify(rewardService).accrue(1L, 50);
        verify(billRepository).save(testBill);
        assertEquals(BillStatus.PAID, testBill.getStatus());
    }

    @Test
    void updateStatus_AlreadyPaid_ThrowsInvalidRequest() {
        testBill.setStatus(BillStatus.PAID);
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBill));

        BillStatusRequest statusRequest = new BillStatusRequest();
        statusRequest.setStatus(BillStatus.PAID);

        assertThrows(InvalidRequestException.class, () -> billService.updateStatus(1L, 1L, statusRequest));
    }

    @Test
    void updateStatus_NotFound_ThrowsException() {
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        BillStatusRequest statusRequest = new BillStatusRequest();
        statusRequest.setStatus(BillStatus.PAID);

        assertThrows(ResourceNotFoundException.class, () -> billService.updateStatus(1L, 1L, statusRequest));
    }

    @Test
    void mapToResponse_ShouldCalculateOverdueCorrectly() {
        testBill.setDueDate(LocalDate.now().minusDays(1));
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBill));

        BillResponse response = billService.getById(1L, 1L);

        assertEquals(BillStatus.OVERDUE, response.getStatus());
    }
}
