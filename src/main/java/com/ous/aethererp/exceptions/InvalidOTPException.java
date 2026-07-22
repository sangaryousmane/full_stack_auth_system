package com.ous.aethererp.exceptions;


public class InvalidOTPException extends RuntimeException {

    public InvalidOTPException(String message) {
        super(message);
    }
}