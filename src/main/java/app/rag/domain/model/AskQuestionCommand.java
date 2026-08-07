package app.rag.domain.model;

public record AskQuestionCommand(
        String question,
        String additionalContext,
        int topK
) {
}
