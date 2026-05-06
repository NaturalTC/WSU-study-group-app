package com.github.wsustudygroupapp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Returns a JSON error body for any exception thrown by a controller. The body always contains
 * <ul>
 *   <li>{@code message} — human-readable error text (kept stable for existing clients)</li>
 *   <li>{@code code} — short machine-readable category, e.g. {@code VALIDATION}, {@code NOT_FOUND}</li>
 *   <li>{@code reference} — short ID that is also written to the server log so support can
 *       correlate a user-reported error with a stack trace</li>
 *   <li>{@code status} — HTTP status as an int</li>
 *   <li>{@code timestamp} — ISO-8601 instant the error was produced</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** ResourceNotFoundException → 404 with structured body. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), ex);
    }

    /** IllegalArgumentException → 400 (typical "validation"/business-rule failure). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION", ex.getMessage(), ex);
    }

    /** Bean-validation failures (@Valid on a controller param) → 400 with all field errors. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());
        String message = details.isEmpty() ? "Validation failed" : String.join("; ", details);
        return build(HttpStatus.BAD_REQUEST, "VALIDATION", message, ex);
    }

    /**
     * Honors the status carried by a {@link ResponseStatusException} (e.g. 403 stays 403).
     * Without this handler, the catch-all RuntimeException handler would coerce every status to 400.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        HttpStatus resolved = HttpStatus.resolve(status.value());
        if (resolved == null) resolved = HttpStatus.INTERNAL_SERVER_ERROR;
        String code = resolved.name();
        String message = ex.getReason() != null ? ex.getReason() : resolved.getReasonPhrase();
        return build(resolved, code, message, ex);
    }

    /**
     * Catches every other RuntimeException and returns 400 with the message — kept for
     * backwards compatibility with existing controllers that throw plain RuntimeExceptions.
     * Prefer throwing a more specific exception when possible.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, "ERROR", ex.getMessage(), ex);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String code, String message, Throwable ex) {
        String reference = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.warn("[{}] {} ({}): {}", reference, code, status.value(), message, ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message",   message);
        body.put("code",      code);
        body.put("reference", reference);
        body.put("status",    status.value());
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(status).body(body);
    }
}
