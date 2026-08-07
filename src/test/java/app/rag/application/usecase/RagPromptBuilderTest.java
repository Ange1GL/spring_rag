package app.rag.application.usecase;

import app.rag.domain.model.RetrievedChunk;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RagPromptBuilderTest {

    private final RagPromptBuilder builder = new RagPromptBuilder();

    @Test
    void buildIncludesContextAndQuestion() {
        String prompt = builder.build("¿Política?",
                List.of(new RetrievedChunk("fragmento", "manual", 0.9)), null);

        assertTrue(prompt.contains("CONTEXTO:"));
        assertTrue(prompt.contains("fragmento"));
        assertTrue(prompt.contains("PREGUNTA:"));
        assertTrue(prompt.contains("¿Política?"));
    }

    @Test
    void buildIncludesAdditionalContextWhenProvided() {
        String prompt = builder.build("¿Política?",
                List.of(new RetrievedChunk("fragmento", "manual", 0.9)), "dato personalizado");

        assertTrue(prompt.contains("dato personalizado"));
    }

    @Test
    void buildHandlesEmptyContext() {
        String prompt = builder.build("¿Política?", List.of(), null);

        assertTrue(prompt.contains("(sin contexto recuperado)"));
    }
}
