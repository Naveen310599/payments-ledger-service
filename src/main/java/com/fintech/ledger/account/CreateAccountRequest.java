package com.fintech.ledger.account;

import com.fintech.ledger.common.IsoCurrencyCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Checked only when the controller parameter is marked @Valid.
// @IsoCurrencyCode accepts null, so @NotNull is needed to make currency required.
public record CreateAccountRequest(
        @NotBlank @Size(max = 100) String ownerName,
        @NotNull @IsoCurrencyCode String currency) {
}
