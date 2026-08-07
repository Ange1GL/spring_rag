package app.rag.application.usecase;

import app.rag.domain.model.IngestResult;
import app.rag.domain.port.in.IngestDocumentUseCase;
import app.rag.domain.port.out.VectorStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngestDocumentService implements IngestDocumentUseCase {

    private final VectorStorePort vectorStorePort;

    @Override
    public IngestResult ingest(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        List<String> chunkIds = vectorStorePort.store(content, "manual");
        return new IngestResult(chunkIds, chunkIds.size());
    }
}


