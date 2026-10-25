package com.fintech.ledger.common;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

public class IsoCurrencyCodeValidator implements ConstraintValidator<IsoCurrencyCode, String> {

    // Built once from the JDK's ISO 4217 list, then each check is a Set lookup
    private static final Set<String> ISO_CODES = Currency.getAvailableCurrencies().stream()
            .map(Currency::getCurrencyCode)
            .collect(Collectors.toUnmodifiableSet());

    // null counts as valid by Bean Validation convention: @NotNull is responsible for "required"
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || ISO_CODES.contains(value);
    }
}
