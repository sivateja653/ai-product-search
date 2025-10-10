package com.aiproductsearch.search_service;

import com.aiproductsearch.search_service.ai.EmbeddingService;
import java.util.Collections;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
class TestStubs {
  @Bean
  @Primary
  EmbeddingService embeddingServiceStub() {
    // Return a constant-size vector; match your model dimension if asserted
    return (String text) -> Collections.nCopies(1536, 0.0f);
  }
}
