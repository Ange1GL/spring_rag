package app.rag.domain.model;

public record TokenUsage(
        int promptTokens,
        int completionTokens,
        int totalTokens
) {
}
