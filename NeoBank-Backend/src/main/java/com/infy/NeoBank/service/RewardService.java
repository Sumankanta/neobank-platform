package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.response.RewardResponse;
import com.infy.NeoBank.entity.Reward;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.exception.AccessDeniedException;
import com.infy.NeoBank.exception.InsufficientBalanceException;
import com.infy.NeoBank.exception.ResourceNotFoundException;
import com.infy.NeoBank.repository.RewardRepository;
import com.infy.NeoBank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;

    @Transactional
    public RewardResponse getBalance(Long userId, Long requestingUserId) {
        if (!userId.equals(requestingUserId)) {
            throw new AccessDeniedException("Access denied to rewards of user: " + userId);
        }
        return mapToResponse(getOrCreate(userId));
    }

    @Transactional
    public void accrue(Long userId, int points) {
        Reward reward = getOrCreate(userId);
        reward.setPointsBalance(reward.getPointsBalance() + points);
        reward.setLastUpdated(LocalDateTime.now());
        rewardRepository.save(reward);
    }

    @Transactional
    public void deduct(Long userId, int points) {
        Reward reward = getOrCreate(userId);
        if (reward.getPointsBalance() < points) {
            throw new InsufficientBalanceException("Insufficient reward points balance");
        }
        reward.setPointsBalance(reward.getPointsBalance() - points);
        reward.setLastUpdated(LocalDateTime.now());
        rewardRepository.save(reward);
    }

    private Reward getOrCreate(Long userId) {
        return rewardRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            Reward reward = new Reward();
            reward.setUser(user);
            return rewardRepository.save(reward);
        });
    }

    private RewardResponse mapToResponse(Reward reward) {
        RewardResponse r = new RewardResponse();
        r.setId(reward.getId());
        r.setUserId(reward.getUser().getId());
        r.setPointsBalance(reward.getPointsBalance());
        r.setLastUpdated(reward.getLastUpdated());
        return r;
    }
}

