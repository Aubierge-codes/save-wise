package com.savewise.finance;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(nullable = false, length = 120)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExpenseCategory category;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "spent_on", nullable = false)
    private LocalDate spentOn;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Expense() {
    }

    public Expense(Long userId, String description, ExpenseCategory category, BigDecimal amount, LocalDate spentOn) {
        this.userId = userId;
        this.createdAt = Instant.now();
        update(description, category, amount, spentOn);
    }

    public void update(String description, ExpenseCategory category, BigDecimal amount, LocalDate spentOn) {
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.spentOn = spentOn;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getSpentOn() {
        return spentOn;
    }
}
