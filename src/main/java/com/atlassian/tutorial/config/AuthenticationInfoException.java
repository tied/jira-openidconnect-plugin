package com.atlassian.tutorial.config;

public class AuthenticationInfoException extends RuntimeException {

    public AuthenticationInfoException(String message) {
        super(message);
    }

    public AuthenticationInfoException(String message, Throwable cause) {
        super(message, cause);
    }
}
