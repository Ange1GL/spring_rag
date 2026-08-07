package app.rag.domain.port.in;

import app.rag.domain.model.DocumentFile;
import app.rag.domain.model.IngestResult;

public interface IngestDocumentUseCase {
    IngestResult ingest(DocumentFile documentFile);
}
