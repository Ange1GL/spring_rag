package app.rag.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rag.chunking")
public record ChunkingProperties(
        int chunkSize,
        int minChunkSizeChars,
        int minChunkLengthToEmbed,
        int maxNumChunks
) {
    public ChunkingProperties {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        if (minChunkSizeChars < 0 || minChunkLengthToEmbed < 0 || maxNumChunks <= 0) {
            throw new IllegalArgumentException("minChunkSizeChars/minChunkLengthToEmbed must not be negative and maxNumChunks must be positive");
        }
    }
}
