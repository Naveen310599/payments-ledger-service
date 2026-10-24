package com.fintech.ledger.common;

// Parent of every "X does not exist" error. GlobalExceptionHandler maps all of them to 404 in one place.
// Abstract so code always throws a specific subclass (AccountNotFoundException, ...), never a vague "not found".
public abstract class NotFoundException extends RuntimeException {

    private final String resource;

    protected NotFoundException(String resource, Object id) {
        super(resource + " " + id + " does not exist");
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }
}
