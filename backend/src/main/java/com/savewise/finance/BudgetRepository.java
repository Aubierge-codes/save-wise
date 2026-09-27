package com.savewise.finance;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserIdOrderByCategory(Long userId);

    Optional<Budget> findByUserIdAndCategory(Long userId, ExpenseCategory category);
}
