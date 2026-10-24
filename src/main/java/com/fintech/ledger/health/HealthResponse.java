package com.fintech.ledger.health;

// A record: a small data class. Java generates the constructor, getters, equals, hashCode, toString.
// Spring turns it into JSON automatically: {"status":"UP","service":"payments-ledger-service"}
public record HealthResponse(String status, String service) {
}
