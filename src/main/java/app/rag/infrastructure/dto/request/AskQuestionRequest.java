package app.rag.infrastructure.dto.request;

public record AskQuestionRequest(
        String question,
        String additionalContext,
        Integer topK
) {
}
