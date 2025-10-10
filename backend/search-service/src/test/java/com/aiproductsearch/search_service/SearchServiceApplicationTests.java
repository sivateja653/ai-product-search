package com.aiproductsearch.search_service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.aiproductsearch.search_service.ai.EmbeddingService;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
      "spring.flyway.enabled=false",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/disabled",
      "spring.kafka.bootstrap-servers=127.0.0.1:65535",
      "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
    })
@ActiveProfiles("test")
@Import(TestQueueConfig.class) // <-- brings in the no-op queue
class SearchServiceApplicationTests {

  @MockBean EmbeddingService embeddingService; // avoid real OpenAI calls
  @MockBean JwtDecoder jwtDecoder; // avoid JWK fetch

  @Test
  void contextLoads() {
    when(embeddingService.embed(anyString())).thenReturn(Collections.nCopies(1536, 0.0f));
  }
}
