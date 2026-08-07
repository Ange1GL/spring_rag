package app.rag.infrastructure.adapter;

import app.rag.application.config.RetrievalProperties;
import app.rag.domain.model.RetrievedChunk;
import app.rag.domain.port.out.SemanticSearchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PgVectorSemanticSearchAdapter implements SemanticSearchPort {

    private final VectorStore vectorStore;
    private final RetrievalProperties retrievalProperties;

    @Override
    public List<RetrievedChunk> search(String query, int topK) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(retrievalProperties.similarityThreshold())
                .build();
        return vectorStore.similaritySearch(request).stream()
                .map(document -> new RetrievedChunk(
                        document.getText(),
                        (String) document.getMetadata().getOrDefault("source", "unknown"),
                        document.getScore() != null ? document.getScore() : 0.0))
                .toList();
    }
}
