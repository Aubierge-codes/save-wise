package com.savewise.finance;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.savewise.auth.AuthUser;
import com.savewise.common.ApiException;
import com.savewise.finance.FinanceDtos.ContributionRequest;
import com.savewise.finance.FinanceDtos.GoalRequest;
import com.savewise.finance.FinanceDtos.GoalResponse;

@RestController
@RequestMapping("/api/goals")
public class SavingGoalController {

    private final SavingGoalRepository goals;

    public SavingGoalController(SavingGoalRepository goals) {
        this.goals = goals;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<GoalResponse> list(@AuthenticationPrincipal AuthUser user) {
        return goals.findByUserIdOrderByCreatedAtAscIdAsc(user.id()).stream().map(GoalResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public GoalResponse create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody GoalRequest req) {
        SavingGoal goal = new SavingGoal(user.id(), req.name().strip(), req.targetAmount(), req.monthlySaving());
        return GoalResponse.from(goals.save(goal));
    }

    @PutMapping("/{id}")
    @Transactional
    public GoalResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable long id,
                               @Valid @RequestBody GoalRequest req) {
        SavingGoal goal = find(user, id);
        goal.update(req.name().strip(), req.targetAmount(), req.monthlySaving());
        return GoalResponse.from(goal);
    }

    /** Adds money to a goal's saved amount. */
    @PostMapping("/{id}/contributions")
    @Transactional
    public GoalResponse contribute(@AuthenticationPrincipal AuthUser user, @PathVariable long id,
                                   @Valid @RequestBody ContributionRequest req) {
        SavingGoal goal = find(user, id);
        goal.addSavings(req.amount());
        return GoalResponse.from(goal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable long id) {
        goals.delete(find(user, id));
    }

    private SavingGoal find(AuthUser user, long id) {
        return goals.findByIdAndUserId(id, user.id()).orElseThrow(() -> ApiException.notFound("Saving goal"));
    }
}
