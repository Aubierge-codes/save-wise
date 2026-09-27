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
import com.savewise.finance.FinanceDtos.IncomeRequest;
import com.savewise.finance.FinanceDtos.IncomeResponse;

@RestController
@RequestMapping("/api/incomes")
public class IncomeController {

    private final IncomeRepository incomes;
    private final Clock clock;

    public IncomeController(IncomeRepository incomes, Clock clock) {
        this.incomes = incomes;
        this.clock = clock;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<IncomeResponse> list(@AuthenticationPrincipal AuthUser user, @RequestParam(required = false) YearMonth month) {
        YearMonth m = Months.orCurrent(month, clock);
        return incomes.findByUserIdAndReceivedOnBetweenOrderByReceivedOnDescIdDesc(user.id(), Months.first(m), Months.last(m))
                .stream().map(IncomeResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public IncomeResponse create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody IncomeRequest req) {
        return IncomeResponse.from(incomes.save(new Income(user.id(), req.source().strip(), req.amount(), req.receivedOn())));
    }

    @PutMapping("/{id}")
    @Transactional
    public IncomeResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable long id,
                                 @Valid @RequestBody IncomeRequest req) {
        Income income = find(user, id);
        income.update(req.source().strip(), req.amount(), req.receivedOn());
        return IncomeResponse.from(income);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable long id) {
        incomes.delete(find(user, id));
    }

    private Income find(AuthUser user, long id) {
        return incomes.findByIdAndUserId(id, user.id()).orElseThrow(() -> ApiException.notFound("Income"));
    }
}
