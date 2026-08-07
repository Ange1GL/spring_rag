package app.rag.application.usecase;

import app.rag.application.config.RetrievalProperties;
import app.rag.domain.model.AskQuestionCommand;
import app.rag.domain.model.AskQuestionResult;
import app.rag.domain.model.RetrievedChunk;
import app.rag.domain.port.out.ModelAIProvider;
import app.rag.domain.port.out.SemanticSearchPort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AskQuestionServiceTest {

    private final SemanticSearchPort semanticSearchPort = mock(SemanticSearchPort.class);
    private final ModelAIProvider modelAIProvider = mock(ModelAIProvider.class);
    private final RagPromptBuilder ragPromptBuilder = new RagPromptBuilder();
    private final RetrievalProperties retrievalProperties = new RetrievalProperties(5, 0.0);
    private final AskQuestionService service =
            new AskQuestionService(semanticSearchPort, modelAIProvider, ragPromptBuilder, retrievalProperties);

    @Test
    void askReturnsAnswerWithSources() {
        List<RetrievedChunk> chunks = List.of(
                new RetrievedChunk("contenido recuperado", "manual", 0.8));
        when(semanticSearchPort.search("pregunta", 5)).thenReturn(chunks);
        when(modelAIProvider.ask(anyString())).thenReturn("respuesta");

        AskQuestionResult result = service.ask(new AskQuestionCommand("pregunta", null, 0));

        assertEquals("respuesta", result.answer());
        assertEquals(chunks, result.sources());
        verify(semanticSearchPort).search("pregunta", 5);
    }

    @Test
    void askUsesDefaultTopKWhenNotProvided() {
        when(semanticSearchPort.search("pregunta", 5)).thenReturn(List.of());
        when(modelAIProvider.ask(anyString())).thenReturn("respuesta");

        service.ask(new AskQuestionCommand("pregunta", null, 0));

        verify(semanticSearchPort).search("pregunta", 5);
    }

    @Test
    void askUsesConfiguredDefaultTopK() {
        AskQuestionService svc = new AskQuestionService(
                semanticSearchPort, modelAIProvider, ragPromptBuilder, new RetrievalProperties(7, 0.0));
        when(semanticSearchPort.search("pregunta", 7)).thenReturn(List.of());
        when(modelAIProvider.ask(anyString())).thenReturn("respuesta");

        svc.ask(new AskQuestionCommand("pregunta", null, 0));

        verify(semanticSearchPort).search("pregunta", 7);
    }

    @Test
    void askRejectsBlankQuestion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.ask(new AskQuestionCommand("   ", null, 5)));
    }
}
