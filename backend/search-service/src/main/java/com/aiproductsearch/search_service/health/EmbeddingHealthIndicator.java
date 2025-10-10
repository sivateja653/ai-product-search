package com.aiproductsearch.search_service.health;

import com.aiproductsearch.search_service.ai.EmbeddingService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class EmbeddingHealthIndicator implements HealthIndicator {

  private final EmbeddingService embeddings;

  public EmbeddingHealthIndicator(EmbeddingService embeddings) {
    this.embeddings = embeddings;
  }

  @Override
  public Health health() { // /called by /actuator/health

    try {
      var v = embeddings.embed("health-check");
      boolean ok = (v != null && !v.isEmpty());
      return ok
          ? Health.up().withDetail("dims", v.size()).build()
          : Health.down().withDetail("reason", "empty vector").build();
    } catch (Exception e) {
      return Health.down(e).build();
    }
  }
}
