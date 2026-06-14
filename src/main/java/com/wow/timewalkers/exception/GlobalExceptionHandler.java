package com.wow.timewalkers.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

// @RestControllerAdvice combines @ControllerAdvice (applies globally to all controllers)
// and @ResponseBody (automatically serializes return values to JSON).
// This is the standard Spring MVC pattern for centralized exception handling —
// instead of putting try/catch blocks in every controller, exceptions propagate here.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @ExceptionHandler registers this method as the handler for CharacterNotFoundException.
    // Spring MVC intercepts any uncaught exception of this type thrown from any controller
    // and invokes this method instead of returning a generic 500 error.
    @ExceptionHandler(CharacterNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(CharacterNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
    }

    // 409 Conflict — the resource already exists (duplicate character name)
    @ExceptionHandler(CharacterNameConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(CharacterNameConflictException ex) {
        return ResponseEntity.status(409).body(Map.of("message", ex.getMessage()));
    }

    // 400 Bad Request — gear validation failed (wrong armor type, invalid weapon, etc.)
    // LinkedHashMap preserves insertion order so "message" always appears before "rejected"
    @ExceptionHandler(GearValidationException.class)
    public ResponseEntity<Map<String, Object>> handleGearValidation(GearValidationException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", ex.getMessage());
        body.put("rejected", ex.getRejections());
        return ResponseEntity.status(400).body(body);
    }
}
