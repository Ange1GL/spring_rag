package app.rag.domain.model;

public record RetrievedChunk(
        String content,
        String source,
        double score
) {
}
