package com.savewise.finance;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingGoalRepository extends JpaRepository<SavingGoal, Long> {

    List<SavingGoal> findByUserIdOrderByCreatedAtAscIdAsc(Long userId);

    Optional<SavingGoal> findByIdAndUserId(Long id, Long userId);
}
