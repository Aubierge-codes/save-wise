package com.savewise.insights;

import java.time.Clock;
import java.time.YearMonth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.savewise.auth.AuthUser;
import com.savewise.common.Months;
import com.savewise.finance.BudgetRepository;
import com.savewise.finance.ExpenseRepository;
import com.savewise.finance.IncomeRepository;
import com.savewise.finance.SavingGoalRepository;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final IncomeRepository incomes;
    private final ExpenseRepository expenses;
    private final BudgetRepository budgets;
    private final SavingGoalRepository goals;
    private final Clock clock;

    public DashboardController(IncomeRepository incomes, ExpenseRepository expenses, BudgetRepository budgets,
                               SavingGoalRepository goals, Clock clock) {
        this.incomes = incomes;
        this.expenses = expenses;
        this.budgets = budgets;
        this.goals = goals;
        this.clock = clock;
    }

    /**
     * @param month {@code yyyy-MM}; defaults to the current month
     */
    @GetMapping
    @Transactional(readOnly = true)
    public Dashboard dashboard(@AuthenticationPrincipal AuthUser user, @RequestParam(required = false) YearMonth month) {
        YearMonth m = Months.orCurrent(month, clock);
        long userId = user.id();
        return InsightsCalculator.calculate(
                m,
                incomes.findByUserIdAndReceivedOnBetweenOrderByReceivedOnDescIdDesc(userId, Months.first(m), Months.last(m)),
                expenses.findByUserIdAndSpentOnBetweenOrderBySpentOnDescIdDesc(userId, Months.first(m), Months.last(m)),
                budgets.findByUserIdOrderByCategory(userId),
                goals.findByUserIdOrderByCreatedAtAscIdAsc(userId));
    }
}
