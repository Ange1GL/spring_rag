package app.rag.domain.exception;

public class InvalidDocumentException extends RuntimeException {

    public InvalidDocumentException(String message) {
        super(message);
    }
}
