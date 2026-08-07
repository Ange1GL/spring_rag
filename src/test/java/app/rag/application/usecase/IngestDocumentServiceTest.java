package app.rag.application.usecase;

import app.rag.domain.model.IngestResult;
import app.rag.domain.port.out.VectorStorePort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IngestDocumentServiceTest {

    private final VectorStorePort vectorStorePort = mock(VectorStorePort.class);
    private final IngestDocumentService service = new IngestDocumentService(vectorStorePort);

    @Test
    void ingestStoresContentAndReturnsResult() {
        when(vectorStorePort.store("contenido", "manual"))
                .thenReturn(List.of("id-1", "id-2"));

        IngestResult result = service.ingest("contenido");

        assertEquals(2, result.chunkCount());
        assertEquals(List.of("id-1", "id-2"), result.chunkIds());
        verify(vectorStorePort).store("contenido", "manual");
    }

    @Test
    void ingestRejectsBlankContent() {
        assertThrows(IllegalArgumentException.class, () -> service.ingest("   "));
    }

    @Test
    void ingestRejectsNullContent() {
        assertThrows(IllegalArgumentException.class, () -> service.ingest(null));
    }
}
