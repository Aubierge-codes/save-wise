package com.savewise.insights;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.savewise.finance.Budget;
import com.savewise.finance.Expense;
import com.savewise.finance.ExpenseCategory;
import com.savewise.finance.Income;
import com.savewise.finance.SavingGoal;
import com.savewise.insights.Dashboard.BudgetStatus;
import com.savewise.insights.Dashboard.Tone;

class InsightsCalculatorTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 9);
    private static final LocalDate DAY = MONTH.atDay(10);

    /** The sample data from the original CLI's Main.java. */
    @Test
    void reproducesTheLegacyCliExample() {
        List<Income> incomes = List.of(income(50_000));
        List<Expense> expenses = List.of(
                expense("Lunch", ExpenseCategory.FOOD, 5_000),
                expense("Bus", ExpenseCategory.TRANSPORT, 2_000),
                expense("Notebook", ExpenseCategory.EDUCATION, 3_000),
                expense("Movie", ExpenseCategory.ENTERTAINMENT, 4_000));
        List<Budget> budgets = List.of(
                budget(ExpenseCategory.FOOD, 15_000),
                budget(ExpenseCategory.TRANSPORT, 10_000),
                budget(ExpenseCategory.EDUCATION, 20_000),
                budget(ExpenseCategory.ENTERTAINMENT, 5_000));
        SavingGoal laptop = new SavingGoal(1L, "Laptop", bd(300_000), bd(50_000));
        laptop.addSavings(bd(50_000));

        Dashboard d = InsightsCalculator.calculate(MONTH, incomes, expenses, budgets, List.of(laptop));

        assertThat(d.totalIncome()).isEqualByComparingTo("50000");
        assertThat(d.totalExpenses()).isEqualByComparingTo("14000");
        assertThat(d.remaining()).isEqualByComparingTo("36000");
        assertThat(d.savingRate()).isEqualByComparingTo("72.0");
        assertThat(d.spendingTooHigh()).isFalse();
        assertThat(d.biggestExpense().description()).isEqualTo("Lunch");

        // Entertainment: 4,000 of 5,000 = 80% -> near limit, which wins over the good saving rate.
        assertThat(d.budgets()).filteredOn(b -> b.category() == ExpenseCategory.ENTERTAINMENT)
                .singleElement()
                .satisfies(b -> assertThat(b.status()).isEqualTo(BudgetStatus.NEAR_LIMIT));
        assertThat(d.recommendation().tone()).isEqualTo(Tone.WARNING);
        assertThat(d.recommendation().message()).contains("entertainment");

        var goal = d.goals().getFirst();
        assertThat(goal.remainingAmount()).isEqualByComparingTo("250000");
        assertThat(goal.progressPercentage()).isEqualByComparingTo("16.7");
        assertThat(goal.monthsToGoal()).isEqualTo(5);
    }

    @Test
    void asksForIncomeWhenThereIsNone() {
        Dashboard d = InsightsCalculator.calculate(MONTH, List.of(), List.of(expense("Bus", ExpenseCategory.TRANSPORT, 500)),
                List.of(), List.of());

        assertThat(d.savingRate()).isEqualByComparingTo("0");
        assertThat(d.spendingTooHigh()).isFalse();
        assertThat(d.recommendation().tone()).isEqualTo(Tone.INFO);
        assertThat(d.recommendation().message()).contains("Add your income");
    }

    @Test
    void warnsWhenSpendingReachesEightyPercentOfIncome() {
        Dashboard d = InsightsCalculator.calculate(MONTH, List.of(income(10_000)),
                List.of(expense("Rent share", ExpenseCategory.OTHER, 8_000)), List.of(), List.of());

        assertThat(d.spendingTooHigh()).isTrue();
        assertThat(d.recommendation().tone()).isEqualTo(Tone.DANGER);
    }

    @Test
    void overBudgetBeatsNearLimit() {
        Dashboard d = InsightsCalculator.calculate(MONTH, List.of(income(100_000)),
                List.of(expense("Snacks", ExpenseCategory.FOOD, 900), expense("Clinic", ExpenseCategory.HEALTH, 2_500)),
                List.of(budget(ExpenseCategory.FOOD, 1_000), budget(ExpenseCategory.HEALTH, 2_000)), List.of());

        assertThat(d.recommendation().message()).isEqualTo("Reduce your spending on health. You are over your budget.");
    }

    @Test
    void praisesAGoodSavingRate() {
        Dashboard d = InsightsCalculator.calculate(MONTH, List.of(income(10_000)),
                List.of(expense("Books", ExpenseCategory.EDUCATION, 1_000)), List.of(), List.of());

        assertThat(d.recommendation().tone()).isEqualTo(Tone.GOOD);
    }

    @Test
    void budgetStatusThresholds() {
        assertThat(InsightsCalculator.statusFor(bd(799), bd(1_000))).isEqualTo(BudgetStatus.UNDER_BUDGET);
        assertThat(InsightsCalculator.statusFor(bd(800), bd(1_000))).isEqualTo(BudgetStatus.NEAR_LIMIT);
        assertThat(InsightsCalculator.statusFor(bd(1_000), bd(1_000))).isEqualTo(BudgetStatus.NEAR_LIMIT);
        assertThat(InsightsCalculator.statusFor(bd(1_001), bd(1_000))).isEqualTo(BudgetStatus.OVER_BUDGET);
    }

    @Test
    void goalWithoutMonthlySavingHasNoEstimate() {
        SavingGoal goal = new SavingGoal(1L, "Phone", bd(100_000), BigDecimal.ZERO);
        assertThat(goal.estimateMonthsToGoal()).isNull();

        goal.addSavings(bd(150_000));
        assertThat(goal.estimateMonthsToGoal()).isZero();
        assertThat(goal.remainingAmount()).isEqualByComparingTo("0");
        assertThat(goal.progressPercentage()).isEqualByComparingTo("100");
    }

    private static Income income(long amount) {
        return new Income(1L, "Allowance", bd(amount), DAY);
    }

    private static Expense expense(String description, ExpenseCategory category, long amount) {
        return new Expense(1L, description, category, bd(amount), DAY);
    }

    private static Budget budget(ExpenseCategory category, long limit) {
        return new Budget(1L, category, bd(limit));
    }

    private static BigDecimal bd(long value) {
        return BigDecimal.valueOf(value);
    }
}
