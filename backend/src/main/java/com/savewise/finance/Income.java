package com.savewise.finance;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "incomes")
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(nullable = false, length = 120)
    private String source;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "received_on", nullable = false)
    private LocalDate receivedOn;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Income() {
    }

    public Income(Long userId, String source, BigDecimal amount, LocalDate receivedOn) {
        this.userId = userId;
        this.createdAt = Instant.now();
        update(source, amount, receivedOn);
    }

    public void update(String source, BigDecimal amount, LocalDate receivedOn) {
        this.source = source;
        this.amount = amount;
        this.receivedOn = receivedOn;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getSource() {
        return source;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getReceivedOn() {
        return receivedOn;
    }
}
