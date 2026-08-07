package app.rag.application.usecase;

import app.rag.domain.model.DocumentFile;
import app.rag.domain.model.IngestResult;
import app.rag.domain.port.in.IngestDocumentUseCase;
import app.rag.domain.port.out.OcrProvider;
import app.rag.domain.port.out.VectorStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngestDocumentService implements IngestDocumentUseCase {

    private final VectorStorePort vectorStorePort;
    private final OcrProvider ocrProvider;

    @Override
    public IngestResult ingest(DocumentFile documentFile) {
        String content = ocrProvider.extractOCR(documentFile.content());
        List<String> chunkIds = vectorStorePort.store(content, documentFile.fileName());
        return new IngestResult(chunkIds, chunkIds.size());
    }
}


