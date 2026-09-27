package com.savewise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    private static final String PASSWORD = "correct-horse-battery";

    @Autowired
    private MockMvc mvc;

    @Test
    void protectedEndpointsRequireASession() throws Exception {
        mvc.perform(get("/api/dashboard")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").cookie(new Cookie("sw_session", "forged.token.value")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void stateChangingRequestsRequireCsrfToken() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(uniqueEmail())))
                .andExpect(status().isForbidden());

        Cookie session = register(uniqueEmail());
        mvc.perform(post("/api/expenses").cookie(session).contentType(MediaType.APPLICATION_JSON)
                        .content(expenseJson("Lunch", "FOOD", 5000, "2026-09-03")))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerLoginAndLogout() throws Exception {
        String email = uniqueEmail();
        Cookie session = register(email);
        assertThat(session.isHttpOnly()).isTrue();

        mvc.perform(get("/api/auth/me").cookie(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(email.toUpperCase())))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Test User"));

        MvcResult logout = mvc.perform(post("/api/auth/logout").with(csrf()).cookie(session))
                .andExpect(status().isNoContent())
                .andReturn();
        assertThat(logout.getResponse().getCookie("sw_session").getMaxAge()).isZero();
    }

    @Test
    void validationErrorsAreReportedPerField() throws Exception {
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"\",\"email\":\"not-an-email\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void repeatedFailedLoginsAreThrottled() throws Exception {
        String email = uniqueEmail();
        register(email);
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson(email, "wrong-password")))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, PASSWORD)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void fullMonthlyFlowProducesDashboard() throws Exception {
        Cookie session = register(uniqueEmail());

        send(session, post("/api/incomes"), "{\"source\":\"Allowance\",\"amount\":50000,\"receivedOn\":\"2026-09-01\"}")
                .andExpect(status().isCreated());
        send(session, post("/api/expenses"), expenseJson("Lunch", "FOOD", 5000, "2026-09-03")).andExpect(status().isCreated());
        send(session, post("/api/expenses"), expenseJson("Movie", "ENTERTAINMENT", 4000, "2026-09-05")).andExpect(status().isCreated());
        // Different month: must not count towards September.
        send(session, post("/api/expenses"), expenseJson("Bus", "TRANSPORT", 999, "2026-08-31")).andExpect(status().isCreated());
        send(session, put("/api/budgets/ENTERTAINMENT"), "{\"monthlyLimit\":5000}").andExpect(status().isOk());
        MvcResult goal = send(session, post("/api/goals"),
                "{\"name\":\"Laptop\",\"targetAmount\":300000,\"monthlySaving\":50000}")
                .andExpect(status().isCreated())
                .andReturn();
        int goalId = JsonPath.read(goal.getResponse().getContentAsString(), "$.id");
        send(session, post("/api/goals/" + goalId + "/contributions"), "{\"amount\":50000}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthsToGoal").value(5));

        mvc.perform(get("/api/expenses").param("month", "2026-09").cookie(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(get("/api/dashboard").param("month", "2026-09").cookie(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("2026-09"))
                .andExpect(jsonPath("$.totalIncome").value(50000))
                .andExpect(jsonPath("$.totalExpenses").value(9000))
                .andExpect(jsonPath("$.remaining").value(41000))
                .andExpect(jsonPath("$.savingRate").value(82.0))
                .andExpect(jsonPath("$.biggestExpense.description").value("Lunch"))
                .andExpect(jsonPath("$.budgets[0].status").value("NEAR_LIMIT"))
                .andExpect(jsonPath("$.recommendation.tone").value("WARNING"))
                .andExpect(jsonPath("$.goals[0].savedAmount").value(50000));
    }

    @Test
    void usersCannotTouchEachOthersData() throws Exception {
        Cookie alice = register(uniqueEmail());
        Cookie bob = register(uniqueEmail());

        MvcResult created = send(alice, post("/api/expenses"), expenseJson("Lunch", "FOOD", 5000, "2026-09-03"))
                .andExpect(status().isCreated())
                .andReturn();
        int id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mvc.perform(delete("/api/expenses/" + id).with(csrf()).cookie(bob)).andExpect(status().isNotFound());
        send(bob, put("/api/expenses/" + id), expenseJson("Hijacked", "OTHER", 1, "2026-09-03"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/expenses").param("month", "2026-09").cookie(bob))
                .andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(delete("/api/expenses/" + id).with(csrf()).cookie(alice)).andExpect(status().isNoContent());
    }

    private ResultActions send(
            Cookie session,
            MockHttpServletRequestBuilder request,
            String json) throws Exception {
        return mvc.perform(request.with(csrf()).cookie(session).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private Cookie register(String email) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(email)))
                .andExpect(status().isCreated())
                .andReturn();
        Cookie session = result.getResponse().getCookie("sw_session");
        assertThat(session).isNotNull();
        return session;
    }

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    private static String registerJson(String email) {
        return "{\"fullName\":\"Test User\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}";
    }

    private static String loginJson(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private static String expenseJson(String description, String category, long amount, String date) {
        return "{\"description\":\"" + description + "\",\"category\":\"" + category + "\",\"amount\":" + amount
                + ",\"spentOn\":\"" + date + "\"}";
    }
}
