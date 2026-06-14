package com.wow.timewalkers.exception;

public class CharacterNameConflictException extends RuntimeException {
    public CharacterNameConflictException(String message) {
        super(message);
    }
}
