package app.rag.domain.model;

import java.util.List;

public record IngestResult(
        List<String> chunkIds,
        int chunkCount
) {
}
