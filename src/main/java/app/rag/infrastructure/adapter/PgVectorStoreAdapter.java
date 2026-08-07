package app.rag.infrastructure.adapter;

import app.rag.domain.port.out.VectorStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PgVectorStoreAdapter implements VectorStorePort {

    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;

    @Override
    public List<String> store(String content, String source) {
        Document document = Document.builder()
                .text(content)
                .metadata("source", source)
                .build();
        List<Document> chunks = tokenTextSplitter.split(document);
        vectorStore.add(chunks);
        return chunks.stream()
                .map(Document::getId)
                .toList();
    }
}
