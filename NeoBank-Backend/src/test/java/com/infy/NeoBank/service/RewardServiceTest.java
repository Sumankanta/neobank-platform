package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.response.RewardResponse;
import com.infy.NeoBank.entity.Reward;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.exception.AccessDeniedException;
import com.infy.NeoBank.exception.InsufficientBalanceException;
import com.infy.NeoBank.exception.ResourceNotFoundException;
import com.infy.NeoBank.repository.RewardRepository;
import com.infy.NeoBank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RewardService rewardService;

    private User testUser;
    private Reward testReward;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testReward = new Reward();
        testReward.setId(1L);
        testReward.setUser(testUser);
        testReward.setPointsBalance(100);
    }

    @Test
    void getBalance_Success() {
        when(rewardRepository.findByUserId(1L)).thenReturn(Optional.of(testReward));

        RewardResponse response = rewardService.getBalance(1L, 1L);

        assertNotNull(response);
        assertEquals(100, response.getPointsBalance());
    }

    @Test
    void getBalance_AccessDenied_ThrowsException() {
        assertThrows(AccessDeniedException.class, () -> rewardService.getBalance(1L, 2L));
    }

    @Test
    void accrue_NewUser_CreatesRewardAndAddsPoints() {
        when(rewardRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(rewardRepository.save(any(Reward.class))).thenReturn(testReward);

        rewardService.accrue(1L, 50);

        verify(rewardRepository, times(2)).save(any(Reward.class));
    }

    @Test
    void accrue_ExistingUser_AddsPoints() {
        when(rewardRepository.findByUserId(1L)).thenReturn(Optional.of(testReward));

        rewardService.accrue(1L, 50);

        assertEquals(150, testReward.getPointsBalance());
        verify(rewardRepository).save(testReward);
    }

    @Test
    void deduct_Success() {
        when(rewardRepository.findByUserId(1L)).thenReturn(Optional.of(testReward));

        rewardService.deduct(1L, 50);

        assertEquals(50, testReward.getPointsBalance());
        verify(rewardRepository).save(testReward);
    }

    @Test
    void deduct_InsufficientPoints_ThrowsException() {
        when(rewardRepository.findByUserId(1L)).thenReturn(Optional.of(testReward));

        assertThrows(InsufficientBalanceException.class, () -> rewardService.deduct(1L, 150));
    }

    @Test
    void getOrCreate_UserNotFound_ThrowsException() {
        when(rewardRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rewardService.accrue(1L, 50));
    }

    @Test
    void accrue_VerifyRepositorySaveCalled() {
        when(rewardRepository.findByUserId(1L)).thenReturn(Optional.of(testReward));

        rewardService.accrue(1L, 10);

        verify(rewardRepository).save(any(Reward.class));
    }

    @Test
    void deduct_VerifyRepositorySaveCalled() {
        when(rewardRepository.findByUserId(1L)).thenReturn(Optional.of(testReward));

        rewardService.deduct(1L, 10);

        verify(rewardRepository).save(any(Reward.class));
    }

    @Test
    void mapToResponse_CorrectMapping() {
        when(rewardRepository.findByUserId(1L)).thenReturn(Optional.of(testReward));

        RewardResponse response = rewardService.getBalance(1L, 1L);

        assertEquals(testReward.getId(), response.getId());
        assertEquals(testUser.getId(), response.getUserId());
    }
}
