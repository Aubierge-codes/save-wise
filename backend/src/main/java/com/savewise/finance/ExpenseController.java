package com.savewise.finance;

import java.time.Clock;
import java.time.YearMonth;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.savewise.auth.AuthUser;
import com.savewise.common.ApiException;
import com.savewise.common.Months;
import com.savewise.finance.FinanceDtos.ExpenseRequest;
import com.savewise.finance.FinanceDtos.ExpenseResponse;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseRepository expenses;
    private final Clock clock;

    public ExpenseController(ExpenseRepository expenses, Clock clock) {
        this.expenses = expenses;
        this.clock = clock;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(@AuthenticationPrincipal AuthUser user, @RequestParam(required = false) YearMonth month) {
        YearMonth m = Months.orCurrent(month, clock);
        return expenses.findByUserIdAndSpentOnBetweenOrderBySpentOnDescIdDesc(user.id(), Months.first(m), Months.last(m))
                .stream().map(ExpenseResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ExpenseResponse create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody ExpenseRequest req) {
        Expense expense = new Expense(user.id(), req.description().strip(), req.category(), req.amount(), req.spentOn());
        return ExpenseResponse.from(expenses.save(expense));
    }

    @PutMapping("/{id}")
    @Transactional
    public ExpenseResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable long id,
                                  @Valid @RequestBody ExpenseRequest req) {
        Expense expense = find(user, id);
        expense.update(req.description().strip(), req.category(), req.amount(), req.spentOn());
        return ExpenseResponse.from(expense);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable long id) {
        expenses.delete(find(user, id));
    }

    private Expense find(AuthUser user, long id) {
        return expenses.findByIdAndUserId(id, user.id()).orElseThrow(() -> ApiException.notFound("Expense"));
    }
}
