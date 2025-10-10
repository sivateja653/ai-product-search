package com.aiproductsearch.search_service;

import com.aiproductsearch.search_service.index.EmbeddingTaskQueue;
import java.util.UUID;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
class TestQueueConfig {

  @Bean
  @Primary
  EmbeddingTaskQueue testEmbeddingTaskQueue() {
    return new EmbeddingTaskQueue() {
      @Override
      public void submit(UUID productId) {
        // no-op in tests
      }
    };
  }
}
