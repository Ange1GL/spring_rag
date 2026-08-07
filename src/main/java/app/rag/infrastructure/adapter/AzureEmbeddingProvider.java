package app.rag.infrastructure.adapter;

import app.rag.domain.port.out.EmbeddingProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AzureEmbeddingProvider
implements EmbeddingProvider
{
    private final EmbeddingModel embeddingModel;

    @Override
    public float[] embed(String text) {
        return  embeddingModel.embed(text);
    }
}
