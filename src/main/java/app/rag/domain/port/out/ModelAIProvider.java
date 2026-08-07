package app.rag.domain.port.out;

public interface ModelAIProvider {
    String ask(String prompt);
}
