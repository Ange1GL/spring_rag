package app.rag.domain.port.out;

import app.rag.domain.model.RetrievedChunk;

import java.util.List;

public interface SemanticSearchPort {
    List<RetrievedChunk> search(String query, int topK);
}
