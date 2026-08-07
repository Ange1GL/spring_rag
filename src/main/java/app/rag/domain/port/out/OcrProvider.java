package app.rag.domain.port.out;

public interface OcrProvider {
    String extractOCR(byte[] file);
}
