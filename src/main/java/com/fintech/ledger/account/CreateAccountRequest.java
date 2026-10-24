package com.fintech.ledger.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Checked only when the controller parameter is marked @Valid.
// @Pattern accepts null, so @NotNull is needed to make currency required.
public record CreateAccountRequest(
        @NotBlank @Size(max = 100) String ownerName,
        @NotNull @Pattern(regexp = "[A-Z]{3}", message = "must be a 3-letter ISO 4217 code, e.g. INR") String currency) {
}
