package com.savewise.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Request and response bodies for the finance endpoints. Amounts are in RWF with up to two
 * decimal places.
 */
public final class FinanceDtos {

    private FinanceDtos() {
    }

    public record ExpenseRequest(
            @NotBlank @Size(max = 120) String description,
            @NotNull ExpenseCategory category,
            @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal amount,
            @NotNull LocalDate spentOn) {
    }

    public record ExpenseResponse(long id, String description, ExpenseCategory category, BigDecimal amount, LocalDate spentOn) {

        static ExpenseResponse from(Expense e) {
            return new ExpenseResponse(e.getId(), e.getDescription(), e.getCategory(), e.getAmount(), e.getSpentOn());
        }
    }

    public record IncomeRequest(
            @NotBlank @Size(max = 120) String source,
            @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal amount,
            @NotNull LocalDate receivedOn) {
    }

    public record IncomeResponse(long id, String source, BigDecimal amount, LocalDate receivedOn) {

        static IncomeResponse from(Income i) {
            return new IncomeResponse(i.getId(), i.getSource(), i.getAmount(), i.getReceivedOn());
        }
    }

    public record BudgetRequest(
            @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal monthlyLimit) {
    }

    public record BudgetResponse(ExpenseCategory category, BigDecimal monthlyLimit) {

        static BudgetResponse from(Budget b) {
            return new BudgetResponse(b.getCategory(), b.getMonthlyLimit());
        }
    }

    public record GoalRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal targetAmount,
            @NotNull @PositiveOrZero @Digits(integer = 12, fraction = 2) BigDecimal monthlySaving) {
    }

    public record ContributionRequest(
            @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount) {
    }

    public record GoalResponse(
            Long id,
            String name,
            BigDecimal targetAmount,
            BigDecimal savedAmount,
            BigDecimal monthlySaving,
            BigDecimal remainingAmount,
            BigDecimal progressPercentage,
            Integer monthsToGoal,
            boolean completed) {

        public static GoalResponse from(SavingGoal g) {
            BigDecimal remaining = g.remainingAmount();
            return new GoalResponse(g.getId(), g.getName(), g.getTargetAmount(), g.getSavedAmount(), g.getMonthlySaving(),
                    remaining, g.progressPercentage(), g.estimateMonthsToGoal(), remaining.signum() == 0);
        }
    }
}
