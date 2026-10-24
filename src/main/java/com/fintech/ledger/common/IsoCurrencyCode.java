package com.fintech.ledger.common;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// A custom Bean Validation constraint: works like @NotBlank, and failures land in the same "errors" map.
// The three members (message, groups, payload) are required by the Bean Validation spec.
@Documented
@Constraint(validatedBy = IsoCurrencyCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface IsoCurrencyCode {

    String message() default "must be a valid ISO 4217 currency code, e.g. INR";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
