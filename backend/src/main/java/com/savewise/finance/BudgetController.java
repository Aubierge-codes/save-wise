package com.savewise.finance;

import java.util.Comparator;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.savewise.auth.AuthUser;
import com.savewise.common.ApiException;
import com.savewise.finance.FinanceDtos.BudgetRequest;
import com.savewise.finance.FinanceDtos.BudgetResponse;

/**
 * Budgets are addressed by category: {@code PUT /api/budgets/FOOD} creates or replaces the
 * food budget.
 */
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetRepository budgets;

    public BudgetController(BudgetRepository budgets) {
        this.budgets = budgets;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<BudgetResponse> list(@AuthenticationPrincipal AuthUser user) {
        return budgets.findByUserIdOrderByCategory(user.id()).stream()
                .sorted(Comparator.comparing(Budget::getCategory))
                .map(BudgetResponse::from)
                .toList();
    }

    @PutMapping("/{category}")
    @Transactional
    public BudgetResponse upsert(@AuthenticationPrincipal AuthUser user, @PathVariable ExpenseCategory category,
                                 @Valid @RequestBody BudgetRequest req) {
        Budget budget = budgets.findByUserIdAndCategory(user.id(), category)
                .orElseGet(() -> new Budget(user.id(), category, req.monthlyLimit()));
        budget.setMonthlyLimit(req.monthlyLimit());
        return BudgetResponse.from(budgets.save(budget));
    }

    @DeleteMapping("/{category}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable ExpenseCategory category) {
        Budget budget = budgets.findByUserIdAndCategory(user.id(), category)
                .orElseThrow(() -> ApiException.notFound("Budget"));
        budgets.delete(budget);
    }
}
