package app.rag.domain.model;

import java.util.List;

public record AskQuestionResult(
        String answer,
        List<RetrievedChunk> sources,
        TokenUsage usage
) {
}
