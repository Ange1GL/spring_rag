package app.rag.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rag.retrieval")
public record RetrievalProperties(
        int defaultTopK,
        double similarityThreshold
) {
    public RetrievalProperties {
        if (defaultTopK <= 0) {
            throw new IllegalArgumentException("defaultTopK must be positive");
        }
        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("similarityThreshold must be between 0.0 and 1.0");
        }
    }
}
