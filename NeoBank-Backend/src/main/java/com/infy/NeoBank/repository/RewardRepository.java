package com.infy.NeoBank.repository;

import com.infy.NeoBank.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    // GET /api/rewards/{userId} — one reward record per user (1-to-1)
    Optional<Reward> findByUserId(Long userId);
}