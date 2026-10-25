package com.fintech.ledger.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Web layer only: the service is a Mockito mock, so these tests check HTTP behaviour, not business logic.
@WebMvcTest(AccountController.class)
class AccountControllerTest {

    private static final UUID ACCOUNT_ID = UUID.fromString("3f2b8c1e-5d4a-4e6f-9a7b-1c2d3e4f5a6b");
    private static final Instant CREATED_AT = Instant.parse("2026-10-25T10:15:30Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    void createReturns201WithLocationAndBody() throws Exception {
        when(accountService.create("Alice", "INR")).thenReturn(alicesAccount());

        mockMvc.perform(post("/api/accounts")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("""
                               {"ownerName": "Alice", "currency": "INR"}
                               """))
               .andExpect(status().isCreated())
               .andExpect(header().string("Location", "http://localhost/api/accounts/" + ACCOUNT_ID))
               .andExpect(jsonPath("$.id").value(ACCOUNT_ID.toString()))
               .andExpect(jsonPath("$.ownerName").value("Alice"))
               .andExpect(jsonPath("$.currency").value("INR"))
               .andExpect(jsonPath("$.createdAt").value("2026-10-25T10:15:30Z"))
               // Checked on the raw JSON: jsonPath would parse the number as a double and lose the trailing zero
               .andExpect(content().string(containsString("\"balance\":1250.50")));
    }

    @Test
    void createRejectsBlankOwnerName() throws Exception {
        mockMvc.perform(post("/api/accounts")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("""
                               {"ownerName": "   ", "currency": "INR"}
                               """))
               .andExpect(status().isBadRequest())
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("$.errors.ownerName").exists());

        verifyNoInteractions(accountService);
    }

    @ParameterizedTest
    // "ABC" and "XYZ" have the right shape but are not real ISO 4217 currencies
    @ValueSource(strings = {"inr", "IN", "RUPEE", "", "ABC", "XYZ"})
    void createRejectsInvalidCurrency(String currency) throws Exception {
        mockMvc.perform(post("/api/accounts")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("""
                               {"ownerName": "Alice", "currency": "%s"}
                               """.formatted(currency)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errors.currency").exists());

        verifyNoInteractions(accountService);
    }

    @Test
    void createRejectsMissingCurrency() throws Exception {
        mockMvc.perform(post("/api/accounts")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("""
                               {"ownerName": "Alice"}
                               """))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errors.currency").exists());

        verifyNoInteractions(accountService);
    }

    @Test
    void createRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/accounts")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{\"ownerName\": "))
               .andExpect(status().isBadRequest());

        verifyNoInteractions(accountService);
    }

    @Test
    void getReturnsAccount() throws Exception {
        when(accountService.get(ACCOUNT_ID)).thenReturn(alicesAccount());

        mockMvc.perform(get("/api/accounts/{id}", ACCOUNT_ID))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(ACCOUNT_ID.toString()))
               .andExpect(jsonPath("$.ownerName").value("Alice"))
               .andExpect(jsonPath("$.currency").value("INR"));
    }

    @Test
    void getReturns404WhenAccountDoesNotExist() throws Exception {
        when(accountService.get(ACCOUNT_ID)).thenThrow(new AccountNotFoundException(ACCOUNT_ID));

        mockMvc.perform(get("/api/accounts/{id}", ACCOUNT_ID))
               .andExpect(status().isNotFound())
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("$.title").value("Account not found"))
               .andExpect(jsonPath("$.detail").value(containsString(ACCOUNT_ID.toString())));
    }

    @Test
    void getReturns400WhenIdIsNotAUuid() throws Exception {
        mockMvc.perform(get("/api/accounts/not-a-uuid"))
               .andExpect(status().isBadRequest());

        verifyNoInteractions(accountService);
    }

    // Exercises GlobalExceptionHandler's catch-all: the internal message must never reach the client
    @Test
    void unexpectedErrorReturns500WithoutLeakingDetails() throws Exception {
        when(accountService.get(ACCOUNT_ID)).thenThrow(new IllegalStateException("db password=secret"));

        mockMvc.perform(get("/api/accounts/{id}", ACCOUNT_ID))
               .andExpect(status().isInternalServerError())
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("$.title").value("Internal Server Error"))
               .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
               .andExpect(content().string(not(containsString("secret"))));
    }

    @Test
    void depositReturns200WithUpdatedBalance() throws Exception {
        // Mockito matches arguments with equals(), which for BigDecimal includes the scale:
        // the JSON 500.00 arrives as BigDecimal("500.00"), so this stub matches it (500 would not)
        Account afterDeposit = new Account(ACCOUNT_ID, "Alice", "INR", new BigDecimal("1750.50"), CREATED_AT);
        when(accountService.deposit(ACCOUNT_ID, new BigDecimal("500.00"))).thenReturn(afterDeposit);

        mockMvc.perform(post("/api/accounts/{id}/deposits", ACCOUNT_ID)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("""
                               {"amount": 500.00}
                               """))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(ACCOUNT_ID.toString()))
               .andExpect(content().string(containsString("\"balance\":1750.50")));
    }

    @ParameterizedTest
    // zero, negative, and a third decimal place
    @ValueSource(strings = {"0", "-10.00", "10.001"})
    void depositRejectsInvalidAmount(String amount) throws Exception {
        mockMvc.perform(post("/api/accounts/{id}/deposits", ACCOUNT_ID)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("""
                               {"amount": %s}
                               """.formatted(amount)))
               .andExpect(status().isBadRequest())
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("$.errors.amount").exists());

        verifyNoInteractions(accountService);
    }

    @Test
    void depositRejectsMissingAmount() throws Exception {
        mockMvc.perform(post("/api/accounts/{id}/deposits", ACCOUNT_ID)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errors.amount").exists());

        verifyNoInteractions(accountService);
    }

    @Test
    void depositReturns404WhenAccountDoesNotExist() throws Exception {
        when(accountService.deposit(eq(ACCOUNT_ID), any())).thenThrow(new AccountNotFoundException(ACCOUNT_ID));

        mockMvc.perform(post("/api/accounts/{id}/deposits", ACCOUNT_ID)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("""
                               {"amount": 10.00}
                               """))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.title").value("Account not found"));
    }

    private static Account alicesAccount() {
        return new Account(ACCOUNT_ID, "Alice", "INR", new BigDecimal("1250.50"), CREATED_AT);
    }
}
