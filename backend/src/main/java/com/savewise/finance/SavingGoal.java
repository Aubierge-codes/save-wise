package com.savewise.finance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "saving_goals")
public class SavingGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "target_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "saved_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal savedAmount;

    /** What the user plans to put aside each month; drives the months-to-goal estimate. */
    @Column(name = "monthly_saving", nullable = false, precision = 14, scale = 2)
    private BigDecimal monthlySaving;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SavingGoal() {
    }

    public SavingGoal(Long userId, String name, BigDecimal targetAmount, BigDecimal monthlySaving) {
        this.userId = userId;
        this.savedAmount = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        update(name, targetAmount, monthlySaving);
    }

    public void update(String name, BigDecimal targetAmount, BigDecimal monthlySaving) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.monthlySaving = monthlySaving;
    }

    public void addSavings(BigDecimal amount) {
        if (amount.signum() > 0) {
            savedAmount = savedAmount.add(amount);
        }
    }

    /** Amount still needed; never negative once the goal is reached. */
    public BigDecimal remainingAmount() {
        return targetAmount.subtract(savedAmount).max(BigDecimal.ZERO);
    }

    /** Percentage of the target saved so far, capped at 100. */
    public BigDecimal progressPercentage() {
        if (targetAmount.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return savedAmount.multiply(BigDecimal.valueOf(100))
                .divide(targetAmount, 1, RoundingMode.HALF_UP)
                .min(BigDecimal.valueOf(100));
    }

    /**
     * Months of {@link #monthlySaving} needed to cover what is left, or {@code null} when no
     * monthly saving is planned (the goal can't be estimated). Zero once the goal is reached.
     */
    public Integer estimateMonthsToGoal() {
        BigDecimal remaining = remainingAmount();
        if (remaining.signum() == 0) {
            return 0;
        }
        if (monthlySaving.signum() <= 0) {
            return null;
        }
        return remaining.divide(monthlySaving, 0, RoundingMode.CEILING).intValueExact();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public BigDecimal getSavedAmount() {
        return savedAmount;
    }

    public BigDecimal getMonthlySaving() {
        return monthlySaving;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
