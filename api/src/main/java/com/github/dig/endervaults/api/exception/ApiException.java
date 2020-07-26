package com.github.dig.endervaults.api.exception;

public class ApiException extends Exception {
    public ApiException(String errorMessage) {
        super(errorMessage);
    }
}
