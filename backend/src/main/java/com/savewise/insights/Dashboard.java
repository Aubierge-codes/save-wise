package com.savewise.insights;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import com.savewise.finance.ExpenseCategory;
import com.savewise.finance.FinanceDtos.GoalResponse;

/**
 * Everything the dashboard shows for one month.
 *
 * @param remaining  income minus expenses; negative when the user overspent
 * @param savingRate share of income left over, as a percentage (may be negative)
 * @param biggestExpense null when the month has no expenses
 */
public record Dashboard(
        YearMonth month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal remaining,
        BigDecimal savingRate,
        boolean spendingTooHigh,
        BiggestExpense biggestExpense,
        List<CategorySpending> spendingByCategory,
        List<BudgetUsage> budgets,
        List<GoalResponse> goals,
        Recommendation recommendation,
        int incomeCount,
        int expenseCount) {

    public record BiggestExpense(String description, ExpenseCategory category, BigDecimal amount) {
    }

    /** @param share percentage of the month's total spending */
    public record CategorySpending(ExpenseCategory category, BigDecimal amount, BigDecimal share) {
    }

    /** @param usedPercent share of the limit already spent; can exceed 100 */
    public record BudgetUsage(
            ExpenseCategory category,
            BigDecimal monthlyLimit,
            BigDecimal spent,
            BigDecimal left,
            BigDecimal usedPercent,
            BudgetStatus status) {
    }

    public enum BudgetStatus {
        UNDER_BUDGET,
        NEAR_LIMIT,
        OVER_BUDGET
    }

    public record Recommendation(Tone tone, String message) {
    }

    public enum Tone {
        GOOD,
        INFO,
        WARNING,
        DANGER
    }
}
