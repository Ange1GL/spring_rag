package app.rag.domain.port.out;

public interface EmbeddingProvider {
    float[] embed(String text);
}
