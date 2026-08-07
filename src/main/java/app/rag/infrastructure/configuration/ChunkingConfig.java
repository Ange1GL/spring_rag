package app.rag.infrastructure.configuration;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ChunkingProperties.class)
public class ChunkingConfig {

    @Bean
    public TokenTextSplitter tokenTextSplitter(ChunkingProperties properties) {
        return TokenTextSplitter.builder()
                .withChunkSize(properties.chunkSize())
                .withMinChunkSizeChars(properties.minChunkSizeChars())
                .withMinChunkLengthToEmbed(properties.minChunkLengthToEmbed())
                .withMaxNumChunks(properties.maxNumChunks())
                .build();
    }
}
