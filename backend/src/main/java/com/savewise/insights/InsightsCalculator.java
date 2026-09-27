package com.savewise.insights;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.savewise.finance.Budget;
import com.savewise.finance.Expense;
import com.savewise.finance.ExpenseCategory;
import com.savewise.finance.FinanceDtos.GoalResponse;
import com.savewise.finance.Income;
import com.savewise.finance.SavingGoal;
import com.savewise.insights.Dashboard.BiggestExpense;
import com.savewise.insights.Dashboard.BudgetStatus;
import com.savewise.insights.Dashboard.BudgetUsage;
import com.savewise.insights.Dashboard.CategorySpending;
import com.savewise.insights.Dashboard.Recommendation;
import com.savewise.insights.Dashboard.Tone;

/**
 * The money rules from the original SaveWise CLI, applied to one month of a user's data.
 * Pure and stateless so the rules can be tested without a database.
 */
public final class InsightsCalculator {

    /** Spending at or above this share of income triggers the warning. */
    static final BigDecimal HIGH_SPENDING_PERCENT = BigDecimal.valueOf(80);
    /** A budget is "near limit" once this share of it is spent. */
    static final BigDecimal NEAR_LIMIT_RATIO = new BigDecimal("0.80");
    /** Saving rate that earns the "great job" message. */
    static final BigDecimal GOOD_SAVING_RATE = BigDecimal.valueOf(20);

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private InsightsCalculator() {
    }

    public static Dashboard calculate(YearMonth month, List<Income> incomes, List<Expense> expenses,
                                      List<Budget> budgets, List<SavingGoal> goals) {
        BigDecimal totalIncome = sum(incomes.stream().map(Income::getAmount).toList());
        BigDecimal totalExpenses = sum(expenses.stream().map(Expense::getAmount).toList());
        BigDecimal remaining = totalIncome.subtract(totalExpenses);
        BigDecimal savingRate = percentOf(remaining, totalIncome);
        boolean spendingTooHigh = totalIncome.signum() > 0
                && percentOf(totalExpenses, totalIncome).compareTo(HIGH_SPENDING_PERCENT) >= 0;

        Map<ExpenseCategory, BigDecimal> byCategory = spendingByCategory(expenses);
        List<BudgetUsage> budgetUsage = budgets.stream()
                .sorted(Comparator.comparing(Budget::getCategory))
                .map(b -> usage(b, byCategory.getOrDefault(b.getCategory(), BigDecimal.ZERO)))
                .toList();

        BiggestExpense biggest = expenses.stream()
                .max(Comparator.comparing(Expense::getAmount))
                .map(e -> new BiggestExpense(e.getDescription(), e.getCategory(), e.getAmount()))
                .orElse(null);

        List<CategorySpending> categorySpending = byCategory.entrySet().stream()
                .map(e -> new CategorySpending(e.getKey(), e.getValue(), percentOf(e.getValue(), totalExpenses)))
                .sorted(Comparator.comparing(CategorySpending::amount).reversed())
                .toList();

        return new Dashboard(
                month,
                totalIncome,
                totalExpenses,
                remaining,
                savingRate,
                spendingTooHigh,
                biggest,
                categorySpending,
                budgetUsage,
                goals.stream().map(GoalResponse::from).toList(),
                recommend(totalIncome, spendingTooHigh, budgetUsage, savingRate),
                incomes.size(),
                expenses.size());
    }

    static BudgetStatus statusFor(BigDecimal spent, BigDecimal limit) {
        if (spent.compareTo(limit) > 0) {
            return BudgetStatus.OVER_BUDGET;
        }
        if (spent.compareTo(limit.multiply(NEAR_LIMIT_RATIO)) >= 0) {
            return BudgetStatus.NEAR_LIMIT;
        }
        return BudgetStatus.UNDER_BUDGET;
    }

    private static BudgetUsage usage(Budget budget, BigDecimal spent) {
        BigDecimal limit = budget.getMonthlyLimit();
        return new BudgetUsage(budget.getCategory(), limit, spent, limit.subtract(spent),
                percentOf(spent, limit), statusFor(spent, limit));
    }

    private static Recommendation recommend(BigDecimal income, boolean spendingTooHigh, List<BudgetUsage> budgets,
                                            BigDecimal savingRate) {
        if (income.signum() <= 0) {
            return new Recommendation(Tone.INFO,
                    "Add your income for this month so SaveWise can build your saving plan.");
        }
        if (spendingTooHigh) {
            return new Recommendation(Tone.DANGER,
                    "Try reducing your expenses. You are spending 80% or more of your income.");
        }
        for (BudgetUsage b : budgets) {
            if (b.status() == BudgetStatus.OVER_BUDGET) {
                return new Recommendation(Tone.DANGER,
                        "Reduce your spending on " + label(b.category()) + ". You are over your budget.");
            }
        }
        for (BudgetUsage b : budgets) {
            if (b.status() == BudgetStatus.NEAR_LIMIT) {
                return new Recommendation(Tone.WARNING,
                        "Be careful with " + label(b.category()) + ". You are getting close to your budget.");
            }
        }
        if (savingRate.compareTo(GOOD_SAVING_RATE) >= 0) {
            return new Recommendation(Tone.GOOD, "Great job! You are saving at least 20% of your income.");
        }
        return new Recommendation(Tone.INFO, "Consider saving more of your remaining money.");
    }

    private static Map<ExpenseCategory, BigDecimal> spendingByCategory(List<Expense> expenses) {
        Map<ExpenseCategory, BigDecimal> totals = new EnumMap<>(ExpenseCategory.class);
        for (Expense e : expenses) {
            totals.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
        }
        return totals;
    }

    /** {@code part / whole * 100} to one decimal place; zero when {@code whole} is not positive. */
    static BigDecimal percentOf(BigDecimal part, BigDecimal whole) {
        if (whole.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return part.multiply(HUNDRED).divide(whole, 1, RoundingMode.HALF_UP);
    }

    private static BigDecimal sum(List<BigDecimal> amounts) {
        return amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String label(ExpenseCategory category) {
        return category.name().toLowerCase(Locale.ROOT);
    }
}
