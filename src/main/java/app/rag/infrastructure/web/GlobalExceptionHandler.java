package app.rag.infrastructure.web;

import app.rag.domain.exception.InvalidDocumentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.io.IOException;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidDocumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDocument(InvalidDocumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipart(MultipartException ex) {
        return build(HttpStatus.BAD_REQUEST, "invalid multipart request");
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIo(IOException ex) {
        return build(HttpStatus.BAD_REQUEST, "could not read file content");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "unexpected error");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), status.getReasonPhrase(), message));
    }

    public record ErrorResponse(int status, String error, String message) {
        public ErrorResponse {
            if (message == null) {
                message = "";
            }
        }
    }
}
