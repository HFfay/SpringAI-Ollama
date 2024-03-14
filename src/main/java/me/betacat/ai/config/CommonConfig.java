package me.betacat.ai.config;

import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {

    @Value("${redisUri:redis://localhost:6379}")
    private String redisUri;

    @Bean
    public VectorStore vectorStore(EmbeddingClient embeddingClient) {
        RedisVectorStore.RedisVectorStoreConfig config = RedisVectorStore.RedisVectorStoreConfig.builder()
                .withURI(redisUri)
                // Define the metadata fields to be used
                // in the similarity search filters.
                .withMetadataFields(
                        RedisVectorStore.MetadataField.tag("country"),
                        RedisVectorStore.MetadataField.numeric("year"))
                .build();

        return new RedisVectorStore(config, embeddingClient);
    }
}
