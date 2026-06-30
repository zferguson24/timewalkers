package com.wow.timewalkers.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // @ExceptionHandler registers this method as the handler for CharacterNotFoundException.
    // Spring MVC intercepts any uncaught exception of this type thrown from any controller
    // and invokes this method instead of returning a generic 500 error.
    @ExceptionHandler(CharacterNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(CharacterNotFoundException ex) {
        log.warn("Character not found: {}", ex.getMessage());
        return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
    }

    // 409 Conflict — the resource already exists (duplicate character name)
    @ExceptionHandler(CharacterNameConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(CharacterNameConflictException ex) {
        log.warn("Character name conflict: {}", ex.getMessage());
        return ResponseEntity.status(409).body(Map.of("message", ex.getMessage()));
    }

    // 400 Bad Request — gear validation failed (wrong armor type, invalid weapon, etc.)
    // LinkedHashMap preserves insertion order so "message" always appears before "rejected"
    @ExceptionHandler(GearValidationException.class)
    public ResponseEntity<Map<String, Object>> handleGearValidation(GearValidationException ex) {
        log.warn("Gear validation failed [{}] — rejected: {}", ex.getMessage(), ex.getRejections());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", ex.getMessage());
        body.put("rejected", ex.getRejections());
        return ResponseEntity.status(400).body(body);
    }

    // 400 Bad Request — invalid race/class combination on character creation
    @ExceptionHandler(InvalidRaceClassCombinationException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRaceClass(InvalidRaceClassCombinationException ex) {
        log.warn("Invalid race/class combination: {}", ex.getMessage());
        return ResponseEntity.status(400).body(Map.of("message", ex.getMessage()));
    }

    // Catch-all for any exception not handled above. Logs the full stack trace so
    // unexpected failures are visible in Railway logs without needing to redeploy with extra instrumentation.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(500).body(Map.of("message", "An unexpected error occurred"));
    }
}
